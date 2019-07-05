package win.morannz.m.notificationmanager

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.fragments.RecentsFragment
import kotlin.math.roundToInt

class NotificationManagerService : NotificationListenerService() {
    private val TAG = this::class.java.simpleName
    private var mLastNotificationKey = ""
    private var lastNotificationTime = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // filter duplicate notifications
        if (sbn.key == mLastNotificationKey && System.currentTimeMillis() < lastNotificationTime + 100) return
        mLastNotificationKey = sbn.key
        lastNotificationTime = System.currentTimeMillis()
        Log.d(TAG, "Notification posted: ${sbn.key}")

        // save notification
        val recentNotifications = getRecentNotifications(this).toMutableList()
        val rn = extractDataFromStatusBarNotification(sbn)
        recentNotifications.add(0, rn)
        saveRecentNotifications(this, recentNotifications.take(C.MAX_NUMBER_OF_RECENT_NOTIFICATIONS).toMutableList())
        recordPackageWithNotifications(this, sbn.packageName)

        // refresh recents list if open
        val i = Intent(RecentsFragment.INTENT_UPDATE)
        i.putExtra(RecentsFragment.UPDATE_DATA, Json.stringify(RecentNotification.serializer(), rn))
        LocalBroadcastManager.getInstance(this).sendBroadcast(i)

        // find & play alert group
        val agId = matchNotificationSelector(sbn)?.alertGroupId ?: return
        val alertGroup = matchAlertGroupById(agId) ?: return
        playAlertGroup(alertGroup)
    }

    private fun screenIsOn(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isInteractive
    }

    private fun getRingerMode(audioManager: AudioManager): Int {
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> RingerMode.NORMAL
            AudioManager.RINGER_MODE_SILENT -> RingerMode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerMode.VIBRATE
            else -> 0
        }
    }

    private fun matchNotificationSelector(sbn: StatusBarNotification): NotificationSelector? {
        val n = sbn.notification
        val title: String? = n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text: String? = n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val notificationSelectors = getNotificationSelectors(this)

        // loop through all selectors
        for ((id, ns) in notificationSelectors) {
            // ignore disabled selectors
            if (ns.disabled) {
                continue
            }

            // app creating notification must match (null = do not try to match)
            if (ns.packageName !== null && ns.packageName != sbn.packageName) {
                continue
            }

            // notification title must match (null = do not try to match)
            val matchTitle = ns.matchTitle
            if (matchTitle !== null) {
                if (title === null || !matchTitle.toRegex().matches(title)) {
                    continue
                }
            }

            // notification content must match (null = do not try to match)
            val matchText = ns.matchText
            if (matchText !== null) {
                if (text === null || !matchText.toRegex().matches(text)) {
                    continue
                }
            }

            // limit frequency of alerts
            if (System.currentTimeMillis() < getNotificationSelectorLastAlertTime(this, id) + ns.minSecsBetweenAlerts * 1000) {
                continue
            }

            // if everything matches, return the selector
            saveNotificationSelectorLastAlertTime(this, id, System.currentTimeMillis())
            return ns
        }

        // if no selectors matched, return null
        return null
    }

    private fun matchAlertGroupById(id: Int): AlertGroup? {
        val alertGroups = getAlertGroups(this)
        val ag = alertGroups[id] ?: return null
        if (System.currentTimeMillis() > getAlertGroupLastAlertTime(this, id) + ag.minSecsBetweenAlerts * 1000) {
            saveAlertGroupLastAlertTime(this, id, System.currentTimeMillis())
            return ag
        }
        return null
    }

    private fun playSound(alertGroup: AlertGroup) {
        val sound = Uri.parse(alertGroup.soundUri ?: return) ?: return
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // only alert for enabled ringer modes
        if ((alertGroup.soundRingerModes and getRingerMode(am)) == 0) return

        // we temporarily change the volume so store original value
        val previousVolume = am.getStreamVolume(AudioManager.STREAM_ALARM)
        val ringtone = RingtoneManager.getRingtone(this, sound) ?: return

        ringtone.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        var volumePercent = alertGroup.volumePercent / 100F
        val relativeStreamVolumePercent = am.getStreamVolume(alertGroup.relativeVolumeStream) / am.getStreamMaxVolume(alertGroup.relativeVolumeStream).toFloat()
        val alarmStreamVolumePercent = am.getStreamVolume(AudioManager.STREAM_ALARM) / am.getStreamMaxVolume(AudioManager.STREAM_ALARM).toFloat()
        if (!alertGroup.absoluteVolume) {
            volumePercent *= relativeStreamVolumePercent
        }

        if (volumePercent < alarmStreamVolumePercent && Build.VERSION.SDK_INT >= 28) {
            ringtone.volume = volumePercent / alarmStreamVolumePercent
        }
        else { // need to change the stream volume
            // use ringtone volume if possible
            if (Build.VERSION.SDK_INT >= 28) {
                ringtone.volume = volumePercent
                am.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
            }
            // otherwise set the stream ringtone as best as we can
            else {
                am.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    (volumePercent * am.getStreamMaxVolume(AudioManager.STREAM_ALARM)).roundToInt(),
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                )
            }

            // get media reference (to find duration of sound)
            val media = MediaPlayer.create(this, sound)
            Handler().postDelayed({
                am.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0)
            }, media.duration.toLong())
            media.release()
        }
        ringtone.play()
    }

    private fun playVibration(alertGroup: AlertGroup) {
        // only alert for enabled ringer modes
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if ((alertGroup.vibrationRingerModes and getRingerMode(am)) == 0) return

        // convert pattern (string) to LongArray
        val pattern = alertGroup.vibrationPattern?.split(",")?.map { it.toLong() }?.toLongArray() ?: return
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= 26) {
            val wave = VibrationEffect.createWaveform(pattern, -1)
            v.vibrate(wave)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, -1)
        }
    }

    private fun playAlertGroup(alertGroup: AlertGroup) {
        // option alertWhenScreenOn
        if (!alertGroup.alertWhenScreenOn && screenIsOn()) return

        playSound(alertGroup)
        playVibration(alertGroup)
    }
}
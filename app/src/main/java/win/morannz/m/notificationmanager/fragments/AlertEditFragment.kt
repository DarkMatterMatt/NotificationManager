package win.morannz.m.notificationmanager.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alert_edit.*
import kotlinx.serialization.json.Json
import win.morannz.m.notificationmanager.*


class AlertEditFragment : Fragment() {
    private var agId: Int = -1
    private var ag: AlertGroup = AlertGroup()
    private var agBackup: AlertGroup = AlertGroup()
    private var createNew: Boolean = false
    private var listener: OnFragmentInteractionListener? = null
    private var alertGroups: MutableMap<Int, AlertGroup> = mutableMapOf()
    private var textWatchersEnabled: Boolean = true
    private var relativeVolumeStreams = mutableMapOf<String, Int>()

    private fun EditText.saveAfterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                if (textWatchersEnabled) {
                    afterTextChanged.invoke(editable.toString())
                    alertGroups[agId] = ag
                    saveAlertGroups(activity!!.applicationContext, alertGroups)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNew = arguments?.getBoolean(C.NEW_ALERT_GROUP) ?: false
        agId = arguments?.getInt(C.ALERT_GROUP, -1) ?: -1
        alertGroups = getAlertGroups(activity!!.applicationContext)
        ag = alertGroups[agId] ?: AlertGroup()

        // deep copy the AlertGroup
        agBackup = Json.parse(AlertGroup.serializer(), Json.stringify(AlertGroup.serializer(), ag))

        relativeVolumeStreams = mutableMapOf(
            getString(R.string.alert_edit_relative_volume_stream_notification) to AudioManager.STREAM_NOTIFICATION,
            getString(R.string.alert_edit_relative_volume_stream_alarm) to AudioManager.STREAM_ALARM,
            getString(R.string.alert_edit_relative_volume_stream_music) to AudioManager.STREAM_MUSIC,
            //getString(R.string.alert_edit_relative_volume_stream_accessibility) to AudioManager.STREAM_ACCESSIBILITY,
            getString(R.string.alert_edit_relative_volume_stream_system) to AudioManager.STREAM_SYSTEM,
            getString(R.string.alert_edit_relative_volume_stream_voice_call) to AudioManager.STREAM_VOICE_CALL,
            getString(R.string.alert_edit_relative_volume_stream_dtmf) to AudioManager.STREAM_DTMF,
            getString(R.string.alert_edit_relative_volume_stream_ring) to AudioManager.STREAM_RING
        )

        setHasOptionsMenu(true)
    }

    private fun getRelativeVolumeStreamString(stream: Int): String {
        for ((k, v) in relativeVolumeStreams) {
            if (v == stream) {
                return k
            }
        }
        return null.toString()
    }

    private fun registerWatchers() {
        alert_edit_name.saveAfterTextChanged { ag.name = it }
        alert_edit_comment.saveAfterTextChanged { ag.comment = it }
        alert_edit_min_secs_between_alerts.saveAfterTextChanged { if (it != "") ag.minSecsBetweenAlerts = it.toInt() }
        alert_edit_sound_uri.setOnClickListener { selectSoundUri() }
        alert_edit_vibration_pattern.saveAfterTextChanged { ag.vibrationPattern = it }
        alert_edit_alert_when_screen_on.setOnCheckedChangeListener { _, isChecked ->
            if (textWatchersEnabled) {
                ag.alertWhenScreenOn = isChecked
                alertGroups[agId] = ag
                saveAlertGroups(activity!!.applicationContext, alertGroups)
            }
        }
        alert_edit_absolute_volume.setOnCheckedChangeListener { _, isChecked ->
            if (textWatchersEnabled) {
                ag.absoluteVolume = isChecked
                alertGroups[agId] = ag
                saveAlertGroups(activity!!.applicationContext, alertGroups)
            }
        }
        alert_edit_volume_percent.saveAfterTextChanged { if (it != "") ag.volumePercent = it.toInt() }
        alert_edit_sound_ringer_modes_group.addOnButtonCheckedListener { _, _, _ ->
            if (textWatchersEnabled) {
                var x = 0
                if (alert_edit_sound_ringer_mode_dnd.isChecked) x += RingerMode.DND
                if (alert_edit_sound_ringer_mode_silent.isChecked) x += RingerMode.SILENT
                if (alert_edit_sound_ringer_mode_vibrate.isChecked) x += RingerMode.VIBRATE
                if (alert_edit_sound_ringer_mode_normal.isChecked) x += RingerMode.NORMAL
                ag.soundRingerModes = x
                alertGroups[agId] = ag
                saveAlertGroups(activity!!.applicationContext, alertGroups)
            }
        }
        alert_edit_vibration_ringer_modes_group.addOnButtonCheckedListener { _, _, _ ->
            if (textWatchersEnabled) {
                var x = 0
                if (alert_edit_vibration_ringer_mode_dnd.isChecked) x += RingerMode.DND
                if (alert_edit_vibration_ringer_mode_silent.isChecked) x += RingerMode.SILENT
                if (alert_edit_vibration_ringer_mode_vibrate.isChecked) x += RingerMode.VIBRATE
                if (alert_edit_vibration_ringer_mode_normal.isChecked) x += RingerMode.NORMAL
                ag.vibrationRingerModes = x
                alertGroups[agId] = ag
                saveAlertGroups(activity!!.applicationContext, alertGroups)
            }
        }
        alert_edit_relative_volume_stream.saveAfterTextChanged { ag.relativeVolumeStream = relativeVolumeStreams[it] ?: AudioManager.STREAM_NOTIFICATION }
    }

    private fun populateFields(ag: AlertGroup) {
        textWatchersEnabled = false
        alert_edit_name.setText(ag.name)
        alert_edit_comment.setText(ag.comment)
        alert_edit_min_secs_between_alerts.setText(ag.minSecsBetweenAlerts.toString())
        alert_edit_sound_uri.setText(ag.soundUri)
        alert_edit_vibration_pattern.setText(ag.vibrationPattern)
        alert_edit_alert_when_screen_on.isChecked = ag.alertWhenScreenOn
        alert_edit_absolute_volume.isChecked = ag.absoluteVolume
        alert_edit_volume_percent.setText(ag.volumePercent.toString())
        alert_edit_sound_ringer_mode_dnd.isChecked = (ag.soundRingerModes and RingerMode.DND) != 0
        alert_edit_sound_ringer_mode_silent.isChecked = (ag.soundRingerModes and RingerMode.SILENT) != 0
        alert_edit_sound_ringer_mode_vibrate.isChecked = (ag.soundRingerModes and RingerMode.VIBRATE) != 0
        alert_edit_sound_ringer_mode_normal.isChecked = (ag.soundRingerModes and RingerMode.NORMAL) != 0
        alert_edit_vibration_ringer_mode_dnd.isChecked = (ag.vibrationRingerModes and RingerMode.DND) != 0
        alert_edit_vibration_ringer_mode_silent.isChecked = (ag.vibrationRingerModes and RingerMode.SILENT) != 0
        alert_edit_vibration_ringer_mode_vibrate.isChecked = (ag.vibrationRingerModes and RingerMode.VIBRATE) != 0
        alert_edit_vibration_ringer_mode_normal.isChecked = (ag.vibrationRingerModes and RingerMode.NORMAL) != 0
        alert_edit_relative_volume_stream.setText(getRelativeVolumeStreamString(ag.relativeVolumeStream))
        textWatchersEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alert_edit, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(type: String, data: Any)
    }


    override fun onStart() {
        super.onStart()

        // populate existing data (or blank template if creating a new alert group)
        populateFields(ag)

        // add options for alertGroup dropdown
        val relativeVolumeStreamAdapter = ArrayAdapter(
            activity!!.applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            relativeVolumeStreams.keys.toList()
        )
        alert_edit_relative_volume_stream.setAdapter(relativeVolumeStreamAdapter)

        // add textWatchers
        registerWatchers()
    }

    private fun selectSoundUri() {
        val i = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            //.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:")
            //.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            //.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(i, RequestCode.SOUND_URI)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestCode.SOUND_URI) {
            if (resultCode == Activity.RESULT_OK) {
                val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    ag.soundUri = uri.toString()
                    alertGroups[agId] = ag
                    saveAlertGroups(activity!!.applicationContext, alertGroups)
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
}

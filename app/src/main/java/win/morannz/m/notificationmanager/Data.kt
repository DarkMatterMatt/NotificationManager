package win.morannz.m.notificationmanager

import android.app.Notification
import android.content.Context
import android.media.AudioManager
import android.preference.PreferenceManager
import android.service.notification.StatusBarNotification
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.internal.IntSerializer

/*
 *  NotificationSelectors (NS) and AlertGroups (AG) stored as a map of IntegerId -> NS/AG
 *  MaxIndex Integer stored for both NS and AG
 *  LastAlertTime Long stored individually for each NS/AG
 */

@Serializable
data class NotificationSelector (
    var name: String = "",
    var comment: String = "",

    var alertGroupId: Int? = null,
    var packageName: String? = null,
    var matchText: String? = null,
    var matchTitle: String? = null,
    var minMillisecsBetweenAlerts: Int = 0,
    var disabled: Boolean = false
)

@Serializable
data class AlertGroup (
    var name: String = "",
    var comment: String = "",

    var vibrationPattern: String? = null,
    var soundUri: String? = null,
    var minMillisecsBetweenAlerts: Int = 0,
    var alertWhenScreenOn: Boolean = true,

    var volumePercent: Int = 100,
    var absoluteVolume: Boolean = false,
    var soundRingerModes: Int = RingerMode.NORMAL,
    var vibrationRingerModes: Int = RingerMode.VIBRATE or RingerMode.NORMAL,
    var relativeVolumeStream: Int = AudioManager.STREAM_NOTIFICATION,
    var disabled: Boolean = false
)

@Serializable
data class RecentNotification (
    val packageName: String,
    val time: Long,
    val title: String,
    val text: String
)

// Packages that send notifications
fun getPackagesWithNotifications(context: Context) : MutableList<String> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val packagesString = p.getString(C.PACKAGES_WITH_NOTIFICATIONS, null) ?: return mutableListOf()
    return packagesString.split(",").toMutableList()
}
fun recordPackageWithNotifications(context: Context, packageName: String) {
    if (packageName in C.PACKAGES_WITH_NOTIFICATIONS_EXCLUDE_LIST) {
        return
    }
    val packages = getPackagesWithNotifications(context)
    if (packageName in packages) {
        return
    }
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    packages.add(packageName)
    e.putString(C.PACKAGES_WITH_NOTIFICATIONS, packages.joinToString(","))
    e.apply()
}

// Notification Selectors
fun getNotificationSelectors(context: Context) : MutableMap<Int, NotificationSelector> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val nsString = p.getString(C.NOTIFICATION_SELECTOR, null) ?: return mutableMapOf()
    val ns = Json.parse((IntSerializer to NotificationSelector.serializer()).map, nsString)
    return ns.toMutableMap()
}
fun saveNotificationSelectors(context: Context, notificationSelectors: MutableMap<Int, NotificationSelector>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val nsString = Json.stringify((IntSerializer to NotificationSelector.serializer()).map, notificationSelectors)
    e.putString(C.NOTIFICATION_SELECTOR, nsString)
    e.apply()
}
fun getNotificationSelectorMaxIndex(context: Context) : Int {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getInt(C.NOTIFICATION_SELECTOR + C.MAX_INDEX, -1)
}
fun saveNotificationSelectorMaxIndex(context: Context, maxIndex: Int) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putInt(C.NOTIFICATION_SELECTOR + C.MAX_INDEX, maxIndex)
    e.apply()
}
fun getNotificationSelectorLastAlertTime(context: Context, id: Int) : Long {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getLong(C.NOTIFICATION_SELECTOR + C.LAST_ALERT_TIME + id, 0)
}
fun saveNotificationSelectorLastAlertTime(context: Context, id: Int, lastAlertTime: Long) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putLong(C.NOTIFICATION_SELECTOR + C.LAST_ALERT_TIME + id, lastAlertTime)
    e.apply()
}

// Alert Groups
fun getAlertGroups(context: Context) : MutableMap<Int, AlertGroup> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val agString = p.getString(C.ALERT_GROUP, null) ?: return mutableMapOf()
    val ag = Json.parse((IntSerializer to AlertGroup.serializer()).map, agString)
    return ag.toMutableMap()
}
fun saveAlertGroups(context: Context, alertGroups: MutableMap<Int, AlertGroup>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val agString = Json.stringify((IntSerializer to AlertGroup.serializer()).map, alertGroups)
    e.putString(C.ALERT_GROUP, agString)
    e.apply()
}
fun getAlertGroupMaxIndex(context: Context) : Int {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getInt(C.ALERT_GROUP + C.MAX_INDEX, -1)
}
fun saveAlertGroupMaxIndex(context: Context, maxIndex: Int) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putInt(C.ALERT_GROUP + C.MAX_INDEX, maxIndex)
    e.apply()
}
fun getAlertGroupLastAlertTime(context: Context, id: Int) : Long {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getLong(C.ALERT_GROUP + C.LAST_ALERT_TIME + id, 0)
}
fun saveAlertGroupLastAlertTime(context: Context, id: Int, lastAlertTime: Long) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putLong(C.ALERT_GROUP + C.LAST_ALERT_TIME + id, lastAlertTime)
    e.apply()
}

// Recent Notifications
fun getRecentNotifications(context: Context) : MutableList<RecentNotification> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val rnString = p.getString(C.RECENT_NOTIFICATION, null) ?: return mutableListOf()
    val rn = Json.parse(RecentNotification.serializer().list, rnString)
    return rn.toMutableList()
}
fun saveRecentNotifications(context: Context, recentNotifications: MutableList<RecentNotification>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val rnString = Json.stringify(RecentNotification.serializer().list, recentNotifications)
    e.putString(C.RECENT_NOTIFICATION, rnString)
    e.apply()
}
fun extractDataFromStatusBarNotification(sbn: StatusBarNotification) : RecentNotification {
    val extras = sbn.notification.extras
    return RecentNotification(
        packageName = sbn.packageName,
        time = sbn.postTime,
        title = extras.getString(Notification.EXTRA_TITLE) ?: "",
        text = extras.getString(Notification.EXTRA_TEXT) ?: ""
    )
}
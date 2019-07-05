package win.morannz.m.notificationmanager

import android.app.Notification
import android.content.Context
import android.media.AudioManager
import android.preference.PreferenceManager
import android.service.notification.StatusBarNotification
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.map

/*
 *  NotificationSelectors (NS) and AlertGroups (AG) stored as a map of IntegerId -> NS/AG
 *  MaxIndex Integer stored for both NS and AG
 *  LastAlertTime Long stored individually for each NS/AG
 */

@Serializable
data class NotificationSelector (
    var name: String = "",
    var comment: String = "",
    var minSecsBetweenAlerts: Int = 0,

    var alertGroupId: Int? = null,
    var packageName: String? = null,
    var matchText: String? = null,
    var matchTitle: String? = null,
    var disabled: Boolean = false
)

@Serializable
data class AlertGroup (
    var name: String = "",
    var comment: String = "",
    var minSecsBetweenAlerts: Int = 0,

    var vibrationPattern: String? = null,
    var soundUri: String? = null,
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
private const val PACKAGES_WITH_NOTIFICATIONS = "packagesWithNotifications"
private val PACKAGES_WITH_NOTIFICATIONS_EXCLUDE_LIST = listOf("android")
fun getPackagesWithNotifications(context: Context) : List<String> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val packagesString = p.getString(PACKAGES_WITH_NOTIFICATIONS, null) ?: return listOf()
    return packagesString.split(",")
}
fun recordPackageWithNotifications(context: Context, packageName: String) {
    if (packageName in PACKAGES_WITH_NOTIFICATIONS_EXCLUDE_LIST) {
        return
    }
    val packages = getPackagesWithNotifications(context).toMutableList()
    if (packageName in packages) {
        return
    }
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    packages.add(packageName)
    e.putString(PACKAGES_WITH_NOTIFICATIONS, packages.joinToString(","))
    e.apply()
}

// Notification Selectors
private const val NOTIFICATION_SELECTORS = "notificationSelectors"
private const val NOTIFICATION_SELECTORS_MAX_INDEX = "notificationSelectorsMaxIndex"
private const val NOTIFICATION_SELECTOR_LAST_ALERT_TIME = "notificationSelectorLastAlertTime:"
fun getNotificationSelectors(context: Context) : Map<Int, NotificationSelector> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val nsString = p.getString(NOTIFICATION_SELECTORS, null) ?: return mapOf()
    return Json.parse((IntSerializer to NotificationSelector.serializer()).map, nsString)
}
fun saveNotificationSelectors(context: Context, notificationSelectors: Map<Int, NotificationSelector>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val nsString = Json.stringify((IntSerializer to NotificationSelector.serializer()).map, notificationSelectors)
    e.putString(NOTIFICATION_SELECTORS, nsString)
    e.apply()
}
fun getNotificationSelectorNewIndex(context: Context) : Int {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val newIndex = p.getInt(NOTIFICATION_SELECTORS_MAX_INDEX, -1) + 1
    e.putInt(NOTIFICATION_SELECTORS_MAX_INDEX, newIndex)
    e.apply()
    return newIndex
}
fun getNotificationSelectorLastAlertTime(context: Context, id: Int) : Long {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getLong(NOTIFICATION_SELECTOR_LAST_ALERT_TIME + id, 0)
}
fun saveNotificationSelectorLastAlertTime(context: Context, id: Int, lastAlertTime: Long) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putLong(NOTIFICATION_SELECTOR_LAST_ALERT_TIME + id, lastAlertTime)
    e.apply()
}

// Alert Groups
private const val ALERT_GROUPS = "alertGroups"
private const val ALERT_GROUPS_MAX_INDEX = "alertGroupsMaxIndex"
private const val ALERT_GROUP_LAST_ALERT_TIME = "alertGroupLastAlertTime:"
fun getAlertGroups(context: Context) : Map<Int, AlertGroup> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val agString = p.getString(ALERT_GROUPS, null) ?: return mapOf()
    return Json.parse((IntSerializer to AlertGroup.serializer()).map, agString)
}
fun saveAlertGroups(context: Context, alertGroups: Map<Int, AlertGroup>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val agString = Json.stringify((IntSerializer to AlertGroup.serializer()).map, alertGroups)
    e.putString(ALERT_GROUPS, agString)
    e.apply()
}
fun getAlertGroupNewIndex(context: Context) : Int {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val newIndex = p.getInt(ALERT_GROUPS_MAX_INDEX, -1) + 1
    e.putInt(ALERT_GROUPS_MAX_INDEX, newIndex)
    e.apply()
    return newIndex
}
fun getAlertGroupLastAlertTime(context: Context, id: Int) : Long {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    return p.getLong(ALERT_GROUP_LAST_ALERT_TIME + id, 0)
}
fun saveAlertGroupLastAlertTime(context: Context, id: Int, lastAlertTime: Long) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    e.putLong(ALERT_GROUP_LAST_ALERT_TIME + id, lastAlertTime)
    e.apply()
}

// Recent Notifications
private const val RECENT_NOTIFICATIONS = "recentNotifications"
fun getRecentNotifications(context: Context) : List<RecentNotification> {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val rnString = p.getString(RECENT_NOTIFICATIONS, null) ?: return listOf()
    return Json.parse(RecentNotification.serializer().list, rnString)
}
fun saveRecentNotifications(context: Context, recentNotifications: List<RecentNotification>) {
    val p = PreferenceManager.getDefaultSharedPreferences(context)
    val e = p.edit()
    val rnString = Json.stringify(RecentNotification.serializer().list, recentNotifications)
    e.putString(RECENT_NOTIFICATIONS, rnString)
    e.apply()
}
fun extractDataFromStatusBarNotification(sbn: StatusBarNotification) : RecentNotification {
    val extras = sbn.notification.extras
    return RecentNotification(
        packageName = sbn.packageName,
        time = sbn.postTime,
        title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "",
        text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
    )
}

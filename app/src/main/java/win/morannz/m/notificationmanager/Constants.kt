package win.morannz.m.notificationmanager

object C {
    const val NO_DATA = 0
    const val TAG = "NM3"
    const val MAX_NUMBER_OF_RECENT_NOTIFICATIONS = 10
    const val RECENT_NOTIFICATION   = "RN"
    const val NOTIFICATION_SELECTOR = "NS"
    const val ALERT_GROUP           = "AG"
    const val MAX_INDEX       = "maxIndex"
    const val LAST_ALERT_TIME = "lastAlertTime"
    const val NOTIFICATION_INTENT = "notificationIntent"
    const val NEW_NOTIFICATION_SELECTOR = "createNew$NOTIFICATION_SELECTOR"
    const val NEW_ALERT_GROUP = "createNew$ALERT_GROUP"
    const val PACKAGES_WITH_NOTIFICATIONS = "PN"
    val PACKAGES_WITH_NOTIFICATIONS_EXCLUDE_LIST = listOf("android")
}

object RequestCode {
    const val SOUND_URI = 1
}

enum class FragmentId {
    SETTINGS,
    RECENTS,
    RECENT_LIST,
    RECENT_LIST_ITEM,
    RECENT_VIEW,
    SELECTORS,
    SELECTOR_LIST,
    SELECTOR_LIST_ITEM,
    SELECTOR_EDIT,
    ALERTS,
    ALERT_LIST,
    ALERT_LIST_ITEM,
    ALERT_EDIT,
}

object RingerMode {
    const val SILENT = 1
    const val VIBRATE = 2
    const val NORMAL = 4
}
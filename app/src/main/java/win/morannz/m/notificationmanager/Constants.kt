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
    const val RINGER_SILENT   = 1
    const val RINGER_VIBRATE  = 2
    const val RINGER_NORMAL   = 4
    const val NOTIFICATION_INTENT = "notificationIntent"
    const val NEW_NOTIFICATION_SELECTOR = "createNewNS"
    const val PACKAGES_WITH_NOTIFICATIONS = "PN"
    val PACKAGES_WITH_NOTIFICATIONS_EXCLUDE_LIST = listOf("android")
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
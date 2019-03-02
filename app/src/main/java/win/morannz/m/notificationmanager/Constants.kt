package win.morannz.m.notificationmanager

object C {
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
}

enum class FragmentId {
    RECENTS,
    RECENT_LIST,
    RECENT_LIST_ITEM,
    SELECTORS,
    SELECTOR_LIST,
    SELECTOR_LIST_ITEM,
    ALERTS,
    ALERT_LIST,
    ALERT_LIST_ITEM,
}

object RingerMode {
    const val SILENT = 1
    const val VIBRATE = 2
    const val NORMAL = 4
}
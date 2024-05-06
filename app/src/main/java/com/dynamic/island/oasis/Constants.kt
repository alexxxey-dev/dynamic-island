package com.dynamic.island.oasis

object Constants {
    const val ENTITLEMENT_ID = "pro"

    const val ACTION_BANNER_LOADED = "ACTION_BANNER_LOADED"
    const val ACTION_INTER_LOADED = "ACTION_INTER_LOADED"

    const val ACTION_SUBSCRIPTIONS_LOADED = "ACTION_SUBSCRIPTIONS_LOADED"
    const val ACTION_SUBSCRIPTION_ACTIVATED = "ACTION_SUBSCRIPTION_ACTIVATED"
    const val ACTION_SUBSCRIPTION_DEACTIVATED = "ACTION_SUBSCRIPTION_DEACTIVATED"
    const val SUB_YEARLY_TRIAL = "yearly_trial:yearlytrial"
    const val SUB_MONTHLY_TRIAL = "monthlywithfreetrial:monthlywithfreetrial"
    const val SUB_MONTHLY = "monthly:monthly"
    const val SUB_YEARLY = "yearly:yearly"

    const val ACTION_CONFIG_FETCH = "ACTION_CONFIG_FETCH"

    const val PARAM_PERMISSION_TYPE = "PERMISSION_TYPE"

    const val INTERSTITIAL_FREQ = 3
    const val RATE_FREQ = 20

    const val AD_LOAD_DELAY = 3000L

    const val CONFIG_PARAM_PAYWALL = "paywall"
    const val CONFIG_PARAM_OFFERING = "offering"

    const val PAYWALL_A = "paywall_a"
    const val PAYWALL_B = "paywall_b"
    const val PAYWALL_C = "paywall_c"

    const val MAX_NOTIFICATIONS = 30

    const val SET_BACKGROUND_COLOR = -10
    const val SET_DISABLE_LANDSCAPE = 0
    const val SET_SCREENSHOT_APP = 1
    const val SET_QUICK_ACTION = 2
    const val SET_SHOW_ALERT = 3
    const val SET_CLEAR_NOTIF = 4
    const val SET_SWIPE_TO_SKIP = 10
    const val SET_CLICK_TO_OPEN = 20
    const val SET_NOTIFICATION_ACTIONS = 60
    const val SET_LOCK_SCREEN = 70
    const val SET_DISABLE_NOTIFICATIONS = 80


    const val ACTION_NOTIFICATION_ACTIONS_UPDATED = "NOTIFICATION_ACTIONS_UPDATED"
    const val DIALOG_SIZE_TO_SCREEN = 0.9


    const val URL_TERMS = "https://sites.google.com/view/dynamicoasisterms"
    const val URL_POLICY = "https://sites.google.com/view/oasis-policy"
    const val SUPPORT_EMAIL = "alexey.1.soloviev@gmail.com"


    val LONG_CLICK_VIBRATION = longArrayOf(0, 60)
    val CLICK_VIBRATION = longArrayOf(0, 40)
    const val SWITCH_SPEED = 4f

    const val BUBBLE_DURATION = 500L
    const val EXPAND_TIME = 700L
    const val COLLAPSE_TIME = 200L
    const val PLAY_PAUSE_SPEED = 4f
    const val PLAYBACK_LISTENER_DELAY = 50L
    const val MAX_MUSIC_PROGRESS = 100000.0

    const val ACTION_CONNECTIVITY = "android.net.conn.CONNECTIVITY_CHANGE"

    const val TIMER_DELAY = 500L


    const val DEFAULT_X = 0.5f
    const val DEFAULT_Y = 0f
    const val DEFAULT_WIDTH = 0.3f
    const val DEFAULT_HEIGHT = 0.2f

    const val ONESIGNAL_API_KEY = "052e249d-6caa-4460-8989-4876a8743861"

    const val CODE_PHONE_PERMISSION = 1234
    const val NOTIFICATION_ID = "NOTIFICATION_ID"
    const val DESTROY_MEDIA_LISTENER = "NOTIFICATION_LISTENER_DISCONNECTED"
    const val CREATE_MEDIA_LISTENER = "NOTIFICATION_LISTENER_CONNECTED"
    const val ACTION_NOTIFICATION_LISTENER_SETTINGS =
        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"


    const val PREFS_DI_PARAMS = "PREFS_DI_PARAMS"
    const val PREFS_NOTCH = "PREFS_NOTCH"
    const val PREFS_SHOW_ONBOARDING = "PREFS_SHOW_ONBOARDING"
    const val PREFS_SUBSCRIPTION = "PREFS_SUBSCRIPTION_STATUS_1"
    const val PREFS_FIRST_LAUNCH = "PREFS_FIRST_LAUNCH"
    const val PREFS_LAST_APPS_UPDATE = "PREFS_LAST_APPS_UPDATE"
    const val PREFS_SCREEN_OPENS = "PREFS_SCREEN_OPENS"
    const val PREFS_INTER_COUNT = "PREFS_INTER_COUNT"
    const val PREFS_BACKGROUND_COLOR = "PREFS_BACKGROUND_COLOR"


    const val MIN_DB_UPDATE = 1000 * 60
    const val TOAST_SHORT = 2000L
    const val ACTION_GET_DI_STATE = "ACTION_GET_DI_STATE"
    const val ACTION_SEND_DI_STATE = "ACTION_SEND_DI_STATE"
    const val ACTION_UPDATE_DI_STATE = "ACTION_UPDATE_DI_STATE"
    const val PARAM_DI_STATE = "PARAM_DI_STATE"

    const val ACTION_CHANGE_DI_PARAMS = "ACTION_CHANGE_NOTCH_PARAMS"


    const val ACTION_UPDATE_TIMER_ACTIONS = "ACTION_UPDATE_TIMER_ACTIONS"
    const val ACTION_START_TIMER = "ACTION_START_TIMER"
    const val ACTION_STOP_TIMER = "ACTION_STOP_TIMER"
    const val ACTION_UPDATE_TIMER = "ACTION_UPDATE_TIMER"
    const val PARAM_TIME = "PARAM_TIME"
    const val PARAM_PACKAGE = "PARAM_PACKAGE"
    const val PARAM_COUNT_DOWN = "PARAM_COUNT_DOWN"

    const val PARAM_LAYOUT_ID = "PARAM_LAYOUT_ID"
    const val ACTION_PHONE_PERMISSION = "ACTION_PHONE_PERMISSION"


    const val ACTION_UPDATE_CALL_DATA = "UPDATE_CALL_ACTIONS"
    const val ACTION_UPDATE_BG = "ACTION_UPDATE_BG_COLOR"
    const val ACTION_NEW_NOTIFICATION = "ACTION_NOTIFICATION"
    const val ACTION_REMOVED_NOTIFICATION = "ACTION_REMOVED_NOTIFICATION"

    const val PARAM_NOTIFCITAION_ACTIONS = "PARAM_NOTIFCITAION_ACTIONS"
    const val PARAM_PHONE_TITLE = "PARAM_PHONE_NUMBER"
    const val PARAM_NOTIFICATION_LIST = "PARAM_NOTIFICATION"


}
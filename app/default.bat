adb uninstall com.dynamic.island.oasis

adb install C:\Users\disco\Desktop\my_app\DynamicIsland\app\build\outputs\apk\debug\app-debug.apk

adb shell pm grant com.dynamic.island.oasis android.permission.CALL_PHONE
adb shell pm grant com.dynamic.island.oasis android.permission.ANSWER_PHONE_CALLS
adb shell pm grant com.dynamic.island.oasis android.permission.READ_CONTACTS
adb shell pm grant com.dynamic.island.oasis android.permission.READ_PHONE_STATE

adb shell dumpsys deviceidle whitelist +com.dynamic.island.oasis
adb shell settings put secure enabled_notification_listeners %nlisteners:com.dynamic.island.oasis/com.dynamic.island.oasis.dynamic_island.listeners.notifications.NotificationListener
adb shell cmd notification allow_listener com.dynamic.island.oasis/com.dynamic.island.oasis.dynamic_island.listeners.notifications.NotificationListener
adb shell settings put secure enabled_accessibility_services %accessibility:com.dynamic.island.oasis/com.dynamic.island.oasis.dynamic_island.AcsbService

adb shell am start -n com.dynamic.island.oasis/com.dynamic.island.oasis.ui.splash.SplashActivity



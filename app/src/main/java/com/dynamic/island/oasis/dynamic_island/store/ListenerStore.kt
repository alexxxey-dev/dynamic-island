package com.dynamic.island.oasis.dynamic_island.store

import android.bluetooth.BluetoothDevice
import android.content.*
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.models.PermissionType
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.listeners.ExceptionListener
import com.dynamic.island.oasis.dynamic_island.listeners.PhoneStateListener
import com.dynamic.island.oasis.dynamic_island.listeners.media.MediaListener
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.util.ext.createReceiver
import com.dynamic.island.oasis.util.ext.destroyReceiver
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListenerStore(
    private val acsb: MainService,
    private val permissions: PermissionsUtil,
    private val viewModelStore: ViewModelStore,
    private val media: MediaSessionManager,
    private val telephony: TelephonyManager,
    private val clipboard: ClipboardManager
) {

    private var exceptionHandler: ExceptionListener? = null

    private var mediaListener: MediaListener? = null
    private var phoneStateListener: PhoneStateListener? = null


    private fun createReceiver() {
        acsb.createReceiver(receiver, IntentFilter().apply {
            addAction(Constants.ACTION_NOTIFICATION_ACTIONS_UPDATED)
            addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Constants.ACTION_UPDATE_BG)
            addAction(Constants.ACTION_UPDATE_CALL_DATA)
            addAction(Constants.ACTION_UPDATE_TIMER_ACTIONS)
            addAction(Constants.ACTION_REMOVED_NOTIFICATION)
            addAction(Constants.ACTION_START_TIMER)
            addAction(Constants.ACTION_STOP_TIMER)
            addAction(Constants.ACTION_UPDATE_TIMER)
            addAction(Constants.ACTION_NEW_NOTIFICATION)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(AudioManager.ACTION_HEADSET_PLUG)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(Constants.ACTION_CHANGE_DI_PARAMS)
            addAction(Constants.ACTION_PHONE_PERMISSION)
            addAction(Constants.CREATE_MEDIA_LISTENER)
            addAction(Constants.DESTROY_MEDIA_LISTENER)
        })
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            executeIntentAction(intent, context, isInitialStickyBroadcast)
        }
    }

    init {
        createExceptionListener()
        createReceiver()
        createMediaListener()
        createPhoneListener()
    }

    private fun executeIntentAction(
        intent: Intent,
        context: Context,
        isInitialStickyBroadcast: Boolean
    ) = CoroutineScope(Dispatchers.IO).safeLaunch {
        withContext(Dispatchers.Main) {
            when (intent.action) {
                Constants.ACTION_NOTIFICATION_ACTIONS_UPDATED -> viewModelStore.notificationViewModel.updateActionsVisibility()
                Constants.ACTION_UPDATE_TIMER_ACTIONS -> viewModelStore.timerViewModel.updateNotifActions(
                    intent
                )

                Constants.ACTION_UPDATE_CALL_DATA -> viewModelStore.callViewModel.updateCallData(
                    intent
                )

                Constants.ACTION_UPDATE_BG -> viewModelStore.updateBackground()
                Constants.ACTION_START_TIMER -> viewModelStore.timerViewModel.onTimerStarted(intent)
                Constants.ACTION_UPDATE_TIMER -> viewModelStore.timerViewModel.onTimerUpdated(intent)
                Constants.ACTION_STOP_TIMER -> viewModelStore.timerViewModel.onTimerStopped(intent)
                Constants.DESTROY_MEDIA_LISTENER -> destroyMediaListener()
                Constants.CREATE_MEDIA_LISTENER -> createMediaListener()
                Constants.ACTION_NEW_NOTIFICATION -> viewModelStore.notificationViewModel.onNotificationAdded(
                    intent
                )

                Constants.ACTION_PHONE_PERMISSION -> createPhoneListener()
                Constants.ACTION_REMOVED_NOTIFICATION -> {
                    viewModelStore.notificationViewModel.onSystemNotificationRemove(intent)
                }

                Intent.ACTION_SCREEN_OFF -> {
                    viewModelStore.diViewModel.onScreenOff()
                }

                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> viewModelStore.diViewModel.onScreenAction()
                Constants.ACTION_CHANGE_DI_PARAMS -> viewModelStore.diViewModel.updateDiParams()
                Intent.ACTION_BATTERY_LOW -> viewModelStore.alertViewModel.onBatteryLow()
                Intent.ACTION_POWER_CONNECTED -> viewModelStore.alertViewModel.onChargingStart()
                AudioManager.ACTION_HEADSET_PLUG -> viewModelStore.alertViewModel.onWiredHeadset(
                    isInitialStickyBroadcast
                )

                Intent.ACTION_CONFIGURATION_CHANGED -> viewModelStore.diViewModel.onConfigurationChange(
                    intent
                )

                AudioManager.RINGER_MODE_CHANGED_ACTION -> viewModelStore.alertViewModel.onSoundModeChanged(
                    isInitialStickyBroadcast,
                    intent
                )

                BluetoothDevice.ACTION_ACL_CONNECTED -> viewModelStore.alertViewModel.onWirelessHeadsetConnected(
                    intent,
                    context
                )
            }
        }
    }





    fun onDestroy() {
        acsb.destroyReceiver(receiver)
        toastJob?.cancel()
        destroyMediaListener()
    }


    private var toastJob: Job? = null
    private fun createExceptionListener() {

        exceptionHandler = ExceptionListener(clipboard) {
            if (toastJob?.isActive == true) toastJob?.cancel()
            toastJob = CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(acsb.applicationContext, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createMediaListener() {
        if (permissions.isGranted(PermissionType.NOTIF) && mediaListener == null) {
            mediaListener = MediaListener(acsb, media, viewModelStore.musicViewModel)
        }
    }

    private fun destroyMediaListener() {
        mediaListener?.onDestroy()
        mediaListener = null
    }


    private fun createPhoneListener() {
        if (permissions.isGranted(PermissionType.PHONE)) {
            phoneStateListener = PhoneStateListener(viewModelStore.callViewModel, telephony)
        }
    }


}
package com.dynamic.island.oasis.dynamic_island.ui.features.alert

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.AudioManager
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayViewModel
import com.dynamic.island.oasis.dynamic_island.ui.DiViewModel
import com.dynamic.island.oasis.dynamic_island.util.BatteryUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.util.ext.getBatteryLevel
import kotlinx.coroutines.launch


class AlertViewModel(
    private val prefs: PrefsUtil,
    private val battery: BatteryUtil,
    private val resources: Resources,
    val di: DiViewModel
) : OverlayViewModel(di) {


    private fun showAlertState(state: DiState.Alert){
        di.setState(state)
    }


    fun hideAlertState()=viewModelScope.launch {
        val previous = di.previousState.value
        val currentState = di.state.value
        if(currentState !is DiState.Alert) return@launch

        if(previous is DiState.Alert){
            di.setState(DiState.Main())
        } else if(previous!=null){
            di.setState(previous)
        }
    }


    fun onWiredHeadset(isInitial:Boolean)=viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_SHOW_ALERT)) return@launch
        if(isInitial) return@launch
        if(di.state.value is DiState.IncomingCall) return@launch
        di.setState(
            DiState.Alert(
            R.raw.headphones,
            resources.getString(R.string.wired)
        ))

    }

    fun onSoundModeChanged(isInitial:Boolean, intent: Intent) =viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_SHOW_ALERT)) return@launch
        val mSoundMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)

        val currentState = di.state.value
        val prevState = di.previousState.value
        val prevCall = prevState is DiState.ActiveCall || prevState is DiState.IncomingCall
        val currentCall = currentState is DiState.ActiveCall || currentState is DiState.IncomingCall

        if(isInitial) return@launch
        if (mSoundMode == -1) return@launch
        if(currentCall) return@launch
        if(prevCall) return@launch

        val text = resources.getString(
            when (mSoundMode) {
                AudioManager.RINGER_MODE_NORMAL -> R.string.on
                AudioManager.RINGER_MODE_VIBRATE -> R.string.vibro
                AudioManager.RINGER_MODE_SILENT -> R.string.off
                else -> return@launch
            }
        )
        val animation = when (mSoundMode) {
            AudioManager.RINGER_MODE_NORMAL -> R.raw.sound_on
            AudioManager.RINGER_MODE_VIBRATE -> R.raw.vibration
            AudioManager.RINGER_MODE_SILENT -> R.raw.sound_off
            else -> return@launch
        }
        showAlertState( DiState.Alert( animation, text))
    }


    fun onWirelessHeadsetConnected(intent: Intent, context: Context)=viewModelScope.launch {
        if(!prefs.settingEnabled(Constants.SET_SHOW_ALERT)) return@launch
        if(di.state.value is DiState.IncomingCall) return@launch
        val device =
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice? ?: return@launch

        //TODO check bluetooth class with permission
        //device.bluetoothClass.deviceClass != BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES

        device.getBatteryLevel(context)?.toString()?.let {
            showAlertState(DiState.Alert(R.raw.headphones, "$it%"))
        }
    }


    //adb shell dumpsys battery set ac 1
    fun onChargingStart() =viewModelScope.launch{
        if(!prefs.settingEnabled(Constants.SET_SHOW_ALERT)) return@launch
        if(di.state.value is DiState.IncomingCall) return@launch
        showAlertState(DiState.Alert( R.raw.battery_charging, "${battery.batteryLevel()}%"))
    }

    //adb shell dumpsys battery set level 10
    fun onBatteryLow()=viewModelScope.launch {
        if(!prefs.settingEnabled(Constants.SET_SHOW_ALERT)) return@launch
        if(di.state.value is DiState.IncomingCall) return@launch
        showAlertState(DiState.Alert( R.raw.battery_low, "${battery.batteryLevel()}%"))
    }


}
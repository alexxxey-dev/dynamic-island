package com.dynamic.island.oasis

import android.app.AppOpsManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.PowerManager
import android.os.Vibrator
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.room.Room
import com.dynamic.island.oasis.data.AdSource
import com.dynamic.island.oasis.data.AppDatabase
import com.dynamic.island.oasis.data.BillingUtil
import com.dynamic.island.oasis.data.MyConfig
import com.dynamic.island.oasis.dynamic_island.util.DiParamsProvider
import com.dynamic.island.oasis.ui.apps.AppsViewModel
import com.dynamic.island.oasis.ui.home.HomeViewModel
import com.dynamic.island.oasis.ui.info.InfoViewModel
import com.dynamic.island.oasis.ui.main.MainViewModel
import com.dynamic.island.oasis.ui.onboarding.OnboardingViewModel
import com.dynamic.island.oasis.ui.paywall.PaywallViewModel
import com.dynamic.island.oasis.ui.permissions.PermissionsViewModel
import com.dynamic.island.oasis.ui.settings.SettingsViewModel
import com.dynamic.island.oasis.util.PermissionsUtil
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.UpdateManager
import com.dynamic.island.oasis.data.repository.AppsRepository
import com.dynamic.island.oasis.data.repository.PermissionsRepository
import com.dynamic.island.oasis.data.repository.SettingsRepository
import com.dynamic.island.oasis.dynamic_island.service.MainService
import com.dynamic.island.oasis.dynamic_island.service.ServiceWrapper
import com.dynamic.island.oasis.ui.dialogs.color_picker.ColorPickerViewModel
import com.dynamic.island.oasis.ui.dialogs.rate.RateViewModel
import com.dynamic.island.oasis.ui.splash.SplashActivity
import com.dynamic.island.oasis.util.AutoStart
import com.dynamic.island.oasis.util.LockGuide
import com.dynamic.island.oasis.util.MIUI
import com.dynamic.island.oasis.util.NetworkUtil
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.safeStartActivity
import com.google.gson.Gson
import com.onesignal.OneSignal
import com.onesignal.notifications.INotification
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module


class App : Application() {
    private val android = module {
        single {
            androidContext().getSharedPreferences(
                androidContext().packageName,
                Context.MODE_PRIVATE
            )
        }
        single { androidContext().packageManager }
        single { androidContext().packageName }
        single { androidContext().applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
        single { androidContext().applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
        single { androidContext().applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager }
        single { androidContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager}
        single { androidContext().applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager}
        single { androidContext().applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager}
        single {androidContext().applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    }
    private val utils = module {
        single { MyConfig(androidContext()) }
        single { PrefsUtil(androidContext(), get()) }
        single { LockGuide() }
        single { PermissionsUtil(androidContext(), get(), get(),get(),get()) }
        single { Gson() }
        single { NetworkUtil(get()) }
        single { UpdateManager(androidContext())}
    }

    private val viewModels = module {
        viewModel { OnboardingViewModel(get(), get()) }
        viewModel { PaywallViewModel(get(), get()) }
        viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
        viewModel { InfoViewModel(get(), get()) }
        viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(),get()) }
        viewModel { PermissionsViewModel(get(), get(), get()) }
        viewModel { AppsViewModel(get(), get()) }
        viewModel { ColorPickerViewModel(get()) }
        viewModel { RateViewModel() }
    }


    private val data = module {
        single { DiParamsProvider(get(), get(), androidContext(), get()) }
        single { AdSource(androidContext(), get()) }
        single { buildDatabase() }
        single { AppsRepository(get(), get(), get(), get()) }
        single { PermissionsRepository(get()) }
        single { SettingsRepository(get(), get()) }
        single { BillingUtil(androidContext(), get(), get(), get()) }

    }

    override fun onCreate() {
        super.onCreate()
        MainService.init(this)
        MainService.startViaWorker(this)
        startKoin()
        startOnesignal()
    }



    private fun startOnesignal(){
        OneSignal.initWithContext(this, Constants.ONESIGNAL_API_KEY)
        OneSignal.Notifications.addClickListener(object: INotificationClickListener{
            override fun onClick(event: INotificationClickEvent) {
                val intent = Intent(this@App, SplashActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                safeStartActivity(intent)
                analyticsEvent("push_click")
            }
        })
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    private fun startKoin() {
        startKoin {
            androidContext(this@App)
            modules(android, utils, data, viewModels)
        }
    }


    private fun buildDatabase() = Room
        .databaseBuilder(this, AppDatabase::class.java, packageName)
        .fallbackToDestructiveMigration()
        .build()
}
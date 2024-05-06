package com.dynamic.island.oasis.data

import android.content.Context
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics

//https://developer.android.com/guide/playcore/in-app-updates/kotlin-java#start-update
class UpdateManager(private val context: Context) {
    private var appUpdateManager: AppUpdateManager? = null

    fun onCreate(intentListener: ActivityResultLauncher<IntentSenderRequest>, installListener:InstallStateUpdatedListener) {
        appUpdateManager =  AppUpdateManagerFactory.create(context)
        appUpdateManager?.registerListener(installListener)
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo ?: return
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager?.startUpdateFlowForResult(
                    appUpdateInfo,
                    intentListener,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    fun onResume(view: View?) {
        appUpdateManager
            ?.appUpdateInfo
            ?.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onUpdateDownloaded(view)
                }
            }
    }

    fun onDestroy(listener:InstallStateUpdatedListener) {
        appUpdateManager?.unregisterListener(listener)
    }

     fun onUpdateDownloaded(view: View?) {
        if (view == null) {
            appUpdateManager?.completeUpdate()
            return
        }
         view.context.analyticsEvent("update_downloaded")
        Snackbar.make(
            view,
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") {
                view.context.analyticsEvent("update_install_clicked")
                appUpdateManager?.completeUpdate()
            }
            setActionTextColor(context.resources.getColor(R.color.white))
            show()
        }

    }



}
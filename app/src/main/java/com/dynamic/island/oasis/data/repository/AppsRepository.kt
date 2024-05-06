package com.dynamic.island.oasis.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.data.AppDatabase
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.MyApp
import com.dynamic.island.oasis.util.ext.getAppLogo
import com.dynamic.island.oasis.util.ext.getAppTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsRepository(
    private val db: AppDatabase,
    private val packageManager: PackageManager,
    private val prefs: PrefsUtil,
    private val packageName: String
) {

    suspend fun updateApp(app: MyApp) = withContext(Dispatchers.IO) {
        db.appsDao().insert(app)
        prefs.setCompatible(app.packageName, app.isSelected)
    }


    suspend fun appsCount(): Int = withContext(Dispatchers.IO) {
        return@withContext installedApps().size
    }

    suspend fun loadApps(context:Context,query: String? = null): List<MyApp> = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() - prefs.lastAppsUpdate() >= Constants.MIN_DB_UPDATE) {
            mapInstalledApps(context,installedApps()).forEach { db.appsDao().insert(it) }
            prefs.lastAppsUpdate(System.currentTimeMillis())
        }

        val result = ArrayList<MyApp>()
        val dbApps = db.appsDao().loadAll().sortedBy { it.name }
        val appList = if (query.isNullOrBlank()) {
            dbApps
        } else {
            dbApps.filter { it.name.lowercase().startsWith(query.lowercase()) }
        }
        appList.forEach {
            result.add(it.apply {
                it.logo = packageManager.getAppLogo(it.packageName)
            })
        }


        result
    }

    private suspend fun installedApps(): List<ApplicationInfo> = withContext(Dispatchers.IO) {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                it.flags and ApplicationInfo.FLAG_SYSTEM != 1
                        || it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            }
            .filter { it.packageName != packageName }
    }

    private suspend fun mapInstalledApps(context: Context, installedApps: List<ApplicationInfo>): List<MyApp> =
        withContext(Dispatchers.IO) {
            val result = ArrayList<MyApp>()
            installedApps.forEach {
                val selected = db.appsDao().load(it.packageName)?.isSelected ?: true
                prefs.setCompatible(it.packageName, selected)

                val name = context.getAppTitle(it.packageName, it)
                if (!name.isNullOrBlank()) {
                    result.add(MyApp(it.packageName, name, selected))
                }
            }
            result
        }
}
package com.dynamic.island.oasis.ui.apps

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dynamic.island.oasis.data.PrefsUtil
import com.dynamic.island.oasis.data.models.MyApp
import com.dynamic.island.oasis.data.repository.AppsRepository
import com.dynamic.island.oasis.util.SingleLiveEvent
import com.dynamic.island.oasis.util.ext.safeLaunch
import kotlinx.coroutines.launch



class AppsViewModel(private val repository: AppsRepository, private val prefs: PrefsUtil) :
    ViewModel() {
    val totalApps = MutableLiveData<Int>()
    val query = MutableLiveData<String>()
    val apps = MutableLiveData<List<MyApp>>()
    val allSelected = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()

    val setSelected = SingleLiveEvent<Pair<Boolean, Int>>()
    private val allApps = ArrayList<MyApp>()



     fun init(context: Context) = viewModelScope.safeLaunch{
        loading.value = true
        totalApps.value = repository.appsCount()

        val repositoryApps = repository.loadApps(context)
        allApps.clear()
        allApps.addAll(repositoryApps)
        apps.value = repositoryApps

        allSelected.value = allApps.count { it.isSelected } == allApps.size
        loading.value = false

    }


    fun loadApps(context:Context, query:String?) = viewModelScope.safeLaunch{
         if(query.isNullOrBlank()){
             apps.value = allApps
         } else{
             apps.value = repository.loadApps(context, query)
         }
    }

    fun onAppSelected(selected:Boolean, app: MyApp) {
        viewModelScope.safeLaunch {
            app.isSelected = selected
            allApps.find { it.packageName == app.packageName }?.isSelected = selected
            allSelected.value = allApps.count { it.isSelected } == allApps.size
            repository.updateApp(app)
        }

    }

    fun onSelectAll(view: View) {
        viewModelScope.safeLaunch {
            val mSelected = !(allSelected.value ?: false)
            allSelected.value = mSelected
            updateApps(mSelected)
        }
    }



    private suspend fun updateApps(mSelected:Boolean) {
        val currentApps = ArrayList(apps.value ?: emptyList())
        currentApps.forEachIndexed { index, pair ->
            setSelected.value =(Pair(mSelected, index))
        }
        allApps.forEach { item ->
            item.isSelected = mSelected
            repository.updateApp(item)
        }

    }
}
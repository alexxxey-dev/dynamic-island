package com.dynamic.island.oasis.dynamic_island.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

abstract class OverlayViewModel( val vm: DiViewModel){
    private val job = Job()
    private val coroutineContext = Dispatchers.Main + job
    val viewModelScope = CoroutineScope(coroutineContext)
    open fun onDestroy(){
        job.cancel()
    }
}
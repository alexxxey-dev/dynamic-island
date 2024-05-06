package com.dynamic.island.oasis.dynamic_island.ui.main


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutDiBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.util.ext.hideAlpha
import com.dynamic.island.oasis.util.ext.safeLaunch
import com.dynamic.island.oasis.util.ext.shake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainView(
    private val context: Context,
    private val mainViewModel: MainViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutDiBinding>(
    context,
    mainViewModel,
    R.layout.layout_di,
    false,
    windowManager,
    inflater
) {
    override val registry= LifecycleRegistry(this)

    init {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            binding = createView()
            setupView(binding!!)
            Logs.view("show_main_view")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logs.view("hide_main_view")
    }

    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {}

    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {}


    override fun setupView(binding: LayoutDiBinding) {
        super.setupView(binding)
        binding.apply {
            background.setOnLongClickListener {
                mainViewModel.onLongClicked()
            }
            background.setOnClickListener { mainViewModel.onClicked() }
        }
        mainViewModel.di.screenLocked.observe(this) { locked ->
            handleScreenLock(locked)
        }
    }

    private fun handleScreenLock(locked: Boolean) {
        if (binding == null) return
        if (!mainViewModel.showLockIcon()) {
            binding?.lock?.visibility = View.GONE
            return
        }

        if (locked) {
            binding?.lock?.clearAnimation()
            binding?.lock?.visibility = View.VISIBLE
        } else if (binding?.lock?.visibility == View.VISIBLE) {
            binding?.lock.hideAlpha(250) { binding?.lock?.visibility = View.GONE }
        }
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).safeLaunch {
            if (current !is DiState.Main) {
                return@safeLaunch
            }

//            if (current.animShake) {
//                binding?.background.shake()
//            }
        }
    }


}
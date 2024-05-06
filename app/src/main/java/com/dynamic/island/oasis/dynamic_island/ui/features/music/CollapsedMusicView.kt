package com.dynamic.island.oasis.dynamic_island.ui.features.music

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutCollapsedMusicBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyMusic
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackState
import com.dynamic.island.oasis.util.ext.showAlphaCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CollapsedMusicView(
    private val context: Context,
    private val viewModel: MusicViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutCollapsedMusicBinding>(
    context,
    viewModel,
    R.layout.layout_collapsed_music,
    false, windowManager, inflater
) {
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = previous is DiState.Music && !previous.expanded
        if (same) return
        Logs.view("show_collapsed_music_view")

        binding = createView()
        setupView(binding!!)
        binding?.backgroundSecond?.showAlphaCoroutine(Constants.COLLAPSE_TIME)
    }

    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        Logs.view("hide_collapsed_music_view")
        destroyView()
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.Music || current.expanded) {
                hide(current, previous, diParams)
                return@launch
            }

            show(current, previous, diParams)
        }

    }


    override fun setupView(binding: LayoutCollapsedMusicBinding) {
        super.setupView(binding)
        binding.apply {
            background.setOnClickListener { viewModel.showMusicApp() }
            background.setOnLongClickListener { viewModel.onLongClicked(true) }
            fakeVisualizer.playAnimation()
        }
        viewModel.music.observe(this) {
            updateMusic(it)
        }
        viewModel.playbackState.observe(this) {
            updatePlaybackState(it)
        }
    }


    private fun updatePlaybackState(state: MyPlaybackState) {
        binding?.let { binding ->
            binding.fakeVisualizer.visibility = if (state.isActive) View.VISIBLE else View.INVISIBLE
        }

    }

    private fun updateMusic(music: MyMusic?) {
        if (music == null) return

        binding?.let { binding ->

            if (music.albumLogo != null) {
                binding.mediaLogo.setImageBitmap(music.albumLogo)
            } else {
                binding.mediaLogo.setImageResource(R.drawable.ic_default_album)
            }

        }
    }


}
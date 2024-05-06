package com.dynamic.island.oasis.dynamic_island.ui.features.music

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.dynamic.island.oasis.Constants
import com.dynamic.island.oasis.R
import com.dynamic.island.oasis.databinding.LayoutExpandedMusicBinding
import com.dynamic.island.oasis.dynamic_island.Logs
import com.dynamic.island.oasis.dynamic_island.data.DiParams
import com.dynamic.island.oasis.dynamic_island.ui.OverlayView
import com.dynamic.island.oasis.dynamic_island.data.DiState
import com.dynamic.island.oasis.dynamic_island.data.MyMusic
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackPosition
import com.dynamic.island.oasis.dynamic_island.data.MyPlaybackState
import com.dynamic.island.oasis.util.ext.analyticsEvent
import com.dynamic.island.oasis.util.ext.setSeekListener
import com.dynamic.island.oasis.util.ext.setSwipeListener
import com.dynamic.island.oasis.util.ext.scaleClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class ExpandedMusicView(
    private val context: Context,
    private val viewModel: MusicViewModel,
    private val windowManager: WindowManager,
    private val inflater: LayoutInflater
) : OverlayView<LayoutExpandedMusicBinding>(
    context,
    viewModel,
    R.layout.layout_expanded_music,
    true,windowManager, inflater
) {
    private var playPauseJob: Job? = null
    override val registry= LifecycleRegistry(this)



    override suspend fun show(current: DiState, previous: DiState?, diParams: DiParams) {
        val same = previous is DiState.Music && previous.expanded
        if (same) return


        Logs.view("show_expanded_music_view")
        binding = createView()
        setupView(binding!!)
        expand(
            pivotY = 0f,
            diParams = diParams
        )
    }

    override suspend fun hide(current: DiState, previous: DiState?, diParams: DiParams) {
        collapse(
            pivotY = 0f,
            diParams = diParams
        )
        Logs.view("hide_expanded_music_view")
        destroyView()
    }

    override fun drawState(current: DiState, previous: DiState?, diParams: DiParams) {
        viewJob = CoroutineScope(Dispatchers.Main).launch {
            if (current !is DiState.Music || !current.expanded) {
                hide(current, previous, diParams)
                return@launch
            }
            show(current, previous, diParams)
        }
    }


    override fun setupView(binding: LayoutExpandedMusicBinding) {
        super.setupView(binding)
        binding?.apply {
            progress.max = Constants.MAX_MUSIC_PROGRESS.toInt()
            close.scaleClickListener {
                context.analyticsEvent("on_music_close_clicked")
                viewModel.onStopClicked()
            }
            next.scaleClickListener {
                viewModel.skipToNext()
                context.analyticsEvent("on_music_next_clicked")
            }
            prev.scaleClickListener {
                viewModel.skipToPrevious()
                context.analyticsEvent("on_music_prev_clicked")
            }
            playPause.setOnClickListener {
                viewModel.playPause()
                context.analyticsEvent("on_music_play_pause_clicked")
            }
            progress.setSeekListener {
                viewModel.seekTo(it)
            }
            root.setSwipeListener(
                onTouchOutside = { viewModel.onLongClicked(false) },
                onLeftToRight = {
                    viewModel.skipToPrevious(true)
                    context.analyticsEvent("on_music_swiped")
                },
                onRightToLeft = {
                    viewModel.skipToNext(true)
                    context.analyticsEvent("on_music_swiped")
                }
            )
        }

        viewModel.music.observe(this) {
            updateLayoutType(it)
            showMusic(it)

        }
        viewModel.playbackState.observe(this) {
            updatePlaybackState(it)
        }
        viewModel.playbackPosition.observe(this) {
            updatePlaybackPosition(it)
        }
    }


    private fun updatePlaybackPosition(position: MyPlaybackPosition) {
        binding?.let { binding ->
            binding.passedTime.text = position.currentTime
            binding.totalTime.text = position.totalTime
            binding.progress.setProgress(position.progress, false)
        }
    }


    private fun updatePlaybackState(state: MyPlaybackState) {
        binding?.let { music ->

            binding?.close?.visibility = if (state.isActive) View.INVISIBLE else View.VISIBLE

            if (state.animate) {
                val speed =
                    if (state.isActive) -Constants.PLAY_PAUSE_SPEED else Constants.PLAY_PAUSE_SPEED
                music.playPause.speed = speed
                music.playPause.playAnimation()
            } else {
                music.playPause.progress = if (state.isActive) -1f else 1f
            }
        }
    }



    private fun updateLayoutType(music: MyMusic?) {
        if (music == null) return
        val isInteractive = music.isInteractive
        val duration = music.duration
        val padding = context.resources.getDimension(R.dimen.music_padding).toInt()
        val paddingProgress = context.resources.getDimension(R.dimen.music_padding_progress).toInt()

        binding?.progress?.isEnabled = isInteractive.seek
        binding?.prev?.isEnabled = isInteractive.skipPrev
        binding?.next?.isEnabled = isInteractive.skipNext
        binding?.playPause?.isEnabled = isInteractive.playPause
        if (isInteractive.hasControls() && duration > 0L) {
            binding?.backgroundSecond?.updatePadding(bottom = padding)
            binding?.layoutControls?.visibility = View.VISIBLE
            binding?.layoutProgress?.visibility = View.VISIBLE
            binding?.appLogoControls?.visibility = View.VISIBLE
            binding?.appLogoProgress?.visibility = View.GONE
            binding?.appLogoNothing?.visibility = View.GONE
        } else if (!isInteractive.hasControls() && duration > 0L) {
            binding?.backgroundSecond?.updatePadding(bottom = paddingProgress)
            binding?.layoutControls?.visibility = View.GONE
            binding?.layoutProgress?.visibility = View.VISIBLE
            binding?.appLogoControls?.visibility = View.GONE
            binding?.appLogoProgress?.visibility = View.VISIBLE
            binding?.appLogoNothing?.visibility = View.GONE
        } else {
            binding?.backgroundSecond?.updatePadding(bottom = padding)
            binding?.layoutControls?.visibility = View.GONE
            binding?.layoutProgress?.visibility = View.GONE
            binding?.appLogoControls?.visibility = View.GONE
            binding?.appLogoProgress?.visibility = View.GONE
            binding?.appLogoNothing?.visibility = View.VISIBLE
        }
    }


    private fun showMusic(music: MyMusic?) {
        if (music == null) return
        binding?.let { binding ->
            binding.artist.text = music.artist
            binding.artist.isSelected = true
            binding.title.text = music.title
            binding.title.isSelected = true

            if (music.albumLogo != null) {
                binding.logo.setImageBitmap(music.albumLogo)
            } else {
                binding.logo.setImageResource(R.drawable.ic_default_album)
            }

            if (music.appLogo != null) {
                binding.appLogoControls.setImageDrawable(music.appLogo)
                binding.appLogoNothing.setImageDrawable(music.appLogo)
                binding.appLogoProgress.setImageDrawable(music.appLogo)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       if(playPauseJob?.isActive == true) playPauseJob?.cancel()
    }


}
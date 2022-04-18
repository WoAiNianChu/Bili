package xyz.keriteal.videoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import xyz.keriteal.videoplayer.databinding.VideoPlayerStdBinding

class VolumeView(context: Context, attrs: AttributeSet) : GestureDialog,
    ConstraintLayout(context, attrs) {
    private var _binding: VideoPlayerStdBinding? = null
    val binding get() = _binding!!

    init {
        val inflater = LayoutInflater.from(context)
        _binding = VideoPlayerStdBinding.inflate(inflater, this)
        
    }

    override fun getView(): View {
        return this
    }

    override fun setPercent(percent: Float) {

    }
}
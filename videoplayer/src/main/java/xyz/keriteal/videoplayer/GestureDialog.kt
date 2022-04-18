package xyz.keriteal.videoplayer

import android.view.View

interface GestureDialog {
    fun getView(): View
    fun setPercent(percent: Float)
}
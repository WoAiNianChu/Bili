package xyz.keriteal.videoplayer.utils

import kotlin.math.pow
import kotlin.math.sqrt

fun Pair<Float, Float>.distance(another: Pair<Float, Float>): Float {
    return sqrt(
        (this.first - another.first).pow(2) +
                (this.second - another.second).pow(2)
    )
}
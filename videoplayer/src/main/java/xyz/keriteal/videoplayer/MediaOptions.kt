package xyz.keriteal.videoplayer

sealed class MediaOptions(
    open val mediaCodec: MediaCodec = MediaCodec.SOFTWARE,
    val autoPlay: Boolean = true
) {
    enum class MediaCodec {
        SOFTWARE, HARDWARE
    }
}

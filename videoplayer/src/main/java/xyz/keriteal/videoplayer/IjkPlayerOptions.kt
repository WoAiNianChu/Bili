package xyz.keriteal.videoplayer

data class IjkPlayerOptions(
    override val mediaCodec: MediaCodec = MediaCodec.SOFTWARE,
    val frameDrop: Long = 1
) : MediaOptions(mediaCodec = mediaCodec)
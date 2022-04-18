package xyz.keriteal.bili.models.video

import com.google.gson.annotations.SerializedName

data class BiliVideo(
    @SerializedName("support_formats") val supportFormats: List<BiliSupportFormats>,
    @SerializedName("durl") val parts: List<BiliVideoUrls>
) {
    data class BiliSupportFormats(
        val quality: Short,
        val format: String,
        @SerializedName("new_description") val description: String,
        @SerializedName("display_desc") val shortDescription: String,
    )

    data class BiliVideoUrls(
        val order: Short,
        /**
         * 视频长度
         */
        val length: Int,
        /**
         * 视频大小
         */
        val size: Int,
        val ahead: String,
        val vhead: String,
        val url: String
    )
}

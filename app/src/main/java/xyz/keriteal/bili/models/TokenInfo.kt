package xyz.keriteal.bili.models

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class TokenInfo(
    @SerializedName("mid") val mid: Long,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int
)
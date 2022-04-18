package xyz.keriteal.bili.constants

object PassportConstants {
    const val PASSWORD_ENCRYPT = BaseConstants.PASS_BASE + "/api/oauth2/getKey"
    const val LOGIN = BaseConstants.PASS_BASE + "/x/passport-login/oauth2/login"
    const val REFRESH_TOKEN = BaseConstants.PASS_BASE + "/api/oauth2/refreshToken"
    const val CHECK_TOKEN = BaseConstants.PASS_BASE + "/api/oauth2/info"
    const val SSO = BaseConstants.PASS_BASE + "/api/login/sso"
    const val QR_CODE = BaseConstants.PASS_BASE + "/x/passport-tv-login/qrcode/auth_code"
    const val QR_CODE_CHECK = BaseConstants.PASS_BASE + "/x/passport-tv-login/qrcode/poll"
}
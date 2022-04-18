package xyz.keriteal.bili.constants

object ServiceConstants {
    object HeaderDefaults {
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36 Edg/92.0.902.62"
        const val DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
        const val CONTENT_TYPE_FORM_ENCODED = "application/x-www-form-urlencoded"
        const val CONTENT_TYPE_JSON = "application/json"
        const val CONTENT_TYPE_GRPC = "application/grpc"
        const val DEFAULT_GRPC_TIMEOUT = "20100m"
        const val DEFAULT_TRANSFER_ENCODING_VALUE = "chunked"
        const val DEFAULT_TE = "trailers"
    }

    object Keys {
        const val ANDROID_KEY = "4409e2ce8ffd12b8"
        const val ANDROID_SECRET = "59b43e04ad6965f34319062b478f83dd"
        const val IOS_KEY = "4ebafd7c4951b366"
        const val IOS_SECRET = "8cb98205e9b2ad3669aad0fce12a4c13"
        const val WEB_KEY = "84956560bc028eb7"
        const val WEB_SECRET = "94aba54af9065f71de72f5508f1cd42e"
        const val LOGIN_KEY = "783bbb7264451d82"
        const val LOGIN_SECRET = "2653583c8873dea268ab9386918b1d65"
    }

    object Headers {
        const val BEARER = "Bearer"
        const val IDENTIFY = "identify_v1"
        const val USER_AGENT = "User-Agent"
        const val REFERER = "Referer"
        const val APP_KEY = "APP-KEY"
        const val AUTHORIZATION = "authorization"
    }

    object Query {
        const val IDX = "idx"
        const val FLUSH = "flush"
        const val COLUMN = "column"
        const val DEVICE = "device"
        const val DEVICE_NAME = "device_name"
        const val PULL = "pull"
        const val APP_KEY = "appkey"
        const val BUILD = "build"
        const val MOBILE_APP = "mobi_app"
        const val PLATFORM = "platform"
        const val TIMESTAMP = "ts"
        const val ACCESS_KEY = "access_key"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val SIGN = "sign"
        const val PASSWORD = "password"
        const val USERNAME = "username"

    }

    const val buildNumber = "5520400"
}
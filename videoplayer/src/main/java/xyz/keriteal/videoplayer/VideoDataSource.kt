package xyz.keriteal.videoplayer

/**
 * 每个视频链接(mSourceMap)为key对应url，
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class VideoDataSource(
    private val sourceMap: SourceMap,
    private val headers: Map<String, String> = mapOf(),
    private val key: String,
    var title: String = "",
    val sourceType: Int = DATA_SOURCE_URL
) {
    companion object {
        const val DEFAULT_URL_KEY = "DEFAULT_URL_KEY"

        const val DATA_SOURCE_URL = 0
        const val DATA_SOURCE_ASSETS = 1
    }

    private var mSourceMap: Map<String, String> = mapOf()
    private var mHeaderMap: MutableMap<String, String> = mutableMapOf()
    private var mCurrentKey = DEFAULT_URL_KEY
    val currentKey get() = mCurrentKey
    val currentUrl get() = mSourceMap[currentKey]

    val headerMap get() = mHeaderMap.toMap()

    var keyChangedListener: OnKeyChangedListener? = null

    constructor(url: String, title: String = "", headers: Map<String, String> = mutableMapOf())
            : this(mapOf(DEFAULT_URL_KEY to url), headers, DEFAULT_URL_KEY, title)

    init {
        if (sourceMap.isEmpty()) {
            throw IllegalArgumentException("Source map is empty")
        }
        if (key == "") {
            mCurrentKey = mSourceMap.firstNotNullOf {
                it.key
            }
        }
        mHeaderMap = headers.toMutableMap()
        mSourceMap = sourceMap.toMutableMap()
    }

    fun putHeader(key: String, value: String) {
        mHeaderMap[key] = value
    }

    fun setKey(key: String) {
        mCurrentKey = key
        keyChangedListener?.onKeyChanged(this)
    }

    interface OnKeyChangedListener {
        fun onKeyChanged(dataSource: VideoDataSource)
    }
}

typealias SourceMap = Map<String, String>
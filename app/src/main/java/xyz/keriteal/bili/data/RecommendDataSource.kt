package xyz.keriteal.bili.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.enums.RequestClientType
import xyz.keriteal.bili.models.home.RecommendCardResponse
import xyz.keriteal.bili.models.home.RecommendCardDataModel
import xyz.keriteal.bili.service.AccountService
import xyz.keriteal.bili.service.RecommendService
import xyz.keriteal.bili.utils.generateAuthorizedQueryMap

class RecommendDataSource(
    private val recommendService: RecommendService,
    private val offsetIndex: Int
) : PagingSource<Int, RecommendCardDataModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecommendCardDataModel> {
        val index = params.key ?: 0
        return try {
            val parameterMap = mapOf(
                ServiceConstants.Query.IDX to index.toString(),
                ServiceConstants.Query.FLUSH to "5",
                ServiceConstants.Query.COLUMN to "4",
                ServiceConstants.Query.DEVICE to "pad",
                ServiceConstants.Query.DEVICE_NAME to "iPad6",
                ServiceConstants.Query.PULL to (index == 0).toString().lowercase()
            )
            val recommendsResponse = withContext(Dispatchers.IO) {
                recommendService.getRecommend(
                    parameterMap.generateAuthorizedQueryMap(RequestClientType.IOS)
                ).data.items
            }
            val avatarItems = getAvatarUrl(recommendsResponse)
            val data = recommendsResponse.map { recommend ->
                RecommendCardDataModel(
                    coverUrl = recommend.cover,
                    upName = recommend.args.upName,
                    upAvatarUrl = avatarItems[recommend.args.upId.toString()] ?: "",
                    title = recommend.title,
                    watched = recommend.watchedDescription ?: recommend.watched,
                    danmaku = recommend.danmakuDescription ?: recommend.danmaku,
                    avId = recommend.playerArgs.aid ?: 0,
                    bvId = recommend.playerArgs.bid ?: "",
                    cId = recommend.playerArgs.cid
                )
            }.toMutableList()
            LoadResult.Page(
                data = data,
                prevKey = if (index == 0) null else index,
                nextKey = index + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecommendCardDataModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    private suspend fun getAvatarUrl(items: List<RecommendCardResponse>): Map<String, String> =
        withContext(Dispatchers.IO) {
            val ids = items.map { it.args.upId.toString() }
            val jsonObj = AccountService.instance
                .getMultipleUserInfo(ids)
                .data
            val gson = Gson()
            val jsonMap: Map<String, Map<*, *>> =
                gson.fromJson<Map<String, Map<*, *>>>(jsonObj, Map::class.java)
            ids.associateWith { id ->
                (jsonMap[id]!!["info"]!! as Map<*, *>)["face"] as String
            }
        }
}
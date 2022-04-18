package xyz.keriteal.bili.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import xyz.keriteal.bili.models.home.RecommendCardDataModel
import xyz.keriteal.bili.service.RecommendService

class RecommendRepository(
    private val service: RecommendService
) {
    fun getRecommendResultStream(offsetIndex: Int): Flow<PagingData<RecommendCardDataModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { RecommendDataSource(service, offsetIndex)}
        ).flow
    }
}
package xyz.keriteal.bili.ui.home.recommend

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.keriteal.bili.constants.ServiceConstants
import xyz.keriteal.bili.data.RecommendDataSource
import xyz.keriteal.bili.data.RecommendRepository
import xyz.keriteal.bili.enums.RequestClientType
import xyz.keriteal.bili.models.home.RecommendCardResponse
import xyz.keriteal.bili.models.home.RecommendCardDataModel
import xyz.keriteal.bili.service.AccountService
import xyz.keriteal.bili.service.RecommendService
import xyz.keriteal.bili.utils.RetrofitUtils

class RecommendViewModel(
    private val repository: RecommendRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var currentOffsetIndex: Int? = null
    private var currentRecommendCards: Flow<PagingData<RecommendCardDataModel>>? = null

    fun loadRecommend(offsetIndex: Int): Flow<PagingData<RecommendCardDataModel>> {
        val newValue = repository.getRecommendResultStream(offsetIndex)
            .cachedIn(viewModelScope)
        currentRecommendCards = newValue
        return newValue
    }
}

private val UiAction.Scroll.shouldFetchMore
    get() = visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount

sealed class UiAction {
    data class Recommend(val offsetIndex: Int) : UiAction()
    data class Scroll(
        val visibleItemCount: Int,
        val lastVisibleItemPosition: Int,
        val totalItemCount: Int
    ) : UiAction()
}

data class UiState(
    val offsetIndex: Int,
    val recommendResult: RecommendResult
)

private const val VISIBLE_THRESHOLD = 5

sealed class UiModel {
    data class RecommendCardItem(val recommendCard: RecommendCardDataModel) : UiModel()
    data class SeparatorItem(val description: String) : UiModel()
}

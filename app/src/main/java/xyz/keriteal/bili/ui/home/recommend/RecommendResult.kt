package xyz.keriteal.bili.ui.home.recommend

import xyz.keriteal.bili.models.home.RecommendCardDataModel
import java.lang.Exception

sealed class RecommendResult {
    data class Success(val data: List<RecommendCardDataModel>) : RecommendResult()
    data class Error(val error: Exception) : RecommendResult()
}
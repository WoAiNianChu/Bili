package xyz.keriteal.bili.ui.home.recommend

import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import xyz.keriteal.bili.data.RecommendRepository
import xyz.keriteal.bili.service.RecommendService

object Injection {
    private fun provideRecommendCardRepository(): RecommendRepository {
        return RecommendRepository(RecommendService.instance)
    }

    fun provideViewModelFactory(owner: SavedStateRegistryOwner): ViewModelProvider.Factory {
        return RecommendViewModelFactory(owner, provideRecommendCardRepository())
    }
}
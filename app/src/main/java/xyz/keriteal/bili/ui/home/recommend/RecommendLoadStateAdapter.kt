package xyz.keriteal.bili.ui.home.recommend

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class RecommendLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<RecommendLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: RecommendLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecommendLoadStateViewHolder {
        return RecommendLoadStateViewHolder.create(parent, retry)
    }
}
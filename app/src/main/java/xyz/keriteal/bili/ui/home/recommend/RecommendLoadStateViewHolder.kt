package xyz.keriteal.bili.ui.home.recommend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import xyz.keriteal.bili.R
import xyz.keriteal.bili.databinding.ItemLoadStateFooterViewItemBinding

class RecommendLoadStateViewHolder(
    private val binding: ItemLoadStateFooterViewItemBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): RecommendLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_load_state_footer_view_item, parent, false)
            val binding = ItemLoadStateFooterViewItemBinding.bind(view)
            return RecommendLoadStateViewHolder(binding, retry)
        }
    }
}
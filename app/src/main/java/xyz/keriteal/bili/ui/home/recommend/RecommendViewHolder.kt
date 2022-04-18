package xyz.keriteal.bili.ui.home.recommend

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.keriteal.bili.R
import xyz.keriteal.bili.databinding.ItemHomeRecommendLayoutBinding
import xyz.keriteal.bili.models.home.RecommendCardDataModel
import xyz.keriteal.bili.utils.BitmapUtils

class RecommendViewHolder(
    private val binding: ItemHomeRecommendLayoutBinding,
    private val onRecommendListener: RecommendAdapter.OnRecommendListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private var recommendCard: RecommendCardDataModel? = null

    init {
        binding.root.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        recommendCard?.let { it ->
            onRecommendListener.onRecommendClicked(it)
        }
    }

    fun bind(recommendCard: RecommendCardDataModel?) {
        recommendCard?.let {
            showData(recommendCard)
        }
    }

    private fun showData(data: RecommendCardDataModel) {
        this.recommendCard = data
        val avatarView = binding.recommendAvatarImageView
        val coverView = binding.recommendCoverImageView
        binding.recommendUpNameTextView.text = data.upName
        binding.recommendDanmakuTextView.text = data.danmaku
        binding.recommendWatchedTextView.text = data.watched
        binding.recommendVideoNameTextView.text = data.title
        binding.recommendCoverImageView.setImageBitmap(null)
        binding.recommendAvatarImageView.setImageBitmap(RecommendAdapter.defaultUpHeader)
        // 加载视频封面
        CoroutineScope(Dispatchers.Main).launch {
            binding.recommendCoverImageView.setImageBitmap(
                BitmapUtils.loadBitmap(
                    binding.root.context,
                    data.coverUrl,
                    fallbackResourceId = R.drawable.noface,
                    width = coverView.width,
                    height = coverView.height,
                    saveToDisk = false
                )
            )
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.recommendAvatarImageView.setImageBitmap(
                BitmapUtils.loadBitmap(
                    binding.root.context,
                    data.upAvatarUrl,
                    width = avatarView.width,
                    height = avatarView.height,
                    savedDirectory = "avatar",
                    fallbackResourceId = R.drawable.noface
                )
            )
        }
    }
}
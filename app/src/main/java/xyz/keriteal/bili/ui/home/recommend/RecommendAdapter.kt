package xyz.keriteal.bili.ui.home.recommend

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import xyz.keriteal.bili.R
import xyz.keriteal.bili.databinding.ItemHomeRecommendLayoutBinding
import xyz.keriteal.bili.models.home.RecommendCardDataModel

class RecommendAdapter(private val onRecommendListener: OnRecommendListener) :
    PagingDataAdapter<RecommendCardDataModel, RecommendViewHolder>(RECOMMEND_COMPARATOR) {
    companion object {
        lateinit var defaultUpHeader: Bitmap

        private val RECOMMEND_COMPARATOR = object : DiffUtil.ItemCallback<RecommendCardDataModel>() {
            override fun areItemsTheSame(
                oldItem: RecommendCardDataModel,
                newItem: RecommendCardDataModel
            ): Boolean = oldItem.cId == newItem.cId

            override fun areContentsTheSame(
                oldItem: RecommendCardDataModel,
                newItem: RecommendCardDataModel
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHomeRecommendLayoutBinding.inflate(inflater, parent, false)
        defaultUpHeader = BitmapFactory.decodeResource(parent.resources, R.drawable.noface)
        return RecommendViewHolder(binding, onRecommendListener)
    }

    override fun onBindViewHolder(holder: RecommendViewHolder, position: Int) {
        val recommendCard = getItem(position)
        if (recommendCard != null)
            holder.bind(recommendCard)
    }

    interface OnRecommendListener {
        fun onRecommendClicked(card: RecommendCardDataModel)
    }
}
package xyz.keriteal.bili.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.keriteal.bili.R
import xyz.keriteal.bili.databinding.FragmentHomeBinding
import xyz.keriteal.bili.models.home.RecommendCardDataModel
import xyz.keriteal.bili.ui.GridSpacingItemDecoration
import xyz.keriteal.bili.ui.home.recommend.Injection
import xyz.keriteal.bili.ui.home.recommend.RecommendAdapter
import xyz.keriteal.bili.ui.home.recommend.RecommendLoadStateAdapter
import xyz.keriteal.bili.ui.home.recommend.RecommendViewModel
import xyz.keriteal.bili.ui.player.PlayerActivity

class HomeFragment : Fragment(), RecommendAdapter.OnRecommendListener {
    private var recommendJob: Job? = null
    private var _binding: FragmentHomeBinding? = null
    private lateinit var model: RecommendViewModel
    private lateinit var recyclerViewAdapter: RecommendAdapter
    private val binding get() = _binding!!

    private fun load(offsetIndex: Int) {
        recommendJob?.cancel()
        recommendJob = lifecycleScope.launch {
            model.loadRecommend(offsetIndex).collectLatest {
                recyclerViewAdapter.submitData(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // 创建Recycler 和 Adapter
        val recyclerView = binding.recommendRecyclerView
//        val layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        val layoutManager = GridLayoutManager(this.context, 2)
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_layout_margin)
        recyclerViewAdapter = RecommendAdapter(this)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true, 0))
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter

        model = ViewModelProvider(this, Injection.provideViewModelFactory(this))
            .get(RecommendViewModel::class.java)

        binding.retryButton.setOnClickListener { recyclerViewAdapter.retry() }

        initAdapter()
        load(0)
        return root
    }

    private fun initAdapter() {
        binding.recommendRecyclerView.adapter = recyclerViewAdapter.withLoadStateFooter(
            footer = RecommendLoadStateAdapter { recyclerViewAdapter.retry() }
        )
        recyclerViewAdapter.addLoadStateListener { loadState ->
            Log.d("Adapter", loadState.toString())
            val isListEmpty =
                loadState.refresh is LoadState.NotLoading && recyclerViewAdapter.itemCount == 0
            binding.emptyList.isVisible = isListEmpty
            binding.recommendRecyclerView.isVisible = !isListEmpty

            binding.recommendRecyclerView.isVisible =
                loadState.source.refresh is LoadState.NotLoading
            binding.recommendRecyclerView.isVisible =
                loadState.source.refresh is LoadState.NotLoading
            binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
            binding.retryButton.isVisible = loadState.source.refresh is LoadState.Error

            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let {
                Toast.makeText(this.context, "网络开小差了", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.recommendRecyclerView.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.recommendRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRecommendClicked(card: RecommendCardDataModel) {
        val intent = Intent(binding.root.context, PlayerActivity::class.java)
        intent.putExtra("bvid", card.bvId)
        intent.putExtra("avid", card.avId)
        intent.putExtra("cid", card.cId)
        intent.putExtra("qn", 32)
        startActivity(intent)
    }
}
package it.lismove.app.android.logWall

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import it.lismove.app.android.R
import it.lismove.app.android.databinding.FragmentLogWallBinding
import it.lismove.app.android.general.LisMoveFragment
import it.lismove.app.android.general.lce.LceDispatcher
import it.lismove.app.android.general.lce.LceView
import it.lismove.app.android.logWall.adapter.LogWallAdapter
import org.koin.android.ext.android.inject
import timber.log.Timber


class LogWallFragment : LisMoveFragment(R.layout.fragment_log_wall), LceView<List<String>> {

    private lateinit var binding: FragmentLogWallBinding

    private val logWallAdapter = LogWallAdapter(listOf())
    val viewModel: LogWallViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
           it.title = "Log Wall"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLogWallBinding.bind(view)
        activity?.let { activity ->
            with(binding.logWallRecyclerView){
                val lm = LinearLayoutManager(activity).apply {
                    reverseLayout = true
                    stackFromEnd = true
                }
                layoutManager = lm
                adapter = logWallAdapter
            }

            with(binding.swipeToRefreshLayout){
                setOnRefreshListener {
                    viewModel.reloadLogWallEvents()
                }
            }
            viewModel.stateObservable.observe(activity, LceDispatcher(this))
            viewModel.reloadLogWallEvents()
        }


    }

    override fun onLoading() {
        Timber.d("onLoading")
        with(binding.swipeToRefreshLayout){
            isRefreshing = true
        }
    }

    override fun onSuccess(data: List<String>) {
        with(binding.swipeToRefreshLayout){
            isRefreshing = false
        }
        logWallAdapter.items = data
        logWallAdapter.notifyDataSetChanged()
        binding.logWallRecyclerView.post(Runnable { binding.logWallRecyclerView.scrollToPosition(0)})
    }

    override fun onError(throwable: Throwable) {
        Timber.d("onError ${throwable.localizedMessage}")
        activity?.let { activity ->
            throwable.localizedMessage?.let {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            }
            with(binding.swipeToRefreshLayout){
                isRefreshing = false
            }
        }
    }
}
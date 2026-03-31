package com.iptvplayer.app.ui.channels

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.databinding.ActivityChannelListBinding
import com.iptvplayer.app.ui.player.PlayerActivity
import com.iptvplayer.app.ui.main.MainViewModel

class ChannelListActivity : FragmentActivity() {

    private lateinit var binding: ActivityChannelListBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ChannelGridAdapter
    private var allChannels: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ChannelGridAdapter { channel ->
            PlayerActivity.start(this, channel)
        }
        binding.channelsGrid.apply {
            layoutManager = GridLayoutManager(this@ChannelListActivity, 6)
            this.adapter = this@ChannelListActivity.adapter
        }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterChannels(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterChannels(query: String) {
        val filtered = if (query.isBlank()) {
            allChannels
        } else {
            allChannels.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.group.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        binding.channelCount.text = "${filtered.size} channels"
    }

    private fun observeViewModel() {
        viewModel.channelGroups.observe(this) { groups ->
            allChannels = groups.flatMap { it.channels }
            adapter.submitList(allChannels)
            binding.channelCount.text = "${allChannels.size} channels"
        }
        viewModel.loadChannels()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            binding.searchInput.requestFocus()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

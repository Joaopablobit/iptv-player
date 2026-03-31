package com.iptvplayer.app.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.ViewModelProvider
import com.iptvplayer.app.R
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.ChannelGroup
import com.iptvplayer.app.ui.channels.ChannelCardPresenter
import com.iptvplayer.app.ui.channels.ChannelListActivity
import com.iptvplayer.app.ui.player.PlayerActivity
import com.iptvplayer.app.utils.Constants

class MainFragment : BrowseSupportFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupUI()
        setupEventListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.loadChannels()
    }

    private fun setupUI() {
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireContext(), R.color.brand_color)
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.search_color)

        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }

    private fun setupEventListeners() {
        // Play channel on select
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Channel -> playChannel(item)
                is ActionItem -> handleAction(item)
            }
        }

        // Long press = toggle favorite
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
            if (item is Channel) {
                viewModel.setSelectedChannel(item)
            }
        }

        // Search
        setOnSearchClickedListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.channelGroups.observe(viewLifecycleOwner) { groups ->
            buildRows(groups)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBarManager.apply {
                if (loading) show() else hide()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                showSetupDialog()
            }
        }
    }

    private fun buildRows(groups: List<ChannelGroup>) {
        rowsAdapter.clear()

        // Add action row (Settings, Add Playlist)
        addActionRow()

        // Add channel group rows
        val channelPresenter = ChannelCardPresenter()
        groups.forEach { group ->
            val listRowAdapter = ArrayObjectAdapter(channelPresenter)
            listRowAdapter.addAll(0, group.channels)

            val header = HeaderItem(group.name)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
    }

    private fun addActionRow() {
        val actionPresenter = ActionPresenter()
        val actionAdapter = ArrayObjectAdapter(actionPresenter)
        actionAdapter.add(ActionItem(R.id.action_add_playlist, getString(R.string.add_playlist), R.drawable.ic_add))
        actionAdapter.add(ActionItem(R.id.action_settings, getString(R.string.settings), R.drawable.ic_settings))
        actionAdapter.add(ActionItem(R.id.action_all_channels, getString(R.string.all_channels), R.drawable.ic_tv))

        rowsAdapter.add(ListRow(HeaderItem(getString(R.string.quick_actions)), actionAdapter))
    }

    private fun playChannel(channel: Channel) {
        viewModel.addToRecent(channel)
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(Constants.EXTRA_CHANNEL, channel)
        }
        startActivity(intent)
    }

    private fun handleAction(action: ActionItem) {
        when (action.id) {
            R.id.action_add_playlist -> showAddPlaylistDialog()
            R.id.action_settings -> startActivity(Intent(requireContext(), SettingsActivity::class.java))
            R.id.action_all_channels -> startActivity(Intent(requireContext(), ChannelListActivity::class.java))
        }
    }

    private fun showAddPlaylistDialog() {
        AddPlaylistDialog.newInstance { url, name ->
            viewModel.addPlaylist(url, name)
        }.show(childFragmentManager, "add_playlist")
    }

    private fun showSetupDialog() {
        if (viewModel.getPlaylists().isEmpty()) {
            showAddPlaylistDialog()
        }
    }

    fun onBackPressed(): Boolean {
        // If headers are showing, let system handle back
        return false
    }
}

data class ActionItem(val id: Int, val title: String, val iconRes: Int)

class ActionPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val view = android.widget.ImageCardView(parent.context).apply {
            setMainImageScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE)
            setMainImageDimensions(160, 120)
            isFocusable = true
            isFocusableInTouchMode = true
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val action = item as? ActionItem ?: return
        val cardView = viewHolder.view as android.widget.ImageCardView
        cardView.titleText = action.title
        cardView.setMainImageDrawable(
            androidx.core.content.ContextCompat.getDrawable(cardView.context, action.iconRes)
        )
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}

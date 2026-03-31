package com.iptvplayer.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.ChannelGroup
import com.iptvplayer.app.data.model.Playlist
import com.iptvplayer.app.data.repository.ChannelRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChannelRepository(application)

    private val _channelGroups = MutableLiveData<List<ChannelGroup>>()
    val channelGroups: LiveData<List<ChannelGroup>> = _channelGroups

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedChannel = MutableLiveData<Channel?>()

    fun loadChannels() {
        val lastUrl = repository.getLastUsedPlaylistUrl()
        if (lastUrl != null) {
            loadFromUrl(lastUrl)
        } else {
            val playlists = repository.getPlaylists()
            if (playlists.isNotEmpty()) {
                loadFromUrl(playlists.first().url)
            } else {
                _error.value = "No playlists configured. Please add an M3U playlist."
            }
        }
    }

    fun loadFromUrl(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.saveLastUsedPlaylistUrl(url)
            val result = repository.loadChannelsFromUrl(url)
            result.onSuccess { channels ->
                val groups = repository.getChannelGroups(channels)
                _channelGroups.value = groups
            }
            result.onFailure { e ->
                _error.value = "Failed to load playlist: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun addPlaylist(url: String, name: String) {
        val playlist = Playlist(name = name, url = url)
        repository.savePlaylist(playlist)
        loadFromUrl(url)
    }

    fun getPlaylists(): List<Playlist> = repository.getPlaylists()

    fun addToRecent(channel: Channel) {
        repository.addToRecent(channel)
    }

    fun setSelectedChannel(channel: Channel) {
        _selectedChannel.value = channel
    }

    fun toggleFavorite(channel: Channel) {
        repository.toggleFavorite(channel)
        // Reload to reflect changes
        val lastUrl = repository.getLastUsedPlaylistUrl()
        if (lastUrl != null) loadFromUrl(lastUrl)
    }
}

package com.iptvplayer.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.ChannelGroup
import com.iptvplayer.app.data.model.Playlist
import com.iptvplayer.app.data.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ChannelRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val favoritesKey = "favorites"
    private var cachedChannels: List<Channel> = emptyList()

    // ---------- Playlist Management ----------

    fun getPlaylists(): List<Playlist> {
        val raw = prefs.getStringSet("playlists", emptySet()) ?: emptySet()
        return raw.mapNotNull { entry ->
            val parts = entry.split("|||")
            if (parts.size >= 2) Playlist(name = parts[0], url = parts[1]) else null
        }
    }

    fun savePlaylist(playlist: Playlist) {
        val current = prefs.getStringSet("playlists", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.add("${playlist.name}|||${playlist.url}")
        prefs.edit().putStringSet("playlists", current).apply()
    }

    fun removePlaylist(playlist: Playlist) {
        val current = prefs.getStringSet("playlists", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.removeIf { it.startsWith("${playlist.name}|||") }
        prefs.edit().putStringSet("playlists", current).apply()
    }

    // ---------- Channel Loading ----------

    suspend fun loadChannelsFromUrl(url: String): Result<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val content = fetchUrl(url)
                val channels = M3UParser.parse(content)
                val favorites = getFavoriteIds()
                val channelsWithFavs = channels.map { it.copy(isFavorite = it.id in favorites) }
                cachedChannels = channelsWithFavs
                // Cache the raw M3U content
                prefs.edit().putString("cached_m3u_$url", content).apply()
                Result.success(channelsWithFavs)
            } catch (e: Exception) {
                // Try to load from cache
                val cached = prefs.getString("cached_m3u_$url", null)
                if (cached != null) {
                    val channels = M3UParser.parse(cached)
                    Result.success(channels)
                } else {
                    Result.failure(e)
                }
            }
        }
    }

    suspend fun loadChannelsFromContent(content: String): Result<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val channels = M3UParser.parse(content)
                val favorites = getFavoriteIds()
                val channelsWithFavs = channels.map { it.copy(isFavorite = it.id in favorites) }
                cachedChannels = channelsWithFavs
                Result.success(channelsWithFavs)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getCachedChannels(): List<Channel> = cachedChannels

    fun getChannelGroups(channels: List<Channel>): List<ChannelGroup> {
        return M3UParser.groupChannels(channels)
    }

    // ---------- Favorites ----------

    fun toggleFavorite(channel: Channel): Boolean {
        val favorites = getFavoriteIds().toMutableSet()
        return if (channel.id in favorites) {
            favorites.remove(channel.id)
            false
        } else {
            favorites.add(channel.id)
            true
        }.also {
            prefs.edit().putStringSet(favoritesKey, favorites).apply()
        }
    }

    fun isFavorite(channelId: String): Boolean {
        return channelId in getFavoriteIds()
    }

    private fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet(favoritesKey, emptySet()) ?: emptySet()
    }

    // ---------- Recently Watched ----------

    fun addToRecent(channel: Channel) {
        val recent = getRecentIds().toMutableList()
        recent.remove(channel.id)
        recent.add(0, channel.id)
        // Keep only last 20
        val trimmed = recent.take(20)
        prefs.edit().putString("recent_channels", trimmed.joinToString(",")).apply()
    }

    fun getRecentChannels(allChannels: List<Channel>): List<Channel> {
        val recentIds = getRecentIds()
        return recentIds.mapNotNull { id -> allChannels.find { it.id == id } }
    }

    private fun getRecentIds(): List<String> {
        val raw = prefs.getString("recent_channels", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",")
    }

    // ---------- Network ----------

    private fun fetchUrl(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVPlayer/1.0 (Android TV)")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}: ${response.message}")
            return response.body?.string() ?: throw Exception("Empty response")
        }
    }

    // ---------- Settings ----------

    fun getLastUsedPlaylistUrl(): String? = prefs.getString("last_playlist_url", null)

    fun saveLastUsedPlaylistUrl(url: String) {
        prefs.edit().putString("last_playlist_url", url).apply()
    }
}

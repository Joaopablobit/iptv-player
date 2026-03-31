package com.iptvplayer.app.data.model

import java.io.Serializable

data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logoUrl: String = "",
    val group: String = "General",
    val epgId: String = "",
    val language: String = "",
    val country: String = "",
    val isFavorite: Boolean = false
) : Serializable {
    companion object {
        fun empty() = Channel(
            id = "",
            name = "Unknown Channel",
            url = "",
            group = "General"
        )
    }
}

data class ChannelGroup(
    val name: String,
    val channels: MutableList<Channel> = mutableListOf(),
    val iconUrl: String = ""
) {
    val channelCount: Int get() = channels.size
}

data class Playlist(
    val name: String,
    val url: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

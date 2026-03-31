package com.iptvplayer.app.data.parser

import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.data.model.ChannelGroup
import java.util.UUID

object M3UParser {

    private const val EXTINF = "#EXTINF"
    private const val EXTM3U = "#EXTM3U"

    /**
     * Parse a raw M3U string into a list of Channel objects.
     */
    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith(EXTINF)) {
                val channel = parseExtInf(line)
                // Find the next non-empty, non-comment line as the stream URL
                var j = i + 1
                while (j < lines.size && (lines[j].isBlank() || lines[j].startsWith("#"))) {
                    j++
                }
                if (j < lines.size) {
                    val url = lines[j].trim()
                    if (url.isNotEmpty() && (url.startsWith("http") || url.startsWith("rtmp") || url.startsWith("rtsp"))) {
                        channels.add(channel.copy(url = url))
                    }
                    i = j + 1
                    continue
                }
            }
            i++
        }

        return channels
    }

    /**
     * Groups channels by their 'group-title' attribute.
     */
    fun groupChannels(channels: List<Channel>): List<ChannelGroup> {
        val groupMap = LinkedHashMap<String, ChannelGroup>()

        // Add Favorites group first if any
        val favorites = channels.filter { it.isFavorite }
        if (favorites.isNotEmpty()) {
            groupMap["⭐ Favorites"] = ChannelGroup("⭐ Favorites", favorites.toMutableList())
        }

        // Group all channels
        channels.forEach { channel ->
            val groupName = channel.group.ifBlank { "General" }
            groupMap.getOrPut(groupName) { ChannelGroup(groupName) }.channels.add(channel)
        }

        return groupMap.values.toList()
    }

    private fun parseExtInf(line: String): Channel {
        // #EXTINF:-1 tvg-id="..." tvg-name="..." tvg-logo="..." group-title="...",Channel Name
        val id = extractAttribute(line, "tvg-id") ?: UUID.randomUUID().toString()
        val tvgName = extractAttribute(line, "tvg-name") ?: ""
        val logo = extractAttribute(line, "tvg-logo") ?: ""
        val group = extractAttribute(line, "group-title") ?: "General"
        val language = extractAttribute(line, "tvg-language") ?: ""
        val country = extractAttribute(line, "tvg-country") ?: ""
        val epgId = extractAttribute(line, "tvg-id") ?: ""

        // Channel name is after the last comma
        val name = line.substringAfterLast(",").trim().ifEmpty {
            tvgName.ifEmpty { "Unknown" }
        }

        return Channel(
            id = id,
            name = name,
            url = "",
            logoUrl = logo,
            group = group.ifBlank { "General" },
            epgId = epgId,
            language = language,
            country = country
        )
    }

    private fun extractAttribute(line: String, attr: String): String? {
        // Matches: attr="value" or attr='value'
        val regex = Regex("""$attr=["']([^"']*)["']""", RegexOption.IGNORE_CASE)
        return regex.find(line)?.groupValues?.get(1)?.trim()
    }

    /**
     * Validate that a URL is a likely valid stream URL.
     */
    fun isValidStreamUrl(url: String): Boolean {
        return url.isNotBlank() && (
            url.startsWith("http://") ||
            url.startsWith("https://") ||
            url.startsWith("rtmp://") ||
            url.startsWith("rtsp://")
        )
    }
}

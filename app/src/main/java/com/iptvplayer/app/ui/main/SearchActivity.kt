package com.iptvplayer.app.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.iptvplayer.app.R

class SearchActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Delegates to ChannelListActivity with search focus
        startActivity(android.content.Intent(this, com.iptvplayer.app.ui.channels.ChannelListActivity::class.java))
        finish()
    }
}

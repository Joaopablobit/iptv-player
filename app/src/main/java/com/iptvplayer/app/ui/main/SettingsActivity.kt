package com.iptvplayer.app.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.iptvplayer.app.R
import com.iptvplayer.app.databinding.ActivitySettingsBinding

class SettingsActivity : FragmentActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupUI()
    }

    private fun setupUI() {
        // Display current playlists
        val playlists = viewModel.getPlaylists()
        binding.playlistCount.text = "${playlists.size} playlist(s) configured"

        binding.btnAddPlaylist.setOnClickListener {
            AddPlaylistDialog.newInstance { url, name ->
                viewModel.addPlaylist(url, name)
                binding.playlistCount.text = "${viewModel.getPlaylists().size} playlist(s) configured"
                Toast.makeText(this, "Playlist added!", Toast.LENGTH_SHORT).show()
            }.show(supportFragmentManager, "add")
        }

        binding.btnBack.setOnClickListener { finish() }

        // App version
        binding.appVersion.text = "Version 1.0.0 | IPTV Player for Android TV"
    }
}

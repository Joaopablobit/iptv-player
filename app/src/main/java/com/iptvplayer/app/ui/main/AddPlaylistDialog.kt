package com.iptvplayer.app.ui.main

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.iptvplayer.app.R

class AddPlaylistDialog : DialogFragment() {

    private var onConfirm: ((url: String, name: String) -> Unit)? = null

    companion object {
        fun newInstance(onConfirm: (url: String, name: String) -> Unit): AddPlaylistDialog {
            return AddPlaylistDialog().apply {
                this.onConfirm = onConfirm
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_playlist, null)

        val urlInput = view.findViewById<EditText>(R.id.input_url)
        val nameInput = view.findViewById<EditText>(R.id.input_name)

        return AlertDialog.Builder(requireContext(), R.style.AppTheme_Dialog)
            .setTitle(getString(R.string.add_playlist))
            .setView(view)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val url = urlInput.text.toString().trim()
                val name = nameInput.text.toString().trim().ifEmpty { "My Playlist" }
                if (url.isBlank() || !url.startsWith("http")) {
                    Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm?.invoke(url, name)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }
}

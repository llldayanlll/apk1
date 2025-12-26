package com.example.myapp

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.concurrent.thread
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var pickButton: Button
    private lateinit var sendButton: Button
    private lateinit var linkInput: EditText
    private lateinit var statusText: TextView
    private var selectedUris = mutableListOf<Uri>()

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                selectedUris.addAll(uris)
                statusText.text = "Selected ${selectedUris.size} files"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        linkInput = EditText(this).apply {
            hint = "Enter upload link here"
        }

        pickButton = Button(this).apply {
            text = "PICK MEDIA"
            setOnClickListener { pickMedia.launch("image/* video/*") }
        }

        sendButton = Button(this).apply {
            text = "SEND"
            setOnClickListener { sendFiles() }
        }

        statusText = TextView(this).apply {
            text = "Ready to upload 0 files"
        }

        layout.apply {
            addView(linkInput)
            addView(pickButton)
            addView(sendButton)
            addView(statusText)
        }

        setContentView(layout)
    }

    private fun sendFiles() {
        val link = linkInput.text.toString().trim()
        if (link.isEmpty()) {
            statusText.text = "Error: Upload link is empty"
            return
        }
        if (selectedUris.isEmpty()) {
            statusText.text = "Error: No files selected"
            return
        }

        thread {
            try {
                selectedUris.forEach { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    // replace below with your upload logic to pCloud / Mega
                    // simulate upload
                    Thread.sleep(500)
                    inputStream?.close()
                }

                runOnUiThread {
                    statusText.text = "Upload complete: ${selectedUris.size} files"
                    selectedUris.clear()
                }
            } catch (e: Exception) {
                runOnUiThread { statusText.text = "Upload failed: ${e.message}" }
            }
        }

        // Git push logic
        thread {
            try {
                Runtime.getRuntime().exec(arrayOf("git", "add", ".")).waitFor()
                Runtime.getRuntime().exec(arrayOf("git", "commit", "-m", "\"VERIFIED: upload attempt\"")).waitFor()
                Runtime.getRuntime().exec(arrayOf("git", "push", "origin", "main")).waitFor()
            } catch (e: Exception) {
                runOnUiThread { statusText.append("\nGit push failed: ${e.message}") }
            }
        }
    }
}


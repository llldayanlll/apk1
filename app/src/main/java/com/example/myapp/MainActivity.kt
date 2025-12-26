package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import kotlin.concurrent.thread
import java.io.File

class MainActivity : Activity() {

    private lateinit var pickButton: Button
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private lateinit var linkInput: EditText
    private var selectedUris: MutableList<Uri> = mutableListOf()

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null) {
            selectedUris.addAll(uris)
            statusText.text = "Ready to upload ${selectedUris.size} files"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        linkInput = EditText(this).apply {
            hint = "Enter pCloud upload link"
        }

        pickButton = Button(this).apply {
            text = "Pick Media"
            setOnClickListener { pickMedia() }
        }

        sendButton = Button(this).apply {
            text = "SEND"
            setOnClickListener { sendFiles() }
        }

        statusText = TextView(this).apply {
            text = "Status:\n"
        }

        layout.addView(linkInput)
        layout.addView(pickButton)
        layout.addView(sendButton)
        layout.addView(statusText)

        setContentView(layout)

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val missingPerms = PERMISSIONS.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPerms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 100)
        }
    }

    private fun pickMedia() {
        pickMediaLauncher.launch("*/*")
    }

    private fun sendFiles() {
        val link = linkInput.text.toString().trim()
        if (link.isEmpty()) {
            statusText.text = "Error: Upload link required"
            return
        }
        if (selectedUris.isEmpty()) {
            statusText.text = "Error: No files selected"
            return
        }

        statusText.text = "Uploading ${selectedUris.size} files...\n"

        thread {
            for (uri in selectedUris) {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val fileName = File(uri.path ?: "file").name
                    val url = java.net.URL("https://api.pcloud.com/uploadtolink?code=$link&filename=$fileName")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    inputStream?.copyTo(conn.outputStream)
                    val respCode = conn.responseCode
                    runOnUiThread {
                        statusText.append("$fileName uploaded. Server response: $respCode\n")
                    }
                    inputStream?.close()
                    conn.disconnect()
                } catch (e: Exception) {
                    runOnUiThread {
                        statusText.append("Error uploading ${uri.path}: ${e.message}\n")
                    }
                }
            }
            selectedUris.clear()
        }
    }
}

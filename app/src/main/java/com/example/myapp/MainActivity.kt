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
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var linkInput: EditText
    private lateinit var pickButton: Button
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private var selectedUris: MutableList<Uri> = mutableListOf()
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )

    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris != null) {
            selectedUris = uris.toMutableList()
            statusText.text = "Selected files:\n" + selectedUris.joinToString("\n") { it.path ?: it.toString() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        linkInput = EditText(this).apply { hint = "Enter your upload link here" }
        pickButton = Button(this).apply { text = "PICK MEDIA" }
        sendButton = Button(this).apply { text = "SEND" }
        statusText = TextView(this).apply { text = "Status:\n" }

        layout.addView(linkInput)
        layout.addView(pickButton)
        layout.addView(sendButton)
        layout.addView(statusText)
        setContentView(layout)

        checkPermissions()

        pickButton.setOnClickListener {
            pickFilesLauncher.launch("*/*") // allow all file types
        }

        sendButton.setOnClickListener {
            val uploadUrl = linkInput.text.toString().trim()
            if (uploadUrl.isEmpty()) {
                statusText.append("\nError: Upload link empty")
                return@setOnClickListener
            }
            if (selectedUris.isEmpty()) {
                statusText.append("\nError: No file selected")
                return@setOnClickListener
            }

            selectedUris.forEach { uri ->
                statusText.append("\nUploading ${uri.lastPathSegment}...")
                thread {
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val url = URL(uploadUrl)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.doOutput = true
                        inputStream?.use { inp ->
                            conn.outputStream.use { out ->
                                inp.copyTo(out)
                            }
                        }
                        val response = conn.inputStream.bufferedReader().readText()
                        runOnUiThread { statusText.append("\nUploaded ${uri.lastPathSegment}: $response") }
                    } catch (e: Exception) {
                        runOnUiThread { statusText.append("\nError uploading ${uri.lastPathSegment}: ${e.message}") }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        statusText.append("\nPermissions granted. Ready to pick files.")
    }
}

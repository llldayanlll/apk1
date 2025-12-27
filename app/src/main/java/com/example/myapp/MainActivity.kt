package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.net.Uri
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.concurrent.thread
import okhttp3.*
import java.io.InputStream

class MainActivity : Activity() {

    private lateinit var uploadUrlInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var selectButton: Button
    private lateinit var sendButton: Button

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private var selectedUris: List<Uri> = listOf()

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNotEmpty()) {
                selectedUris = uris
                appendOutput("Selected ${uris.size} file(s)\n")
            } else {
                appendOutput("No files selected\n")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkAndRequestPermissions()
    }

    private fun createUI() {

        val title = TextView(this).apply {
            text = "Koofr Uploader"
            textSize = 22f
        }

        uploadUrlInput = EditText(this).apply {
            hint = "Paste Koofr upload link code here"
            setPadding(20, 20, 20, 20)
        }

        selectButton = Button(this).apply {
            text = "SELECT FILES"
            setOnClickListener {
                pickMedia.launch("*/*")
            }
        }

        sendButton = Button(this).apply {
            text = "SEND FILES"
            setOnClickListener {
                if (selectedUris.isNotEmpty()) {
                    uploadFiles(selectedUris)
                } else {
                    appendOutput("No files selected\n")
                }
            }
        }

        outputText = TextView(this).apply {
            text = "=== Upload Log ===\n\n"
            setPadding(20, 20, 20, 20)
        }

        scrollView = ScrollView(this).apply {
            addView(outputText)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
            addView(title)
            addView(uploadUrlInput)
            addView(selectButton)
            addView(sendButton)
            addView(scrollView)
        }

        setContentView(layout)
    }

    private fun checkAndRequestPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }

    private fun uploadFiles(uris: List<Uri>) {
        val uploadCode = uploadUrlInput.text.toString().trim()

        if (uploadCode.isEmpty()) {
            appendOutput("ERROR: Upload link code missing\n")
            return
        }

        thread {
            val client = OkHttpClient()

            for (uri in uris) {
                try {
                    runOnUiThread {
                        appendOutput("Uploading: $uri\n")
                    }

                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val fileName = uri.lastPathSegment ?: "file"

                    val requestBody = inputStream?.readBytes()?.let { bytes ->
                        RequestBody.create(MediaType.parse("application/octet-stream"), bytes)
                    } ?: continue

                    val request = Request.Builder()
                        .url("https://api.koofr.net/v1/uploadlink/$uploadCode")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw Exception("HTTP ${response.code()}")
                    }

                    runOnUiThread {
                        appendOutput("SUCCESS: $fileName\n")
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        appendOutput("FAILED: ${e.message}\n")
                    }
                }
            }
        }
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}

package com.example.myapp

import android.os.Bundle
import android.widget.*
import android.net.Uri
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import okhttp3.*
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var uploadUrlInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var pickButton: Button
    private lateinit var sendButton: Button
    private var selectedUris: List<Uri> = emptyList()

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val PICK_FILES_REQUEST = 101

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
            hint = "Paste Koofr upload link here"
            setPadding(20, 20, 20, 20)
        }

        pickButton = Button(this).apply {
            text = "SELECT FILES"
            setOnClickListener { pickFiles() }
        }

        sendButton = Button(this).apply {
            text = "SEND FILES"
            setOnClickListener {
                if (selectedUris.isNotEmpty()) {
                    uploadFiles(selectedUris)
                } else {
                    appendOutput("No files selected to send\n")
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
            addView(pickButton)
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

    private fun pickFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select files"), PICK_FILES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILES_REQUEST && resultCode == RESULT_OK) {
            val uris = mutableListOf<Uri>()
            data?.data?.let { uris.add(it) }
            data?.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            }
            if (uris.isNotEmpty()) {
                selectedUris = uris
                appendOutput("Selected ${uris.size} files\n")
            } else {
                appendOutput("No files selected\n")
            }
        }
    }

    private fun uploadFiles(uris: List<Uri>) {
        val uploadUrl = uploadUrlInput.text.toString().trim()
        if (uploadUrl.isEmpty()) {
            appendOutput("ERROR: Upload link missing\n")
            return
        }

        thread {
            val client = OkHttpClient()
            for (uri in uris) {
                try {
                    runOnUiThread { appendOutput("Uploading: $uri\n") }

                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val fileName = uri.lastPathSegment ?: "upload_file"

                    val requestBody = inputStream?.let { stream ->
                        object : RequestBody() {
                            override fun contentType() = MediaType.parse("application/octet-stream")
                            override fun writeTo(sink: okio.BufferedSink) {
                                stream.source().use { source -> sink.writeAll(source) }
                            }
                        }
                    } ?: continue

                    val request = Request.Builder()
                        .url(uploadUrl)
                        .post(MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", fileName, requestBody)
                            .build())
                        .build()

                    val response = client.newCall(request).execute()
                    if (!response.isSuccessful) throw Exception("HTTP ${response.code()}")
                    runOnUiThread { appendOutput("SUCCESS: $uri\n") }

                } catch (e: Exception) {
                    runOnUiThread { appendOutput("FAILED: ${e.message}\n") }
                }
            }
        }
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}

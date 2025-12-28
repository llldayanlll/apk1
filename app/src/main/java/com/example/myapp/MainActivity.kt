package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.net.Uri
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread
import java.net.HttpURLConnection
import java.net.URL
import java.io.DataOutputStream

class MainActivity : Activity() {

    private lateinit var uploadUrlInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val PICK_FILES_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkAndRequestPermissions()
    }

    private fun createUI() {

        val title = TextView(this).apply {
            text = "pCloud Uploader"
            textSize = 22f
        }

        uploadUrlInput = EditText(this).apply {
            hint = "Paste pCloud upload link here"
            setPadding(20, 20, 20, 20)
        }

        val pickButton = Button(this).apply {
            text = "SELECT FILES"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(intent, PICK_FILES_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILES_CODE && resultCode == RESULT_OK && data != null) {
            val uris = mutableListOf<Uri>()

            data.clipData?.let { clip ->
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            } ?: data.data?.let {
                uris.add(it)
            }

            if (uris.isNotEmpty()) {
                uploadFiles(uris)
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
            for (uri in uris) {
                try {
                    runOnUiThread {
                        appendOutput("Uploading: $uri\n")
                    }

                    uploadSingleFile(uploadUrl, uri)

                    runOnUiThread {
                        appendOutput("SUCCESS: $uri\n")
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        appendOutput("FAILED: ${e.message}\n")
                    }
                }
            }
        }
    }

    private fun uploadSingleFile(uploadUrl: String, uri: Uri) {

        val boundary = "----AndroidBoundary${System.currentTimeMillis()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val url = URL(uploadUrl)
        val connection = url.openConnection() as HttpURLConnection

        connection.apply {
            doOutput = true
            doInput = true
            requestMethod = "POST"
            setRequestProperty(
                "Content-Type",
                "multipart/form-data; boundary=$boundary"
            )
        }

        val outputStream = DataOutputStream(connection.outputStream)

        val fileName = uri.lastPathSegment ?: "upload_file"
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        outputStream.writeBytes(twoHyphens + boundary + lineEnd)
        outputStream.writeBytes(
            "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineEnd"
        )
        outputStream.writeBytes("Content-Type: $mimeType$lineEnd")
        outputStream.writeBytes(lineEnd)

        contentResolver.openInputStream(uri)?.use { input ->
            input.copyTo(outputStream)
        }

        outputStream.writeBytes(lineEnd)
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
        outputStream.flush()
        outputStream.close()

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw Exception("HTTP $responseCode")
        }

        connection.disconnect()
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}

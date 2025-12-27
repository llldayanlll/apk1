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
    private var selectedUris: List<Uri> = emptyList()

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkAndRequestPermissions()
    }

    private fun createUI() {

        val title = TextView(this).apply {
            text = "pCloud Form Upload (Test)"
            textSize = 22f
        }

        uploadUrlInput = EditText(this).apply {
            hint = "Paste pCloud upload link here"
        }

        val pickButton = Button(this).apply {
            text = "SELECT FILES"
            setOnClickListener { pickFiles() }
        }

        val sendButton = Button(this).apply {
            text = "SEND"
            setOnClickListener { uploadFiles() }
        }

        outputText = TextView(this).apply {
            text = "=== Upload Log ===\n\n"
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

    private fun pickFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK) {
            val list = mutableListOf<Uri>()

            data?.clipData?.let {
                for (i in 0 until it.itemCount) {
                    list.add(it.getItemAt(i).uri)
                }
            } ?: data?.data?.let {
                list.add(it)
            }

            selectedUris = list
            appendOutput("Selected ${list.size} file(s)\n")
        }
    }

    private fun uploadFiles() {
        val uploadUrl = uploadUrlInput.text.toString().trim()
        if (uploadUrl.isEmpty() || selectedUris.isEmpty()) {
            appendOutput("ERROR: Missing link or files\n")
            return
        }

        thread {
            for (uri in selectedUris) {
                try {
                    runOnUiThread { appendOutput("Uploading: $uri\n") }
                    uploadFormStyle(uploadUrl, uri)
                    runOnUiThread { appendOutput("DONE: $uri\n") }
                } catch (e: Exception) {
                    runOnUiThread { appendOutput("FAILED: ${e.message}\n") }
                }
            }
        }
    }

    private fun uploadFormStyle(uploadUrl: String, uri: Uri) {

        val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            doOutput = true
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        val out = DataOutputStream(conn.outputStream)

        val fileName = uri.lastPathSegment ?: "file"
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"

        out.writeBytes(twoHyphens + boundary + lineEnd)
        out.writeBytes(
            "Content-Disposition: form-data; name=\"file[]\"; filename=\"$fileName\"$lineEnd"
        )
        out.writeBytes("Content-Type: $mime$lineEnd$lineEnd")

        contentResolver.openInputStream(uri)!!.copyTo(out)

        out.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd)
        out.flush()
        out.close()

        if (conn.responseCode !in 200..399) {
            throw Exception("HTTP ${conn.responseCode}")
        }

        conn.disconnect()
    }

    private fun checkAndRequestPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}

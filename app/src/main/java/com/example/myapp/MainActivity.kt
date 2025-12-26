package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import kotlin.concurrent.thread
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var pickButton: Button
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private var selectedFile: File? = null
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        pickButton = Button(this).apply { text = "PICK MEDIA" }
        sendButton = Button(this).apply { text = "SEND" }
        statusText = TextView(this).apply { text = "Status:\n" }

        layout.addView(pickButton)
        layout.addView(sendButton)
        layout.addView(statusText)

        setContentView(layout)

        checkPermissions()

        pickButton.setOnClickListener {
            // minimal: simulate selecting a file
            selectedFile = File("/sdcard/Download/example.jpg") // replace with real file picking
            statusText.text = "Selected file: ${selectedFile?.name}"
        }

        sendButton.setOnClickListener {
            selectedFile?.let { file ->
                statusText.append("\nUploading ${file.name}...")
                thread {
                    try {
                        val url = URL("YOUR_UPLOAD_URL_HERE") // replace with actual URL
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.doOutput = true
                        conn.outputStream.use { out ->
                            out.write(file.readBytes())
                        }
                        val response = conn.inputStream.bufferedReader().readText()
                        runOnUiThread { statusText.append("\nUpload finished: $response") }
                    } catch (e: Exception) {
                        runOnUiThread { statusText.append("\nError: ${e.message}") }
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

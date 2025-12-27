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
import java.net.HttpURLConnection
import java.net.URL
import java.io.DataOutputStream
import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.X509Certificate

class MainActivity : Activity() {

    private lateinit var uploadUrlInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                uploadFiles(uris)
            } else {
                appendOutput("No files selected\n")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trustAllCertificates()  // Disable SSL check (Android 6 fix)

        createUI()
        checkAndRequestPermissions()
    }

    private fun trustAllCertificates() {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
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

        val pickButton = Button(this).apply {
            text = "SELECT FILES"
            setOnClickListener {
                pickMedia.launch("*/*")
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


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
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {

    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView

    private val PICK_FILES_CODE = 101
    private val PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    // ==== FILELU API KEY ====
    private val API_KEY = "443198khiq1nlo42j8uqh"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkPermissions()
    }

    private fun createUI() {
        val title = TextView(this).apply {
            text = "Auto FileLu Uploader"
            textSize = 22f
        }

        val pickButton = Button(this).apply {
            text = "SELECT & SEND"
            setOnClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                startActivityForResult(i, PICK_FILES_CODE)
            }
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
            addView(pickButton)
            addView(scrollView)
        }

        setContentView(layout)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0])
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 100)
        }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)

        if (req == PICK_FILES_CODE && res == RESULT_OK && data != null) {
            val uris = mutableListOf<Uri>()

            data.clipData?.let {
                for (i in 0 until it.itemCount) {
                    uris.add(it.getItemAt(i).uri)
                }
            } ?: data.data?.let { uris.add(it) }

            if (uris.isNotEmpty()) uploadFiles(uris)
        }
    }

    private fun uploadFiles(uris: List<Uri>) {
        thread {
            for (uri in uris) {
                try {
                    log("Preparing upload: $uri")

                    val (uploadUrl, sessId) = getUploadServer()
                    uploadFile(uploadUrl, sessId, uri)

                    log("SUCCESS")
                } catch (e: Exception) {
                    log("FAILED: ${e.message}")
                }
            }
        }
    }

    // STEP 1 — get upload server + sess_id
    private fun getUploadServer(): Pair<String, String> {
        val url = URL("https://filelu.com/api/upload/server?key=$API_KEY")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()
        conn.disconnect()

        val uploadUrl =
            Regex("\"result\":\"([^\"]+)\"").find(response)!!.groupValues[1]
        val sessId =
            Regex("\"sess_id\":\"([^\"]+)\"").find(response)!!.groupValues[1]

        return Pair(uploadUrl, sessId)
    }

    // STEP 2 — upload file
    private fun uploadFile(uploadUrl: String, sessId: String, uri: Uri) {

        val boundary = "----Android${System.currentTimeMillis()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val conn = (URL(uploadUrl).openConnection() as HttpURLConnection).apply {
            doOutput = true
            requestMethod = "POST"
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        val out = DataOutputStream(conn.outputStream)

        fun field(name: String, value: String) {
            out.writeBytes(twoHyphens + boundary + lineEnd)
            out.writeBytes("Content-Disposition: form-data; name=\"$name\"$lineEnd$lineEnd")
            out.writeBytes(value + lineEnd)
        }

        field("sess_id", sessId)
        field("utype", "prem")

        val name = uri.lastPathSegment ?: "file"
        val type = contentResolver.getType(uri) ?: "application/octet-stream"

        out.writeBytes(twoHyphens + boundary + lineEnd)
        out.writeBytes(
            "Content-Disposition: form-data; name=\"file\"; filename=\"$name\"$lineEnd"
        )
        out.writeBytes("Content-Type: $type$lineEnd$lineEnd")

        contentResolver.openInputStream(uri)!!.copyTo(out)

        out.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd)
        out.flush()
        out.close()

        if (conn.responseCode !in 200..299) {
            throw Exception("HTTP ${conn.responseCode}")
        }

        conn.disconnect()
    }

    private fun log(msg: String) {
        runOnUiThread {
            outputText.append(msg + "\n")
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }
}

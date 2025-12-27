package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var uploadUrlInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView
    private val PICK_REQUEST = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
    }

    private fun createUI() {
        val title = TextView(this).apply { text = "Uploader"; textSize = 22f }
        uploadUrlInput = EditText(this).apply { hint = "Paste upload link here"; setPadding(20,20,20,20) }
        val pickButton = Button(this).apply { 
            text = "SELECT FILES"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(intent, PICK_REQUEST)
            }
        }
        outputText = TextView(this).apply { text = "=== Upload Log ===\n\n"; setPadding(20,20,20,20) }
        scrollView = ScrollView(this).apply { addView(outputText) }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40,60,40,60)
            addView(title); addView(uploadUrlInput); addView(pickButton); addView(scrollView)
        }
        setContentView(layout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_REQUEST && resultCode == RESULT_OK) {
            val uris = mutableListOf<Uri>()
            data?.data?.let { uris.add(it) }
            data?.clipData?.let { cd -> for (i in 0 until cd.itemCount) uris.add(cd.getItemAt(i).uri) }
            if (uris.isNotEmpty()) uploadFiles(uris)
            else appendOutput("No files selected\n")
        }
    }

    private fun uploadFiles(uris: List<Uri>) {
        val uploadUrl = uploadUrlInput.text.toString().trim()
        if (uploadUrl.isEmpty()) { appendOutput("ERROR: Upload link missing\n"); return }
        thread {
            for (uri in uris) {
                try {
                    runOnUiThread { appendOutput("Uploading: $uri\n") }
                    uploadSingleFile(uploadUrl, uri)
                    runOnUiThread { appendOutput("SUCCESS: $uri\n") }
                } catch (e: Exception) {
                    runOnUiThread { appendOutput("FAILED: ${e.message}\n") }
                }
            }
        }
    }

    private fun uploadSingleFile(uploadUrl: String, uri: Uri) {
        val boundary = "----Boundary${System.currentTimeMillis()}"
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val url = URL(uploadUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.doOutput = true; conn.doInput = true; conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        val out = DataOutputStream(conn.outputStream)
        val fileName = uri.lastPathSegment ?: "upload_file"
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        out.writeBytes(twoHyphens + boundary + lineEnd)
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineEnd")
        out.writeBytes("Content-Type: $mimeType$lineEnd$lineEnd")
        contentResolver.openInputStream(uri)?.use { it.copyTo(out) }
        out.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd)
        out.flush(); out.close()

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) throw Exception("HTTP $responseCode")
        conn.disconnect()
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}

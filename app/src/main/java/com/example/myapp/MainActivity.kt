package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import kotlinx.coroutines.*
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var ipInput: EditText
    private lateinit var fileUri: Uri
    private lateinit var statusBox: TextView

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            fileUri = uri
            statusBox.append("Selected file: ${getFileName(uri)}\n")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ipInput = EditText(this).apply { hint = "Enter public IP" }
        val pickButton = Button(this).apply { text = "Pick Media"; setOnClickListener { pickFile.launch("*/*") } }
        val sendButton = Button(this).apply { text = "Send"; setOnClickListener { uploadFile() } }
        statusBox = TextView(this).apply { setPadding(20) }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40)
            addView(TextView(this@MainActivity).apply { text = "Public IP:" })
            addView(ipInput)
            addView(pickButton)
            addView(sendButton)
            addView(TextView(this@MainActivity).apply { text = "\nStatus:" })
            addView(statusBox)
        }

        setContentView(layout)
    }

    private fun uploadFile() {
        if (!::fileUri.isInitialized) {
            statusBox.append("No file selected\n")
            return
        }
        val ip = ipInput.text.toString().trim()
        if (ip.isEmpty()) {
            statusBox.append("Enter IP\n")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://$ip:8080/upload")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****")

                val out = conn.outputStream.bufferedWriter()
                val inputStream = contentResolver.openInputStream(fileUri)
                val fileBytes = inputStream!!.readBytes()
                out.write("--*****\r\nContent-Disposition: form-data; name=\"file\"; filename=\"${getFileName(fileUri)}\"\r\n\r\n")
                out.flush()
                conn.outputStream.write(fileBytes)
                conn.outputStream.flush()
                out.write("\r\n--*****--\r\n")
                out.flush()
                conn.outputStream.close()

                val response = conn.inputStream.bufferedReader().readText()
                withContext(Dispatchers.Main) {
                    statusBox.append("Server response: $response\n")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusBox.append("Error: ${e.message}\n")
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = cursor.getString(index)
            }
        }
        return name
    }
}

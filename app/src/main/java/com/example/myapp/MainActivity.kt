package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.core.view.setPadding
import kotlinx.coroutines.*
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    private lateinit var ipInput: EditText
    private var fileUri: Uri? = null
    private lateinit var statusBox: TextView

    private val PICK_FILE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ipInput = EditText(this).apply { hint = "Enter public IP" }
        val pickButton = Button(this).apply { 
            text = "Pick Media"
            setOnClickListener { pickFile() }
        }
        val sendButton = Button(this).apply { 
            text = "Send"
            setOnClickListener { uploadFile() }
        }
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

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            fileUri = data?.data
            fileUri?.let { statusBox.append("Selected file: ${getFileName(it)}\n") }
        }
    }

    private fun uploadFile() {
        val uri = fileUri
        if (uri == null) {
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

                val output = conn.outputStream
                val inputStream = contentResolver.openInputStream(uri)
                val fileBytes = inputStream!!.readBytes()
                val writer = OutputStreamWriter(output)

                writer.write("--*****\r\nContent-Disposition: form-data; name=\"file\"; filename=\"${getFileName(uri)}\"\r\n\r\n")
                writer.flush()
                output.write(fileBytes)
                output.flush()
                writer.write("\r\n--*****--\r\n")
                writer.flush()
                output.close()

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

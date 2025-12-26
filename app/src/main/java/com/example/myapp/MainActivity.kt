package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import android.provider.DocumentsContract
import android.provider.MediaStore

class MainActivity : Activity() {

    private lateinit var emailInput: EditText
    private lateinit var passInput: EditText
    private lateinit var statusBox: TextView
    private var selectedUris: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }

        emailInput = EditText(this).apply {
            hint = "pCloud Email"
        }

        passInput = EditText(this).apply {
            hint = "pCloud Password"
        }

        val pickBtn = Button(this).apply {
            text = "PICK MEDIA"
            setOnClickListener { pickMedia() }
        }

        val sendBtn = Button(this).apply {
            text = "SEND"
            setOnClickListener { sendFiles() }
        }

        statusBox = TextView(this).apply {
            text = "Status:\n"
        }

        layout.addView(emailInput)
        layout.addView(passInput)
        layout.addView(pickBtn)
        layout.addView(sendBtn)
        layout.addView(statusBox)

        setContentView(layout)
    }

    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            selectedUris.clear()

            data?.clipData?.let {
                for (i in 0 until it.itemCount) {
                    selectedUris.add(it.getItemAt(i).uri)
                }
            } ?: data?.data?.let {
                selectedUris.add(it)
            }

            statusBox.append("Selected files: ${selectedUris.size}\n")
        }
    }

    private fun sendFiles() {
        if (selectedUris.isEmpty()) {
            statusBox.append("No files selected\n")
            return
        }
        statusBox.append("Send pressed. Ready to upload ${selectedUris.size} files.\n")
        // pCloud upload logic hooks HERE (next step)
    }
}

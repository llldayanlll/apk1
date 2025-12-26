package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.content.Intent
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var log: TextView
    private var authToken: String? = null
    private val PICK = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = EditText(this).apply { hint = "pCloud Email" }
        val pass = EditText(this).apply {
            hint = "Password"
            inputType = 129
        }

        val login = Button(this).apply {
            text = "LOGIN"
            setOnClickListener {
                thread {
                    val url = URL("https://api.pcloud.com/login?getauth=1&username=${email.text}&password=${pass.text}")
                    val res = url.readText()
                    val json = JSONObject(res)
                    if (json.getInt("result") == 0) {
                        authToken = json.getString("auth")
                        runOnUiThread { log.append("Login OK\n") }
                    } else {
                        runOnUiThread { log.append("Login failed\n") }
                    }
                }
            }
        }

        val pick = Button(this).apply {
            text = "PICK MEDIA"
            setOnClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
                i.type = "*/*"
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(i, PICK)
            }
        }

        log = TextView(this)

        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(email)
            addView(pass)
            addView(login)
            addView(pick)
            addView(log)
        }

        setContentView(l)
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK && res == RESULT_OK && authToken != null) {
            val uris = mutableListOf<Uri>()
            data?.clipData?.let {
                for (i in 0 until it.itemCount) uris.add(it.getItemAt(i).uri)
            } ?: data?.data?.let { uris.add(it) }

            uris.forEach { upload(it) }
        }
    }

    private fun upload(uri: Uri) {
        thread {
            try {
                val stream = contentResolver.openInputStream(uri)!!
                val url = URL("https://api.pcloud.com/uploadfile?auth=$authToken")
                val conn = url.openConnection() as HttpURLConnection
                conn.doOutput = true
                conn.requestMethod = "POST"
                stream.copyTo(conn.outputStream)
                runOnUiThread { log.append("Uploaded\n") }
            } catch (e: Exception) {
                runOnUiThread { log.append("Error\n") }
            }
        }
    }
}

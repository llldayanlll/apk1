package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var commandInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var homeDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeDir = File(filesDir, "home")
        if (!homeDir.exists()) {
            homeDir.mkdirs()
        }

        createUI()
        checkAndRequestPermissions()
        appendOutput("HOME = ${homeDir.absolutePath}\n")
    }

    private fun createUI() {
        val title = TextView(this).apply {
            text = "Android Terminal"
            textSize = 22f
        }

        commandInput = EditText(this).apply {
            hint = "ls, pwd, mkdir, touch, echo hi > a"
        }

        val runBtn = Button(this).apply {
            text = "EXECUTE"
            setOnClickListener { executeCommand() }
        }

        outputText = TextView(this).apply {
            text = "=== READY ===\n"
        }

        scrollView = ScrollView(this).apply {
            addView(outputText)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 40)
            addView(title)
            addView(commandInput)
            addView(runBtn)
            addView(scrollView)
        }

        setContentView(layout)
    }

    private fun checkAndRequestPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        }
    }

    private fun executeCommand() {
        val cmd = commandInput.text.toString().trim()
        if (cmd.isEmpty()) return

        appendOutput("\n$ $cmd\n")

        thread {
            try {
                val pb = ProcessBuilder("sh", "-c", cmd)
                pb.directory(homeDir)
                pb.redirectErrorStream(true)

                val proc = pb.start()
                val out = proc.inputStream.bufferedReader().readText()
                proc.waitFor()

                runOnUiThread {
                    appendOutput(out)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    appendOutput("ERROR: ${e.message}\n")
                }
            }
        }

        commandInput.text.clear()
    }

    private fun appendOutput(text: String) {
        outputText.append(text)
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}

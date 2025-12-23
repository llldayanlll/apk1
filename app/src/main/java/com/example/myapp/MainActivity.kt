package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.os.Environment
import android.content.Intent
import android.provider.Settings
import java.io.*
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var scroll: ScrollView

    private lateinit var shell: Process
    private lateinit var shellIn: BufferedWriter
    private lateinit var shellOut: BufferedReader

    private lateinit var currentDir: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create app home
        val home = File(filesDir, "home")
        if (!home.exists()) home.mkdirs()
        currentDir = home

        requestAllFilesAccess()
        startShell(currentDir)
        buildUI()

        append("HOME = ${home.absolutePath}\n")
    }

    private fun requestAllFilesAccess() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
            append("Please grant All Files Access for full storage browsing.\n")
        }
    }

    private fun startShell(dir: File) {
        val pb = ProcessBuilder("sh")
        pb.directory(dir)
        pb.redirectErrorStream(true)

        shell = pb.start()
        shellIn = BufferedWriter(OutputStreamWriter(shell.outputStream))
        shellOut = BufferedReader(InputStreamReader(shell.inputStream))

        thread {
            var line: String?
            while (shellOut.readLine().also { line = it } != null) {
                runOnUiThread {
                    append(line!! + "\n")
                }
            }
        }
    }

    private fun buildUI() {
        input = EditText(this)

        val runBtn = Button(this).apply { text = "EXECUTE"; setOnClickListener { sendCommand() } }
        val homeBtn = Button(this).apply { text = "HOME"; setOnClickListener { goHome() } }
        val storageBtn = Button(this).apply { text = "STORAGE"; setOnClickListener { goStorage() } }

        output = TextView(this).apply { text = "=== TERMINAL READY ===\n" }
        scroll = ScrollView(this).apply { addView(output) }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 40)
            addView(input)
            addView(runBtn)
            addView(homeBtn)
            addView(storageBtn)
            addView(scroll)
        }

        setContentView(layout)
    }

    private fun sendCommand() {
        val cmd = input.text.toString()
        if (cmd.isBlank()) return

        append("\n$ $cmd\n")
        thread { shellIn.write(cmd); shellIn.newLine(); shellIn.flush() }
        input.text.clear()
    }

    private fun goHome() {
        val homeDir = File(filesDir, "home")
        append("\n$ cd ${homeDir.absolutePath}\n")
        shellIn.write("cd ${homeDir.absolutePath}")
        shellIn.newLine()
        shellIn.flush()
        currentDir = homeDir
    }

    private fun goStorage() {
        val storageDir = File("/storage/emulated/0")
        append("\n$ cd /storage/emulated/0\n")
        shellIn.write("cd /storage/emulated/0")
        shellIn.newLine()
        shellIn.flush()
        currentDir = storageDir
    }

    private fun append(t: String) {
        output.append(t)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun onDestroy() {
        super.onDestroy()
        shell.destroy()
    }
}

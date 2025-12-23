package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.*
import java.io.*
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var scroll: ScrollView

    private lateinit var shell: Process
    private lateinit var shellIn: BufferedWriter
    private lateinit var shellOut: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val home = File(filesDir, "home")
        if (!home.exists()) home.mkdirs()

        startShell(home)
        buildUI()

        append("HOME = ${home.absolutePath}\n")
    }

    private fun startShell(home: File) {
        val pb = ProcessBuilder("sh")
        pb.directory(home)
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
        val run = Button(this).apply {
            text = "EXECUTE"
            setOnClickListener { sendCommand() }
        }

        output = TextView(this).apply {
            text = "=== TERMINAL READY ===\n"
        }

        scroll = ScrollView(this).apply {
            addView(output)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 40)
            addView(input)
            addView(run)
            addView(scroll)
        }

        setContentView(layout)
    }

    private fun sendCommand() {
        val cmd = input.text.toString()
        if (cmd.isBlank()) return

        append("\n$ $cmd\n")

        thread {
            shellIn.write(cmd)
            shellIn.newLine()
            shellIn.flush()
        }

        input.text.clear()
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

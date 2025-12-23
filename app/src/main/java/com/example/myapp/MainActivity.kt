package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Environment
import kotlin.concurrent.thread

class MainActivity : Activity() {
    
    private lateinit var commandInput: EditText
    private lateinit var outputText: TextView
    private lateinit var scrollView: ScrollView
    
    // Permissions needed
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        checkAndRequestPermissions()
    }
    
    private fun createUI() {
        // Title
        val title = TextView(this).apply {
            text = "Android Terminal"
            textSize = 24f
        }
        
        // Command input
        commandInput = EditText(this).apply {
            hint = "Enter command (ls, pwd, echo, etc.)"
            setPadding(20, 20, 20, 20)
        }
        
        // Execute button
        val executeButton = Button(this).apply {
            text = "EXECUTE"
            setOnClickListener { executeCommand() }
            setPadding(30, 15, 30, 15)
        }
        
        // Permission button
        val permButton = Button(this).apply {
            text = "REQUEST PERMISSIONS"
            setOnClickListener { checkAndRequestPermissions() }
            setPadding(20, 10, 20, 10)
        }
        
        // Output area
        outputText = TextView(this).apply {
            text = "=== Android Terminal ===\n\n"
            textSize = 14f
            setPadding(20, 20, 20, 20)
        }
        
        scrollView = ScrollView(this).apply {
            addView(outputText)
        }
        
        // Layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
            
            addView(title)
            addView(TextView(this@MainActivity).apply {
                text = "Command:"
                setPadding(0, 20, 0, 10)
            })
            addView(commandInput)
            addView(executeButton)
            addView(permButton)
            addView(TextView(this@MainActivity).apply {
                text = "\nOutput:"
                setPadding(0, 30, 0, 10)
            })
            addView(scrollView)
        }
        
        setContentView(layout)
    }
    
    private fun checkAndRequestPermissions() {
        val missingPerms = PERMISSIONS.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPerms.isNotEmpty()) {
            appendOutput("Requesting permissions: ${missingPerms.joinToString()}\n")
            ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 100)
        } else {
            appendOutput("All permissions granted!\n")
            showStorageInfo()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            appendOutput("Permissions granted. Try commands now.\n")
            showStorageInfo()
        }
    }
    
    private fun showStorageInfo() {
        appendOutput("\nStorage Information:\n")
        appendOutput("App directory: ${filesDir.absolutePath}\n")
        
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val extDir = Environment.getExternalStorageDirectory()
            appendOutput("External storage: ${extDir.absolutePath}\n")
            appendOutput("Try: ls ${extDir.absolutePath}\n")
            appendOutput("Try: ls /sdcard\n")
            appendOutput("Try: ls /storage/emulated/0\n")
        } else {
            appendOutput("External storage not available\n")
        }
        appendOutput("\n")
    }
    
    private fun executeCommand() {
        val command = commandInput.text.toString().trim()
        if (command.isEmpty()) return
        
        appendOutput("\n\$ $command\n")
        
        thread {
            try {
                // Execute command
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                
                // Read output and error
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                
                // Wait for completion
                process.waitFor()
                
                // Display results
                runOnUiThread {
                    if (output.isNotEmpty()) {
                        appendOutput(output)
                    }
                    if (error.isNotEmpty()) {
                        appendOutput("Error: $error\n")
                    }
                    appendOutput("Exit code: ${process.exitValue()}\n")
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    appendOutput("Failed: ${e.message}\n")
                }
            }
        }
        
        commandInput.text.clear()
    }
    
    private fun appendOutput(text: String) {
        outputText.append(text)
        // Auto-scroll to bottom
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}

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
import java.io.File

class MainActivity : Activity() {
    
    private lateinit var commandInput: EditText
    private lateinit var outputText: TextView
    
    // Legacy permissions (pre-Android 11)
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        requestPermissions()
        showStorageInfo()
    }
    
    private fun createUI() {
        val title = TextView(this)
        title.setText("Terminal (Legacy Storage)")
        title.setTextSize(20f)
        
        commandInput = EditText(this)
        commandInput.setHint("Try: ls /sdcard, pwd, etc.")
        commandInput.setPadding(20, 20, 20, 20)
        
        val executeButton = Button(this)
        executeButton.setText("EXECUTE")
        executeButton.setOnClickListener { runCommand() }
        
        val permButton = Button(this)
        permButton.setText("REQUEST PERMISSIONS")
        permButton.setOnClickListener { 
            requestPermissions()
            showStorageInfo()
        }
        
        outputText = TextView(this)
        outputText.setText("Terminal Output:\n")
        outputText.setPadding(20, 20, 20, 20)
        
        val scrollView = ScrollView(this)
        scrollView.addView(outputText)
        
        val layout = LinearLayout(this)
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.setPadding(40, 60, 40, 60)
        layout.addView(title)
        layout.addView(commandInput)
        layout.addView(executeButton)
        layout.addView(permButton)
        layout.addView(scrollView)
        
        setContentView(layout)
    }
    
    private fun showStorageInfo() {
        outputText.append("\nStorage Paths:\n")
        
        // App's private directory (always accessible)
        val appDir = filesDir
        outputText.append("App Dir: ${appDir.absolutePath}\n")
        
        // External storage (needs permission)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val extDir = Environment.getExternalStorageDirectory()
            outputText.append("External: ${extDir.absolutePath}\n")
            
            // Common Huawei paths
            outputText.append("Try: ls ${extDir.absolutePath}\n")
            outputText.append("Try: ls /storage/emulated/0\n")
            outputText.append("Try: ls /sdcard\n")
        } else {
            outputText.append("External storage NOT available\n")
        }
    }
    
    private fun requestPermissions() {
        val missingPerms = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPerms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPerms.toTypedArray(), 100)
            outputText.append("\nRequesting permissions...\n")
        } else {
            outputText.append("\nAll permissions granted!\n")
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            outputText.append("\nPermissions result received.\n")
            showStorageInfo()
        }
    }
    
    private fun runCommand() {
        val cmd = commandInput.getText().toString()
        if (cmd.isEmpty()) return
        
        outputText.append("\n\$ $cmd\n")
        
        Thread {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                val output = process.getInputStream().bufferedReader().readText()
                val error = process.getErrorStream().bufferedReader().readText()
                process.waitFor()
                
                runOnUiThread {
                    if (output.isNotEmpty()) outputText.append("$output\n")
                    if (error.isNotEmpty()) outputText.append("ERROR: $error\n")
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    outputText.append("Failed: ${e.message}\n")
                }
            }
        }.start()
        
        commandInput.setText("")
    }
}

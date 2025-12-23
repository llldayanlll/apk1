package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ScrollView

class MainActivity : Activity() {
    
    private lateinit var commandInput: EditText
    private lateinit var outputText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create simple UI
        commandInput = EditText(this).apply {
            hint = "Type command here"
            setPadding(20, 20, 20, 20)
        }
        
        val executeButton = Button(this).apply {
            text = "RUN"
            setOnClickListener { runCommand() }
        }
        
        outputText = TextView(this).apply {
            text = "Terminal Output:\n\n"
            setPadding(20, 20, 20, 20)
        }
        
        val scrollView = ScrollView(this).apply {
            addView(outputText)
        }
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
            addView(TextView(this@MainActivity).apply {
                text = "Simple Terminal"
                textSize = 20f
            })
            addView(commandInput)
            addView(executeButton)
            addView(scrollView)
        }
        
        setContentView(layout)
    }
    
    private fun runCommand() {
        val cmd = commandInput.text.toString()
        if (cmd.isEmpty()) return
        
        outputText.append("\n\$ $cmd\n")
        
        try {
            // Execute command
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            
            outputText.append(output)
            if (error.isNotEmpty()) outputText.append("Error: $error\n")
            
        } catch (e: Exception) {
            outputText.append("Failed: ${e.message}\n")
        }
        
        commandInput.text.clear()
    }
}

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
        
        // Create UI with Java-style code
        val title = TextView(this)
        title.setText("Simple Terminal")
        title.setTextSize(20f)
        
        commandInput = EditText(this)
        commandInput.setHint("Type command here")
        commandInput.setPadding(20, 20, 20, 20)
        
        val executeButton = Button(this)
        executeButton.setText("RUN")
        executeButton.setOnClickListener { runCommand() }
        
        outputText = TextView(this)
        outputText.setText("Terminal Output:\n\n")
        outputText.setPadding(20, 20, 20, 20)
        
        val scrollView = ScrollView(this)
        scrollView.addView(outputText)
        
        val layout = LinearLayout(this)
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.setPadding(40, 60, 40, 60)
        layout.addView(title)
        layout.addView(commandInput)
        layout.addView(executeButton)
        layout.addView(scrollView)
        
        setContentView(layout)
    }
    
    private fun runCommand() {
        val cmd = commandInput.getText().toString()
        if (cmd.isEmpty()) return
        
        outputText.append("\n\$ $cmd\n")
        
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val output = process.getInputStream().bufferedReader().readText()
            val error = process.getErrorStream().bufferedReader().readText()
            process.waitFor()
            
            outputText.append(output)
            if (error.isNotEmpty()) outputText.append("Error: $error\n")
            
        } catch (e: Exception) {
            outputText.append("Failed: ${e.message}\n")
        }
        
        commandInput.setText("")
    }
}

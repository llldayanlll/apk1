package com.example.clickcounter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // SIMPLE layout without complex features
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = android.view.Gravity.CENTER
        
        // Title
        val title = TextView(this)
        title.text = "Addition Calculator"
        title.textSize = 24f
        layout.addView(title)
        
        // Input 1
        val input1 = EditText(this)
        input1.hint = "First number"
        input1.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input1.setPadding(20, 20, 20, 20)
        layout.addView(input1)
        
        // Plus sign
        val plus = TextView(this)
        plus.text = "+"
        plus.textSize = 20f
        plus.gravity = android.view.Gravity.CENTER
        layout.addView(plus)
        
        // Input 2
        val input2 = EditText(this)
        input2.hint = "Second number"
        input2.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input2.setPadding(20, 20, 20, 20)
        layout.addView(input2)
        
        // Add Button
        val addButton = Button(this)
        addButton.text = "ADD"
        addButton.setPadding(30, 15, 30, 15)
        layout.addView(addButton)
        
        // Result
        val result = TextView(this)
        result.text = "Result: "
        result.textSize = 20f
        result.setPadding(0, 30, 0, 0)
        layout.addView(result)
        
        // Clear Button
        val clearButton = Button(this)
        clearButton.text = "CLEAR"
        clearButton.setPadding(30, 10, 30, 10)
        layout.addView(clearButton)
        
        // Button actions
        addButton.setOnClickListener {
            try {
                val num1 = if (input1.text.toString().isEmpty()) 0.0 
                          else input1.text.toString().toDouble()
                val num2 = if (input2.text.toString().isEmpty()) 0.0 
                          else input2.text.toString().toDouble()
                val sum = num1 + num2
                result.text = "Result: $sum"
            } catch (e: Exception) {
                result.text = "Error: Use numbers only"
            }
        }
        
        clearButton.setOnClickListener {
            input1.text.clear()
            input2.text.clear()
            result.text = "Result: "
        }
        
        // Set padding for entire layout
        layout.setPadding(50, 100, 50, 100)
        
        setContentView(layout)
    }
}

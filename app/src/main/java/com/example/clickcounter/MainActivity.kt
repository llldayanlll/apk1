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
        
        // Create input fields
        val input1 = EditText(this).apply {
            hint = "Enter first number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(20, 20, 20, 20)
        }
        
        val input2 = EditText(this).apply {
            hint = "Enter second number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(20, 20, 20, 20)
        }
        
        // Create result text
        val resultText = TextView(this).apply {
            text = "Result: "
            textSize = 24f
            setPadding(0, 40, 0, 40)
        }
        
        // Create add button
        val addButton = Button(this).apply {
            text = "ADD"
            setOnClickListener {
                try {
                    val num1 = input1.text.toString().toDoubleOrNull() ?: 0.0
                    val num2 = input2.text.toString().toDoubleOrNull() ?: 0.0
                    val sum = num1 + num2
                    resultText.text = "Result: \$sum"
                } catch (e: Exception) {
                    resultText.text = "Error: Enter valid numbers"
                }
            }
        }
        
        // Create clear button
        val clearButton = Button(this).apply {
            text = "CLEAR"
            setOnClickListener {
                input1.text.clear()
                input2.text.clear()
                resultText.text = "Result: "
            }
        }
        
        // Create layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(50, 100, 50, 100)
            
            addView(TextView(this@MainActivity).apply {
                text = "Addition Calculator"
                textSize = 32f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, 50)
            })
            
            addView(input1)
            addView(TextView(this@MainActivity).apply {
                text = "+"
                textSize = 24f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 10, 0, 10)
            })
            addView(input2)
            addView(addButton)
            addView(clearButton)
            addView(resultText)
        }
        
        setContentView(layout)
    }
}

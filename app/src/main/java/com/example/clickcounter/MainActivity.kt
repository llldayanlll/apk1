package com.example.clickcounter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var count = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "Clicks: 0"
            textSize = 48f
        }
        
        val clickButton = Button(this).apply {
            text = "CLICK ME!"
            setOnClickListener {
                count++
                textView.text = "Clicks: $count"
            }
        }
        
        val resetButton = Button(this).apply {
            text = "RESET"
            setOnClickListener {
                count = 0
                textView.text = "Clicks: $count"
            }
        }
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(100, 100, 100, 100)
            addView(textView)
            addView(clickButton)
            addView(resetButton)
        }
        
        setContentView(layout)
    }
}

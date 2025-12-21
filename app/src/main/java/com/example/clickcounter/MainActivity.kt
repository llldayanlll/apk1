package com.example.clickcounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    private var count = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "Clicks: 0"
            textSize = 48f
        }
        
        val button = Button(this).apply {
            text = "Click Me!"
            setOnClickListener {
                count++
                textView.text = "Clicks: $count"
            }
        }
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            addView(textView)
            addView(button)
        }
        
        setContentView(layout)
    }
}

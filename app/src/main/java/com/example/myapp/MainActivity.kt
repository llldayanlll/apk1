package com.example.myapp

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // SIMPLE - No AppCompat, No Kotlin extensions
        val textView = TextView(this)
        textView.text = "TCP Chat App"
        textView.textSize = 24f
        textView.gravity = Gravity.CENTER
        
        val status = TextView(this)
        status.text = "Working on Huawei..."
        status.textSize = 16f
        status.gravity = Gravity.CENTER
        status.setPadding(0, 50, 0, 0)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(100, 100, 100, 100)
        
        layout.addView(textView)
        layout.addView(status)
        
        setContentView(layout)
    }
}

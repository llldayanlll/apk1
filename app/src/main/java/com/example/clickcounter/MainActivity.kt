package com.example.clickcounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    
    private var count = 0
    private lateinit var countTextView: TextView
    private lateinit var clickButton: Button
    private lateinit var resetButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        countTextView = findViewById(R.id.countTextView)
        clickButton = findViewById(R.id.clickButton)
        resetButton = findViewById(R.id.resetButton)
        
        updateCount()
        
        clickButton.setOnClickListener {
            count++
            updateCount()
        }
        
        resetButton.setOnClickListener {
            count = 0
            updateCount()
        }
    }
    
    private fun updateCount() {
        countTextView.text = "Clicks: $count"
    }
}

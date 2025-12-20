package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var count = 0
    private lateinit var countText: TextView
    private lateinit var clickButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countText = findViewById(R.id.count_text)
        clickButton = findViewById(R.id.click_button)
        resetButton = findViewById(R.id.reset_button)

        updateCountText()

        clickButton.setOnClickListener {
            count++
            updateCountText()
            if (count == 10) {
                countText.text = "🎉 You reached 10 clicks!"
            }
        }

        resetButton.setOnClickListener {
            count = 0
            updateCountText()
        }
    }

    private fun updateCountText() {
        countText.text = "Clicks: $count"
    }
}

package com.example.lab2

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)



        val tvHistory = findViewById<TextView>(R.id.tvHistory)
        val btnClear = findViewById<Button>(R.id.btnClearHistory)

        loadHistory(tvHistory)

        btnClear.setOnClickListener {
            val file = File(filesDir, "history.txt")
            if (file.exists()) {
                file.delete()
                tvHistory.text = "Дані відсутні"
                Toast.makeText(this, "Історію очищено!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHistory(tvHistory: TextView) {
        val file = File(filesDir, "history.txt")
        if (file.exists() && file.readText().isNotEmpty()) {
            tvHistory.text = file.readText()
        } else {
            tvHistory.text = "Дані відсутні"
        }
    }
}
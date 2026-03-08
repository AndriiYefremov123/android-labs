package com.example.lab1

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editQuestion = findViewById<EditText>(R.id.editQuestion)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val btnOk = findViewById<Button>(R.id.btnOk)
        val textResult = findViewById<TextView>(R.id.textResult)

        btnOk.setOnClickListener {
            val question = editQuestion.text.toString()

            if (question.isEmpty()) {
                Toast.makeText(this, "Введіть питання!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedId = radioGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(this, "Оберіть відповідь!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val radio = findViewById<RadioButton>(selectedId)
            val answer = radio.text.toString()

            textResult.text = "Питання: $question\nВідповідь: $answer"
        }
    }
}
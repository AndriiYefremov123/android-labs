package com.example.lab2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.io.File

class InputFragment : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etQuestion = view.findViewById<EditText>(R.id.etQuestion)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnOpen = view.findViewById<Button>(R.id.btnOpen)

        btnOk.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            val selectedId = radioGroup.checkedRadioButtonId

            if (question.isEmpty() && selectedId == -1) {
                Toast.makeText(requireContext(),
                    "Введіть питання та оберіть відповідь!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (question.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Введіть питання!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedId == -1) {
                Toast.makeText(requireContext(),
                    "Оберіть відповідь!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val answer = if (selectedId == R.id.rbYes) "Так" else "Ні"

            // Зберігаємо у ViewModel
            viewModel.question.value = question
            viewModel.answer.value = answer

            // Записуємо у файл
            try {
                val file = File(requireContext().filesDir, "history.txt")
                file.appendText("Питання: $question | Відповідь: $answer\n")
                Toast.makeText(requireContext(),
                    "Збережено у файл!",
                    Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "Помилка збереження!",
                    Toast.LENGTH_SHORT).show()
            }

            // Перехід до ResultFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ResultFragment())
                .addToBackStack(null)
                .commit()
        }

        // Перехід до HistoryActivity
        btnOpen.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}
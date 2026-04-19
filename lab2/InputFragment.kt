package com.example.lab2

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

        btnOk.setOnClickListener {
            val question = etQuestion.text.toString().trim()
            val selectedId = radioGroup.checkedRadioButtonId

            // Валідація — перевірка чи всі дані введені
            if (question.isEmpty() && selectedId == -1) {
                Toast.makeText(
                    requireContext(),
                    "Введіть питання та оберіть відповідь!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (question.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Введіть питання!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedId == -1) {
                Toast.makeText(
                    requireContext(),
                    "Оберіть відповідь!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val answer = if (selectedId == R.id.rbYes) "Так" else "Ні"

            // Зберігаємо дані у ViewModel
            viewModel.question.value = question
            viewModel.answer.value = answer

            // Переходимо до ResultFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ResultFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // Метод для очищення форми (викликається з ResultFragment через ViewModel)
    fun clearForm() {
        view?.findViewById<EditText>(R.id.etQuestion)?.text?.clear()
        view?.findViewById<RadioGroup>(R.id.radioGroup)?.clearCheck()
    }
}
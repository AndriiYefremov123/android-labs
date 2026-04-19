package com.example.lab2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ResultFragment : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // Отримуємо дані з ViewModel і відображаємо
        val question = viewModel.question.value ?: ""
        val answer = viewModel.answer.value ?: ""
        tvResult.text = "Питання: $question\nВідповідь: $answer"

        btnCancel.setOnClickListener {
            // Очищаємо ViewModel
            viewModel.question.value = ""
            viewModel.answer.value = ""

            // Повертаємось назад до InputFragment
            parentFragmentManager.popBackStack()
        }
    }
}
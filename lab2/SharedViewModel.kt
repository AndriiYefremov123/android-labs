package com.example.lab2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val question = MutableLiveData<String>()
    val answer = MutableLiveData<String>()
}
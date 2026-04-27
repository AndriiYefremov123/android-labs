package com.example.lab4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var selectedUri: Uri? = null
    private lateinit var tvSelectedFile: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var etUrl: EditText
    private lateinit var radioGroupType: RadioGroup
    private lateinit var radioGroupSource: RadioGroup

    // Лаунчер для вибору файлу
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            tvSelectedFile.text = "Обрано: ${uri.lastPathSegment}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioGroupType = findViewById(R.id.radioGroupType)
        radioGroupSource = findViewById(R.id.radioGroupSource)
        etUrl = findViewById(R.id.etUrl)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        val btnPlay = findViewById<Button>(R.id.btnPlay)

        // Показуємо/ховаємо елементи залежно від джерела
        radioGroupSource.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbLocal -> {
                    btnSelectFile.visibility = View.VISIBLE
                    etUrl.visibility = View.GONE
                }
                R.id.rbInternet -> {
                    btnSelectFile.visibility = View.GONE
                    etUrl.visibility = View.VISIBLE
                }
            }
        }

        btnSelectFile.setOnClickListener {
            requestPermissionAndPickFile()
        }

        btnPlay.setOnClickListener {
            val sourceId = radioGroupSource.checkedRadioButtonId
            val typeId = radioGroupType.checkedRadioButtonId

            if (typeId == -1) {
                Toast.makeText(this, "Оберіть тип файлу!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (sourceId == -1) {
                Toast.makeText(this, "Оберіть джерело!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isVideo = typeId == R.id.rbVideo

            if (sourceId == R.id.rbInternet) {
                val url = etUrl.text.toString().trim()
                if (url.isEmpty()) {
                    Toast.makeText(this, "Введіть URL!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                openPlayer(url, isVideo)
            } else {
                if (selectedUri == null) {
                    Toast.makeText(this, "Оберіть файл!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                openPlayer(selectedUri.toString(), isVideo)
            }
        }
    }

    private fun requestPermissionAndPickFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val typeId = radioGroupType.checkedRadioButtonId
            val permission = if (typeId == R.id.rbVideo)
                Manifest.permission.READ_MEDIA_VIDEO
            else
                Manifest.permission.READ_MEDIA_AUDIO

            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
            } else {
                pickFile()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                pickFile()
            }
        }
    }

    private fun pickFile() {
        val typeId = radioGroupType.checkedRadioButtonId
        if (typeId == -1) {
            Toast.makeText(this, "Спочатку оберіть тип файлу!", Toast.LENGTH_SHORT).show()
            return
        }
        val mimeType = if (typeId == R.id.rbVideo) "video/*" else "audio/*"
        filePickerLauncher.launch(mimeType)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickFile()
        } else {
            Toast.makeText(this, "Дозвіл не надано!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlayer(uriString: String, isVideo: Boolean) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("uri", uriString)
        intent.putExtra("isVideo", isVideo)
        startActivity(intent)
    }
}
package ro.pub.cs.systems.eim.practicaltest02v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var editTextWord: EditText
    private lateinit var buttonLookup: Button
    private lateinit var textViewDefinition: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v2_main)

        // Legăm componentele UI
        editTextWord = findViewById(R.id.editTextWord)
        buttonLookup = findViewById(R.id.buttonLookup)
        textViewDefinition = findViewById(R.id.textViewDefinition)

        // Acțiune buton (deocamdată doar un exemplu de reacție)
        buttonLookup.setOnClickListener {
            val word = editTextWord.text.toString().trim()
            textViewDefinition.text = if (word.isNotEmpty()) {
                "Căutăm definiția pentru \"$word\"..."
            } else {
                "Introduceți un cuvânt mai întâi!"
            }
        }
    }
}

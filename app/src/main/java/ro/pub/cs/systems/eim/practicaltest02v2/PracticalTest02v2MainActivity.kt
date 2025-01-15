package ro.pub.cs.systems.eim.practicaltest02v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import kotlin.concurrent.thread

class PracticalTest02v2MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PracticalTest02v2"

        // Acțiunea broadcast-ului
        const val ACTION_DICTIONARY_BROADCAST = "ro.pub.cs.systems.eim.practicaltest02v2.DICTIONARY_BROADCAST"
        // Cheia extra
        const val EXTRA_DEFINITION = "extra_definition"
    }

    private lateinit var editTextWord: EditText
    private lateinit var buttonLookup: Button
    private lateinit var textViewDefinition: TextView

    // BroadcastReceiver dinamic
    private val definitionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DICTIONARY_BROADCAST) {
                val definition = intent.getStringExtra(EXTRA_DEFINITION) ?: "N/A"
                textViewDefinition.text = definition
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v2_main)

        editTextWord = findViewById(R.id.editTextWord)
        buttonLookup = findViewById(R.id.buttonLookup)
        textViewDefinition = findViewById(R.id.textViewDefinition)

        // Android 12+ => trebuie să specificăm RECEIVER_NOT_EXPORTED dacă nu vrem să fie public
        val intentFilter = IntentFilter(ACTION_DICTIONARY_BROADCAST)
        registerReceiver(
            definitionReceiver,
            intentFilter,
            /* broadcastPermission = */ null,
            /* scheduler = */ null,
            Context.RECEIVER_NOT_EXPORTED
        )

        buttonLookup.setOnClickListener {
            val word = editTextWord.text.toString().trim()
            if (word.isNotEmpty()) {
                lookupDefinition(word)
            } else {
                textViewDefinition.text = getString(R.string.hint_enter_word)
            }
        }
    }

    // Face request la https://api.dictionaryapi.dev/api/v2/entries/en/<word>
    // Afișăm tot răspunsul în Logcat, apoi parsăm prima definiție.
    private fun lookupDefinition(word: String) {
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    // Afișăm tot răspunsul
                    Log.d(TAG, "Răspuns complet:\n$responseBody")

                    // Parsăm JSON
                    val rootArray = JSONArray(responseBody)
                    val firstObj = rootArray.optJSONObject(0)
                    val meanings = firstObj?.optJSONArray("meanings")
                    val firstMeaningObj = meanings?.optJSONObject(0)
                    val definitions = firstMeaningObj?.optJSONArray("definitions")
                    val firstDefObj = definitions?.optJSONObject(0)
                    val firstDefinition = firstDefObj?.optString("definition") ?: "Def. indisponibilă"

                    Log.d(TAG, "Prima definiție: $firstDefinition")

                    // Emit broadcast
                    val broadcastIntent = Intent(ACTION_DICTIONARY_BROADCAST)
                    broadcastIntent.setPackage(packageName) // doar propria aplicație, + exported false
                    broadcastIntent.putExtra(EXTRA_DEFINITION, firstDefinition)
                    sendBroadcast(broadcastIntent)
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e(TAG, "Eroare: ${ex.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(definitionReceiver)
    }
}

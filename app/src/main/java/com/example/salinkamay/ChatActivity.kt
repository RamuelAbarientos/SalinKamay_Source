package com.example.salinkamay

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class ChatActivity : AppCompatActivity(), WebSocketManager.OnMessageListener {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageEditText: TextInputEditText
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var voiceButton: ImageButton


    private lateinit var webSocketManager: WebSocketManager



    private val serverUrl = "ws://10.19.49.75:8001/ws"


    private val speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            messageEditText.setText(spokenText)
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_chat
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_chat -> {

                    true
                }
                R.id.nav_camera -> {
                    startActivity(Intent(this, TranslateActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }


        messageEditText = findViewById(R.id.messageEditText)
        val sendButton: Button = findViewById(R.id.sendButton)
        voiceButton = findViewById(R.id.voiceButton)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        val backButton: ImageView = findViewById(R.id.backButton)
        val cameraButton: ImageView = findViewById(R.id.userProfilePic)


        backButton.setOnClickListener {
            finish()
        }


        cameraButton.setOnClickListener {
            val intent = Intent(this, TranslateActivity::class.java)
            startActivity(intent)
        }


        voiceButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }


        chatAdapter = ChatAdapter(this)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }


        chatAdapter.loadMessages()


        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {

                addUserMessage(messageText)
                messageEditText.text?.clear()
            }
        }


        initializeWebSocket()
    }

    private fun initializeWebSocket() {
        webSocketManager = WebSocketManager(serverUrl)
        webSocketManager.setOnMessageListener(this)
        webSocketManager.connect()
    }


    override fun onMessageReceived(word: String) {

        if (word.length > 1 && word != "Not okay" && word != "Okay"  && word != "Letter"  && word != "Number") {
            runOnUiThread {
                addServerMessage(word)
            }
        }
    }

    override fun onConnectionEstablished() {
        runOnUiThread {
            Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionError(error: String) {
        runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }

        try {
            speechRecognizer.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addUserMessage(text: String) {
        val message = ChatMessage(text, isFromServer = false)
        chatAdapter.addMessage(message)
        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }


    private fun addServerMessage(text: String) {
        val message = ChatMessage(text, isFromServer = true)
        chatAdapter.addMessage(message)
        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()

        webSocketManager.disconnect()
        chatAdapter.shutdown()
    }
}
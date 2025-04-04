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

    // WebSocket manager for connecting to the server
    private lateinit var webSocketManager: WebSocketManager

    // Server URL - replace with your actual server IP and port
    private val serverUrl = "ws://192.168.254.159:8001/ws"

    // Register for speech recognition result
    private val speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            messageEditText.setText(spokenText)
        }
    }

    // Register for permission request
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

        // Initialize views
        messageEditText = findViewById(R.id.messageEditText)
        val sendButton: Button = findViewById(R.id.sendButton)
        voiceButton = findViewById(R.id.voiceButton) // New voice button
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        val backButton: ImageView = findViewById(R.id.backButton)
        val cameraButton: ImageView = findViewById(R.id.userProfilePic) // Camera button

        // Handle Back Button Click
        backButton.setOnClickListener {
            finish() // Closes the activity and returns to the previous screen
        }

        // Handle Camera Button Click (Navigate to TranslateActivity)
        cameraButton.setOnClickListener {
            val intent = Intent(this, TranslateActivity::class.java)
            startActivity(intent)
        }

        // Setup voice button click listener
        voiceButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Setup RecyclerView with Adapter
        chatAdapter = ChatAdapter(this)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        // Load saved messages
        chatAdapter.loadMessages()

        // Setup send button click listener
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Add message as user message (not from server)
                addUserMessage(messageText)
                messageEditText.text?.clear()
            }
        }

        // Initialize WebSocket connection
        initializeWebSocket()
    }

    private fun initializeWebSocket() {
        webSocketManager = WebSocketManager(serverUrl)
        webSocketManager.setOnMessageListener(this)
        webSocketManager.connect()
    }

    // WebSocket OnMessageListener interface implementations
    override fun onMessageReceived(word: String) {
        // Add the word received from the server to the chat
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

    // Method to add user messages
    private fun addUserMessage(text: String) {
        val message = ChatMessage(text, isFromServer = false)
        chatAdapter.addMessage(message)
        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }

    // Method to add server messages
    private fun addServerMessage(text: String) {
        val message = ChatMessage(text, isFromServer = true)
        chatAdapter.addMessage(message)
        chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources when activity is destroyed
        webSocketManager.disconnect()
        chatAdapter.shutdown()
    }
}
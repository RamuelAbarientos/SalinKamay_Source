package com.example.salinkamay

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class TranslateActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var tvStatus: TextView
    private var webSocket: WebSocket? = null

    companion object {
        private const val TAG = "TranslateActivity"

        private const val WEB_SOCKET_URL = "ws://10.19.49.75:8001/ws"
        const val EXTRA_RECEIVED_WORD = "com.example.salinkamay.EXTRA_RECEIVED_WORD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)


        imageView = findViewById(R.id.imageView)
        tvStatus = findViewById(R.id.tvStatus)


        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_placeholder)
            .into(imageView)


        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(WEB_SOCKET_URL)
            .build()

        try {
            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    runOnUiThread {
                        Log.d(TAG, "WebSocket connection opened")
                        Toast.makeText(this@TranslateActivity, "Connected to server!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        Log.d(TAG, "Full received message: $text")
                        val jsonObject = JSONObject(text)


                        if (jsonObject.has("image") && jsonObject.has("letter")) {
                            val base64Image = jsonObject.getString("image")
                            val letter = jsonObject.getString("letter")


                            val bitmap = decodeBase64ToBitmap(base64Image)

                            runOnUiThread {
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap)
                                    tvStatus.text = "$letter"
                                } else {
                                    tvStatus.text = "Failed to decode image"
                                }
                            }
                        }


                        if (jsonObject.has("word")) {
                            val receivedWord = jsonObject.getString("word")
                            Log.d(TAG, "Received word: $receivedWord")


                            if (receivedWord.isNotEmpty()) {
                                sendWordToChat(receivedWord)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onMessage", e)
                        runOnUiThread {
                            tvStatus.text = "Error processing message"
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e(TAG, "WebSocket failure", t)
                    runOnUiThread {
                        Toast.makeText(this@TranslateActivity, "Connection failed: ${t.message}", Toast.LENGTH_LONG).show()
                        tvStatus.text = "Connection error: ${t.message}"
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "WebSocket closed: code=$code, reason=$reason")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up WebSocket", e)
            Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_LONG).show()
        }


        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_camera
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
                    startActivity(Intent(this, ChatActivity::class.java))
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
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.cancel()
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val base64Image = base64Str.replace("data:image/png;base64,", "")
            val decodedBytes = Base64.decode(base64Image, Base64.NO_WRAP)
            val inputStream = ByteArrayInputStream(decodedBytes)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Bitmap decoding error", e)
            null
        }
    }


    private fun sendWordToChat(word: String) {

        val intent = Intent("com.example.salinkamay.RECEIVED_WORD_ACTION")
        intent.putExtra(EXTRA_RECEIVED_WORD, word)
        sendBroadcast(intent)


        val chatIntent = Intent(this, ChatActivity::class.java)
        chatIntent.putExtra(EXTRA_RECEIVED_WORD, word)
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(chatIntent)


        Toast.makeText(this, "New message: $word", Toast.LENGTH_SHORT).show()
    }
}

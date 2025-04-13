package com.example.salinkamay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Base64

class ChatTranslateActivity : AppCompatActivity() {

    private lateinit var translatedMessageTextView: TextView
    private lateinit var gestureImagesLayout: LinearLayout
    private var webSocket: WebSocket? = null
    private val TAG = "ChatTranslateActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_translate)

        translatedMessageTextView = findViewById(R.id.translatedMessage)
        gestureImagesLayout = findViewById(R.id.gestureImagesLayout)

        // Get the message text passed from the intent
        val messageText = intent.getStringExtra("message") ?: ""
        translatedMessageTextView.text = "Translating: $messageText"

        // Connect to WebSocket server and send the message
        connectWebSocket(messageText)
    }

    private fun connectWebSocket(message: String) {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            // HOTSPOT PIXEL 6a - "ws://10.19.49.75:8001/ws"
            // WiFi sa bahay PC- "ws://10.19.49.80:8001/ws"

            .url("ws://10.19.49.75:8002/ws")  // Replace with your server IP
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d(TAG, "WebSocket connection established")
                // Send the message to translate
                webSocket.send(message)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d(TAG, "Message received: $text")

                runOnUiThread {
                    try {
                        // Parse the JSON response
                        val jsonResponse = JSONObject(text)
                        val word = jsonResponse.getString("word")
                        val letters = jsonResponse.getJSONArray("letters")

                        // Clear previous images
                        gestureImagesLayout.removeAllViews()

                        // Create a TextView for each letter and an ImageView below it
                        for (i in 0 until letters.length()) {
                            val letterObj = letters.getJSONObject(i)
                            val letter = letterObj.getString("letter")

                            // Create container for this letter
                            val letterContainer = LinearLayout(this@ChatTranslateActivity)
                            letterContainer.orientation = LinearLayout.VERTICAL
                            letterContainer.gravity = Gravity.CENTER
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginEnd = 16  // Add some spacing between letters
                            letterContainer.layoutParams = layoutParams

                            // Add letter text
                            val letterTextView = TextView(this@ChatTranslateActivity)
                            letterTextView.text = letter.uppercase()
                            letterTextView.textSize = 18f
                            letterContainer.addView(letterTextView)

                            // Add sign language image
                            val imageView = ImageView(this@ChatTranslateActivity)
                            val imageParams = LinearLayout.LayoutParams(1000, 1000)
                            imageView.layoutParams = imageParams

                            if (letterObj.has("image")) {
                                val imageBase64 = letterObj.getString("image")
                                val bitmap = base64ToBitmap(imageBase64)
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap)
                                }
                            } else if (letterObj.has("error")) {
                                // Show placeholder for error
                                imageView.setImageResource(R.drawable.space)
                                Toast.makeText(
                                    this@ChatTranslateActivity,
                                    letterObj.getString("error"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            letterContainer.addView(imageView)
                            gestureImagesLayout.addView(letterContainer)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing response", e)
                        Toast.makeText(
                            this@ChatTranslateActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Close the WebSocket connection since we've received our response
                webSocket.close(1000, "Received response")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e(TAG, "WebSocket failure", t)
                runOnUiThread {
                    Toast.makeText(
                        this@ChatTranslateActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.d(TAG, "WebSocket closing: $reason")
                webSocket.close(1000, "Closing")
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        try {
            // Strip the "data:image/png;base64," part if present
            val base64Image = base64String.replace("data:image/png;base64,", "")
            Log.d(TAG, "Base64 String after removing prefix: $base64Image")

            val decodedString = Base64.decode(base64Image, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding base64 string: ${e.message}")
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close(1000, "Activity destroyed")
    }
}

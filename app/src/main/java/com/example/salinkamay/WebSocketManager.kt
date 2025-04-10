package com.example.salinkamay

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import org.json.JSONObject

class WebSocketManager(private val serverUrl: String) {

    private var webSocketClient: WebSocketClient? = null
    private var messageListener: OnMessageListener? = null

    interface OnMessageListener {
        fun onMessageReceived(word: String)
        fun onConnectionEstablished()
        fun onConnectionError(error: String)
    }

    fun setOnMessageListener(listener: OnMessageListener) {
        this.messageListener = listener
    }

    fun connect() {
        try {
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d(TAG, "WebSocket connection opened")
                    messageListener?.onConnectionEstablished()
                }

                override fun onMessage(message: String?) {
                    Log.d(TAG, "WebSocket message received: $message")
                    if (message != null) {
                        try {
                            val jsonObject = JSONObject(message)
                            val word = jsonObject.optString("letter", "")
                            if (word.isNotEmpty()) {
                                messageListener?.onMessageReceived(word)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing WebSocket message: ${e.message}")
                        }
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket connection closed. Code: $code, Reason: $reason")
                    attemptReconnect()
                }

                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket error: ${ex?.message}")
                    messageListener?.onConnectionError("Connection error: ${ex?.message}")
                    attemptReconnect()
                }
            }

            webSocketClient?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid WebSocket URI: ${e.message}")
            messageListener?.onConnectionError("Invalid server URL")
        } catch (e: Exception) {
            Log.e(TAG, "WebSocket connection error: ${e.message}")
            messageListener?.onConnectionError("Connection error: ${e.message}")
        }
    }

    private fun attemptReconnect() {

        disconnect()
        Thread.sleep(3000)
        connect()
    }

    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
    }

    fun isConnected(): Boolean {
        return webSocketClient?.isOpen == true
    }

    companion object {
        private const val TAG = "WebSocketManager"
    }
}
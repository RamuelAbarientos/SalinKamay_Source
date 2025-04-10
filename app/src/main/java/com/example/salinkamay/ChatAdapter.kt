package com.example.salinkamay

import android.app.AlertDialog
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale
import android.content.Intent
import android.animation.ObjectAnimator


class ChatAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        private const val VIEW_TYPE_USER_MESSAGE = 0
        private const val VIEW_TYPE_SERVER_MESSAGE = 1
    }

    private val messages: MutableList<ChatMessage> = mutableListOf()
    private var enlargedPosition: Int = RecyclerView.NO_POSITION
    private val prefs = context.getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)


    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        loadMessages()


        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                isTtsReady = (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED)

                if (!isTtsReady) {
                    Log.e("ChatAdapter", "TTS language not supported")
                }
            } else {
                Log.e("ChatAdapter", "TTS initialization failed")
            }
        }
    }


    class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: AutoScrollTextView = itemView.findViewById(R.id.messageText)
        val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
    }


    class ServerMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: AutoScrollTextView = itemView.findViewById(R.id.messageText)
        val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromServer) {
            VIEW_TYPE_SERVER_MESSAGE
        } else {
            VIEW_TYPE_USER_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SERVER_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_server_message, parent, false)
                ServerMessageViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_message, parent, false)
                UserMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is UserMessageViewHolder -> {
                bindUserMessage(holder, message, position)
            }
            is ServerMessageViewHolder -> {
                bindServerMessage(holder, message, position)
            }
        }

    }

    private fun bindUserMessage(holder: UserMessageViewHolder, message: ChatMessage, position: Int) {
        holder.messageText.text = message.text
        holder.messageTimestamp.text = message.getFormattedTime()


        holder.messageText.setAutoScrollEnabled(true)


        if (position == enlargedPosition) {
            holder.messageText.textSize = 125f
            holder.messageText.setLargeTextMode(true)
        } else {
            holder.messageText.textSize = 16f
            holder.messageText.setLargeTextMode(false)
        }


        holder.messageText.post {
            holder.messageText.restartScrolling()
        }


        holder.messageText.setOnClickListener {
            val previousPosition = enlargedPosition
            enlargedPosition = if (position == enlargedPosition) RecyclerView.NO_POSITION else position

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(position)
        }


        holder.messageText.postDelayed({
            holder.messageText.restartScrolling()
        }, 100)


        holder.itemView.setOnLongClickListener {
            showOptionsDialog(position)
            true
        }
    }

    private fun bindServerMessage(holder: ServerMessageViewHolder, message: ChatMessage, position: Int) {
        holder.messageText.text = message.text
        holder.messageTimestamp.text = message.getFormattedTime()


        holder.messageText.setAutoScrollEnabled(true)


        if (position == enlargedPosition) {
            holder.messageText.textSize = 125f
            holder.messageText.setLargeTextMode(true)
        } else {
            holder.messageText.textSize = 16f
            holder.messageText.setLargeTextMode(false)
        }


        holder.messageText.post {
            holder.messageText.restartScrolling()
        }


        holder.messageText.setOnClickListener {
            val previousPosition = enlargedPosition
            enlargedPosition = if (position == enlargedPosition) RecyclerView.NO_POSITION else position

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(position)
        }


        holder.messageText.postDelayed({
            holder.messageText.restartScrolling()
        }, 100)


        holder.itemView.setOnLongClickListener {
            showOptionsDialog(position)
            true
        }
    }

    private fun toggleTextSize(position: Int, messageText: AutoScrollTextView) {
        val targetSize = if (messageText.textSize == 16f) 24f else 16f
        ObjectAnimator.ofFloat(messageText, "textSize", targetSize).apply {
            duration = 200
            start()
        }
    }

    private fun showOptionsDialog(position: Int) {
        val options = arrayOf("Read Aloud", "Delete Message", "Translate Message")

        AlertDialog.Builder(context)
            .setTitle("Message Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> readMessageAloud(position)
                    1 -> showDeleteConfirmationDialog(position)
                    2 -> translateMessage(position)
                }
            }
            .show()
    }

    private fun readMessageAloud(position: Int) {
        if (position >= 0 && position < messages.size) {
            val messageText = messages[position].text

            if (isTtsReady) {
                textToSpeech?.stop()
                textToSpeech?.speak(messageText, TextToSpeech.QUEUE_FLUSH, null, "msg_${position}")
                Toast.makeText(context, "Reading message...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Text-to-Speech not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Yes") { _, _ -> removeMessage(position) }
            .setNegativeButton("No", null)
            .show()
    }


    private fun translateMessage(position: Int) {
        if (position >= 0 && position < messages.size) {
            val messageText = messages[position].text


            Toast.makeText(context, "Translating: $messageText", Toast.LENGTH_SHORT).show()


            val intent = Intent(context, ChatTranslateActivity::class.java)
            intent.putExtra("message", messageText)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        saveMessages()
        notifyItemInserted(messages.size - 1)
    }

    fun removeMessage(position: Int) {
        if (position >= 0 && position < messages.size) {
            messages.removeAt(position)
            saveMessages()
            notifyItemRemoved(position)
            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadMessages() {
        val json = prefs.getString("messages", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<ChatMessage>>() {}.type
            messages.clear()
            messages.addAll(Gson().fromJson(json, type))
            notifyDataSetChanged()
        }
    }

    private fun saveMessages() {
        prefs.edit().putString("messages", Gson().toJson(messages)).apply()
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
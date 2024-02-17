package com.github.namuan.mirrollama

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val HUMAN = "🗣"
private const val ROBOT = "🤖"

class ChatViewModel {
    val prompt: StringProperty = SimpleStringProperty()
    val chatHistory: StringProperty = SimpleStringProperty()
    val disablePrompting: BooleanProperty = SimpleBooleanProperty()

    private val file: File = File(applicationDirectory, "chat.txt")

    fun resetPrompt() {
        prompt.set("")
        disablePrompting.set(false)
    }

    fun disableNewRequests() {
        disablePrompting.set(true)
    }

    fun enableNewRequests() {
        disablePrompting.set(false)
        resetPrompt()
    }

    fun getChatContext(): String {
        return safeChatHistory() + safePrompt()
    }

    fun updateChatContext(promptResponse: String) {
        val output = buildOutputFrom(safePrompt(), promptResponse)
        chatHistory.set(safeChatHistory() + output)
        file.appendText(output)
    }

    fun clearChatHistory() {
        chatHistory.set("")
    }

    private fun safeChatHistory() = chatHistory.get().orEmpty()

    private fun safePrompt() = prompt.get().orEmpty()

    private fun buildOutputFrom(
        prompt: String,
        result: String
    ): String {
        val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())

        return """

[${dateString}] ${HUMAN}: $prompt

${ROBOT}: $result
---""".trimIndent()
    }
}

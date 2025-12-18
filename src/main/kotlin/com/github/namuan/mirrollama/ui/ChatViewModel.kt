package com.github.namuan.mirrollama.ui

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class ChatModelSession {
    val output: StringProperty = SimpleStringProperty("")
    val showProgress: BooleanProperty = SimpleBooleanProperty(false)
    val enableLike: BooleanProperty = SimpleBooleanProperty(true)

    fun clearOutput() {
        output.set("")
        showProgress.set(true)
    }

    fun updateChatContext(promptResponse: String) {
        showProgress.set(false)
        output.set(output.get() + promptResponse)
    }

    fun disableLike() {
        enableLike.set(false)
    }

    fun reset() {
        enableLike.set(true)
    }
}

class ChatViewModel {
    val prompt: StringProperty = SimpleStringProperty()
    val disablePrompting: BooleanProperty = SimpleBooleanProperty()
    
    val modelSessions = listOf(
        ChatModelSession(),
        ChatModelSession(),
        ChatModelSession()
    )

    fun disableNewRequests() {
        disablePrompting.set(true)
    }

    fun enableNewRequests() {
        disablePrompting.set(false)
        modelSessions.forEach { it.reset() }
    }

    fun clearAllOutputs() {
        modelSessions.forEach { it.clearOutput() }
    }

    fun safePrompt() = prompt.get().orEmpty()

    fun safeMixItPromptWith(outputs: List<String>): String {
        return safeMixItPrompt() + "\n" + outputs.joinToString("\n")
    }

    // Prompt from https://github.com/severian42/MoA-Ollama-ChatApp/blob/main/utils.py
    private fun safeMixItPrompt(): String {
        return """
            You have been provided with a set of responses from various open-source models to the latest user query. Your task is to synthesize these responses into a single, high-quality response. It is crucial to critically evaluate the information provided in these responses, recognizing that some of it may be biased or incorrect. Your response should not simply replicate the given answers but should offer a refined, accurate, and comprehensive reply to the instruction. Ensure your response is well-structured, coherent, and adheres to the highest standards of accuracy and reliability.
        """.trimIndent()
    }
}

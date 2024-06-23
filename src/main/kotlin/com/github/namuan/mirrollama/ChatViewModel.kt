package com.github.namuan.mirrollama

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class ChatViewModel {
    val prompt: StringProperty = SimpleStringProperty()
    val outputModel1: StringProperty = SimpleStringProperty()
    val outputModel2: StringProperty = SimpleStringProperty()
    val outputModel3: StringProperty = SimpleStringProperty()
    val disablePrompting: BooleanProperty = SimpleBooleanProperty()
    val showModel1Progress: BooleanProperty = SimpleBooleanProperty(false)
    val showModel2Progress: BooleanProperty = SimpleBooleanProperty(false)
    val showModel3Progress: BooleanProperty = SimpleBooleanProperty(false)
    val enableModel1Like: BooleanProperty = SimpleBooleanProperty(true)
    val enableModel2Like: BooleanProperty = SimpleBooleanProperty(true)
    val enableModel3Like: BooleanProperty = SimpleBooleanProperty(true)

    fun disableNewRequests() {
        disablePrompting.set(true)
    }

    fun enableNewRequests() {
        disablePrompting.set(false)
        enableModel1Like.set(true)
        enableModel2Like.set(true)
        enableModel3Like.set(true)
    }

    fun updateChatContext1(promptResponse: String) {
        showModel1Progress.set(false)
        outputModel1.set(outputModel1.get() + promptResponse)
    }

    fun updateChatContext2(promptResponse: String) {
        showModel2Progress.set(false)
        outputModel2.set(outputModel2.get() + promptResponse)
    }

    fun updateChatContext3(promptResponse: String) {
        showModel3Progress.set(false)
        outputModel3.set(outputModel3.get() + promptResponse)
    }

    fun clearAllOutputs() {
        clearModel1Output()
        clearModel2Output()
        clearModel3Output()
    }

    fun clearModel1Output() {
        outputModel1.set("")
        showModel1Progress.set(true)
    }

    fun clearModel2Output() {
        outputModel2.set("")
        showModel2Progress.set(true)
    }

    fun clearModel3Output() {
        outputModel3.set("")
        showModel3Progress.set(true)
    }

    fun disableModel1Like() {
        enableModel1Like.set(false)
    }

    fun disableModel2Like() {
        enableModel2Like.set(false)
    }

    fun disableModel3Like() {
        enableModel3Like.set(false)
    }

    fun safePrompt() = prompt.get().orEmpty()

    fun safeMixItPromptWith(output1: String, output2: String, output3: String): String {
        return safeMixItPrompt() + "\n" + output1 + "\n" + output2 + "\n" + output3
    }

    // Prompt from https://github.com/severian42/MoA-Ollama-ChatApp/blob/main/utils.py
    private fun safeMixItPrompt(): String {
        return """
            You have been provided with a set of responses from various open-source models to the latest user query. Your task is to synthesize these responses into a single, high-quality response. It is crucial to critically evaluate the information provided in these responses, recognizing that some of it may be biased or incorrect. Your response should not simply replicate the given answers but should offer a refined, accurate, and comprehensive reply to the instruction. Ensure your response is well-structured, coherent, and adheres to the highest standards of accuracy and reliability.
        """.trimIndent()
    }
}

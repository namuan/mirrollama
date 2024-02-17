package com.github.namuan.mirrollama

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

class ChatViewModel {
    val prompt: StringProperty = SimpleStringProperty()
    val chatHistory1: StringProperty = SimpleStringProperty()
    val chatHistory2: StringProperty = SimpleStringProperty()
    val chatHistory3: StringProperty = SimpleStringProperty()
    val disablePrompting: BooleanProperty = SimpleBooleanProperty()

    fun disableNewRequests() {
        disablePrompting.set(true)
    }

    fun enableNewRequests() {
        disablePrompting.set(false)
    }

    fun updateChatContext1(promptResponse: String) {
        chatHistory1.set(chatHistory1.get() + promptResponse)
    }

    fun updateChatContext2(promptResponse: String) {
        chatHistory2.set(chatHistory2.get() + promptResponse)
    }

    fun updateChatContext3(promptResponse: String) {
        chatHistory3.set(chatHistory3.get() + promptResponse)
    }

    fun clearChatHistory() {
        chatHistory1.set("")
        chatHistory2.set("")
        chatHistory3.set("")
    }

    fun safePrompt() = prompt.get().orEmpty()

}

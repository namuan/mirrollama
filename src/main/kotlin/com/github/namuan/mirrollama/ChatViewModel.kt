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
    val showModel1Progress: BooleanProperty = SimpleBooleanProperty(false)
    val showModel2Progress: BooleanProperty = SimpleBooleanProperty(false)
    val showModel3Progress: BooleanProperty = SimpleBooleanProperty(false)

    fun disableNewRequests() {
        disablePrompting.set(true)
    }

    fun enableNewRequests() {
        disablePrompting.set(false)
    }

    fun updateChatContext1(promptResponse: String) {
        showModel1Progress.set(false)
        chatHistory1.set(chatHistory1.get() + promptResponse)
    }

    fun updateChatContext2(promptResponse: String) {
        showModel2Progress.set(false)
        chatHistory2.set(chatHistory2.get() + promptResponse)
    }

    fun updateChatContext3(promptResponse: String) {
        showModel3Progress.set(false)
        chatHistory3.set(chatHistory3.get() + promptResponse)
    }

    fun clearChatHistory() {
        chatHistory1.set("")
        showModel1Progress.set(true)
        chatHistory2.set("")
        showModel2Progress.set(true)
        chatHistory3.set("")
        showModel3Progress.set(true)
    }

    fun safePrompt() = prompt.get().orEmpty()

}

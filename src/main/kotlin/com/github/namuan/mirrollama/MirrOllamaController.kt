package com.github.namuan.mirrollama

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import java.util.concurrent.Executors

class MirrOllamaController {

    private val chatViewModel = ChatViewModel()

    lateinit var txtModel1: TextArea
    lateinit var selectModel1: ComboBox<Any>

    lateinit var txtModel2: TextArea
    lateinit var selectModel2: ComboBox<Any>

    lateinit var txtModel3: TextArea
    lateinit var selectModel3: ComboBox<Any>

    lateinit var txtPrompt: TextArea
    lateinit var btnSend: Button

    fun bindShortcuts() {
        btnSend.assignShortcuts(KeyCode.S) {
            btnSend.fire()
        }
        txtPrompt.assignShortcuts(KeyCode.I) {
            txtPrompt.requestFocus()
        }
    }

    fun bindViewModel() {
        txtPrompt.textProperty().bindBidirectional(chatViewModel.prompt)
        btnSend.disableProperty().bindBidirectional(chatViewModel.disablePrompting)
        txtModel1.textProperty().bindBidirectional(chatViewModel.chatHistory)
    }

    fun updateChatContext(chatContext: String) {
        logger.debug { "updateChatContext: $chatContext" }
        chatViewModel.updateChatContext(chatContext)
    }

    fun onSendPrompt(actionEvent: ActionEvent) {
        val chatContext: String = chatViewModel.getChatContext()

        val task = OllamaApiTask(chatContext, ::updateChatContext)
        task.setOnSucceeded {
            logger.debug { "OllamaApiTask task completed: ${task}" }
//            chatViewModel.updateChatContext(task.value.response)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }
        task.setOnFailed {
            val errorMessage = task.exception.stackTraceToString()
            chatViewModel.updateChatContext(errorMessage)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        Executors.newSingleThreadExecutor().submit(task)
    }

    private fun Node.assignShortcuts(keyCode: KeyCode, trigger: Runnable) {
        scene.accelerators[KeyCodeCombination(keyCode, KeyCombination.CONTROL_ANY)] = trigger
    }

    fun init() {
        txtPrompt.text = "Who are you?"
    }
}
package com.github.namuan.mirrollama

import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextArea
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import java.util.concurrent.Executors

class MirrOllamaController {


    private val chatViewModel = ChatViewModel()

    lateinit var txtModel1: TextArea
    lateinit var selectModel1: ComboBox<String>
    lateinit var progressModel1: ProgressIndicator

    lateinit var txtModel2: TextArea
    lateinit var selectModel2: ComboBox<String>
    lateinit var progressModel2: ProgressIndicator

    lateinit var txtModel3: TextArea
    lateinit var selectModel3: ComboBox<String>
    lateinit var progressModel3: ProgressIndicator

    lateinit var txtPrompt: TextArea
    lateinit var btnSend: Button

    fun bindShortcuts() {
        btnSend.assignShortcuts(KeyCode.S) { btnSend.fire() }
        txtPrompt.assignShortcuts(KeyCode.I) { txtPrompt.requestFocus() }
    }

    fun bindViewModel() {
        txtPrompt.textProperty().bindBidirectional(chatViewModel.prompt)
        btnSend.disableProperty().bindBidirectional(chatViewModel.disablePrompting)
        txtModel1.textProperty().bindBidirectional(chatViewModel.chatHistory1)
        txtModel2.textProperty().bindBidirectional(chatViewModel.chatHistory2)
        txtModel3.textProperty().bindBidirectional(chatViewModel.chatHistory3)
    }

    private fun updateChatContext1(chatContext: String) {
        progressModel1.isVisible = false
        chatViewModel.updateChatContext1(chatContext)
    }

    private fun updateChatContext2(chatContext: String) {
        progressModel2.isVisible = false
        chatViewModel.updateChatContext2(chatContext)
    }

    private fun updateChatContext3(chatContext: String) {
        progressModel3.isVisible = false
        chatViewModel.updateChatContext3(chatContext)
    }

    fun onSendPrompt(actionEvent: ActionEvent) {
        chatViewModel.clearChatHistory()

        val chatContext: String = chatViewModel.safePrompt()
        progressModel1.isVisible = true
        progressModel2.isVisible = true
        progressModel3.isVisible = true
        submitTaskFor(chatContext, selectModel1.selectionModel.selectedItem, ::updateChatContext1)
        submitTaskFor(chatContext, selectModel2.selectionModel.selectedItem, ::updateChatContext2)
        submitTaskFor(chatContext, selectModel3.selectionModel.selectedItem, ::updateChatContext3)
    }

    private fun submitTaskFor(chatContext: String, selectedModel: String, callback: (String) -> Unit) {
        val task = OllamaCompletionApiTask(selectedModel, chatContext, callback)
        task.setOnSucceeded {
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }
        task.setOnFailed {
            val errorMessage = task.exception.stackTraceToString()
            callback(errorMessage)
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
        loadAvailableModels()
        txtPrompt.text = "Who are you?"
    }

    private fun loadAvailableModels() {
        val task = OllamaModelsApiTask()
        task.setOnSucceeded {
            val (model1, model2, model3) = loadSelectedModels()
            logger.debug { "Loaded selected models: $model1, $model2, $model3" }
            selectModel1.items.addAll(task.value)
            selectModel2.items.addAll(task.value)
            selectModel3.items.addAll(task.value)
            chatViewModel.enableNewRequests()
            updateSelectedModels(model1, model2, model3)
        }
        Executors.newSingleThreadExecutor().submit(task)
    }

    private fun updateSelectedModels(model1: String?, model2: String?, model3: String?) {
        selectModel1.selectionModel.select(model1 ?: selectModel1.items[0])
        selectModel2.selectionModel.select(model2 ?: selectModel2.items[0])
        selectModel3.selectionModel.select(model3 ?: selectModel3.items[0])
    }

    fun modelChanged(actionEvent: ActionEvent) {
        saveSelectedModels(
            selectModel1.selectionModel.selectedItem,
            selectModel2.selectionModel.selectedItem,
            selectModel3.selectionModel.selectedItem
        )
    }
}
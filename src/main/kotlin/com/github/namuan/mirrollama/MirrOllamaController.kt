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
    lateinit var selectModel1: ComboBox<String>

    lateinit var txtModel2: TextArea
    lateinit var selectModel2: ComboBox<String>

    lateinit var txtModel3: TextArea
    lateinit var selectModel3: ComboBox<String>

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
        txtModel1.textProperty().bindBidirectional(chatViewModel.chatHistory1)
        txtModel2.textProperty().bindBidirectional(chatViewModel.chatHistory2)
        txtModel3.textProperty().bindBidirectional(chatViewModel.chatHistory3)
    }

    fun updateChatContext1(chatContext: String) {
        logger.debug { "updateChatContext1: $chatContext" }
        chatViewModel.updateChatContext1(chatContext)
    }

    fun updateChatContext2(chatContext: String) {
        logger.debug { "updateChatContext2: $chatContext" }
        chatViewModel.updateChatContext2(chatContext)
    }

    fun updateChatContext3(chatContext: String) {
        logger.debug { "updateChatContext3: $chatContext" }
        chatViewModel.updateChatContext3(chatContext)
    }

    fun onSendPrompt(actionEvent: ActionEvent) {
        chatViewModel.clearChatHistory()

        val chatContext: String = chatViewModel.getChatContext()
        val selectedOllamaModel1: String = selectModel1.selectionModel.selectedItem
        val task1 = OllamaApiTask(selectedOllamaModel1, chatContext, ::updateChatContext1)
        task1.setOnSucceeded {
            logger.debug { "OllamaApiTask task1 completed: ${task1}" }
//            chatViewModel.updateChatContext(task1.value.response)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }
        task1.setOnFailed {
            val errorMessage = task1.exception.stackTraceToString()
            chatViewModel.updateChatContext1(errorMessage)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        Executors.newSingleThreadExecutor().submit(task1)

        val selectedOllamaModel2: String = selectModel2.selectionModel.selectedItem
        val task2 = OllamaApiTask(selectedOllamaModel2, chatContext, ::updateChatContext2)
        task2.setOnSucceeded {
            logger.debug { "OllamaApiTask task2 completed: ${task2}" }
//            chatViewModel.updateChatContext(task2.value.response)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }
        task2.setOnFailed {
            val errorMessage = task2.exception.stackTraceToString()
            chatViewModel.updateChatContext2(errorMessage)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        Executors.newSingleThreadExecutor().submit(task2)

        val selectedOllamaModel3: String = selectModel3.selectionModel.selectedItem
        val task3 = OllamaApiTask(selectedOllamaModel3, chatContext, ::updateChatContext3)
        task3.setOnSucceeded {
            logger.debug { "OllamaApiTask task3 completed: ${task3}" }
//            chatViewModel.updateChatContext(task3.value.response)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }
        task3.setOnFailed {
            val errorMessage = task3.exception.stackTraceToString()
            chatViewModel.updateChatContext3(errorMessage)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        Executors.newSingleThreadExecutor().submit(task3)
    }

    private fun Node.assignShortcuts(keyCode: KeyCode, trigger: Runnable) {
        scene.accelerators[KeyCodeCombination(keyCode, KeyCombination.CONTROL_ANY)] = trigger
    }

    fun init() {
        loadAvailableModels()
        txtPrompt.text = "Who are you?"
    }

    private fun updateSelectedModels(model1: String?, model2: String?, model3: String?) {
        selectModel1.selectionModel.select(model1 ?: selectModel1.items[0])
        selectModel2.selectionModel.select(model2 ?: selectModel2.items[0])
        selectModel3.selectionModel.select(model3 ?: selectModel3.items[0])
    }

    private fun loadAvailableModels() {
        val task = OllamaModelsApiTask()
        task.setOnSucceeded {
            val (model1, model2, model3) = loadSelectedModels()
            logger.debug { "OllamaModelsApiTask task completed: ${task.value}" }
            selectModel1.items.addAll(task.value)
            selectModel2.items.addAll(task.value)
            selectModel3.items.addAll(task.value)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
            println("Models from configuration: $model1, $model2, $model3")
            updateSelectedModels(model1, model2, model3)
        }
        task.setOnFailed {
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        Executors.newSingleThreadExecutor().submit(task)
    }

    fun model1Changed(actionEvent: ActionEvent) {
        val model1 = selectModel1.selectionModel.selectedItem
        val model2 = selectModel2.selectionModel.selectedItem
        val model3 = selectModel3.selectionModel.selectedItem
        logger.debug { "model1Changed: $model1" }
        saveSelectedModels(model1, model2, model3)
    }

    fun model2Changed(actionEvent: ActionEvent) {
        val model1 = selectModel1.selectionModel.selectedItem
        val model2 = selectModel2.selectionModel.selectedItem
        val model3 = selectModel3.selectionModel.selectedItem
        logger.debug { "model2Changed: $model1" }
        saveSelectedModels(model1, model2, model3)
    }

    fun model3Changed(actionEvent: ActionEvent) {
        val model1 = selectModel1.selectionModel.selectedItem
        val model2 = selectModel2.selectionModel.selectedItem
        val model3 = selectModel3.selectionModel.selectedItem
        logger.debug { "model3Changed: $model1" }
        saveSelectedModels(model1, model2, model3)
    }
}
package com.github.namuan.mirrollama

import javafx.application.Platform
import javafx.beans.property.StringProperty
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

class MainController {
    private val executorService = Executors.newSingleThreadExecutor()
    private val chatViewModel = ChatViewModel()

    lateinit var txtModel1: TextArea
    lateinit var selectModel1: ComboBox<String>
    lateinit var progressModel1: ProgressIndicator
    lateinit var btnLikeModel1: Button

    lateinit var txtModel2: TextArea
    lateinit var selectModel2: ComboBox<String>
    lateinit var progressModel2: ProgressIndicator
    lateinit var btnLikeModel2: Button

    lateinit var txtModel3: TextArea
    lateinit var selectModel3: ComboBox<String>
    lateinit var progressModel3: ProgressIndicator
    lateinit var btnLikeModel3: Button

    lateinit var txtPrompt: TextArea
    lateinit var btnSend: Button

    fun bindShortcuts() {
        btnSend.assignShortcuts(KeyCode.S) { btnSend.fire() }
        txtPrompt.assignShortcuts(KeyCode.I) { txtPrompt.requestFocus() }
    }

    fun bindViewModel() {
        txtPrompt.textProperty().bindBidirectional(chatViewModel.prompt)
        btnSend.disableProperty().bindBidirectional(chatViewModel.disablePrompting)
        txtModel1.textProperty().bindBidirectional(chatViewModel.outputModel1)
        txtModel2.textProperty().bindBidirectional(chatViewModel.outputModel2)
        txtModel3.textProperty().bindBidirectional(chatViewModel.outputModel3)
        progressModel1.visibleProperty().bindBidirectional(chatViewModel.showModel1Progress)
        progressModel2.visibleProperty().bindBidirectional(chatViewModel.showModel2Progress)
        progressModel3.visibleProperty().bindBidirectional(chatViewModel.showModel3Progress)
        btnLikeModel1.visibleProperty().bindBidirectional(chatViewModel.enableModel1Like)
        btnLikeModel2.visibleProperty().bindBidirectional(chatViewModel.enableModel2Like)
        btnLikeModel3.visibleProperty().bindBidirectional(chatViewModel.enableModel3Like)
    }

    private fun updateChatContext1(chatContext: String) {
        chatViewModel.updateChatContext1(chatContext)
    }

    private fun updateChatContext2(chatContext: String) {
        chatViewModel.updateChatContext2(chatContext)
    }

    private fun updateChatContext3(chatContext: String) {
        chatViewModel.updateChatContext3(chatContext)
    }

    fun onSendPrompt(actionEvent: ActionEvent) {
        chatViewModel.clearAllOutputs()

        val chatContext: String = chatViewModel.safePrompt()
        submitTaskFor(
            chatContext,
            selectModel1.selectionModel.selectedItem,
            chatViewModel.outputModel1,
            ::updateChatContext1
        )
        submitTaskFor(
            chatContext,
            selectModel2.selectionModel.selectedItem,
            chatViewModel.outputModel2,
            ::updateChatContext2
        )
        submitTaskFor(
            chatContext,
            selectModel3.selectionModel.selectedItem,
            chatViewModel.outputModel3,
            ::updateChatContext3
        )
    }

    private fun submitTaskFor(
        chatContext: String,
        selectedModel: String,
        outputProperty: StringProperty,
        callback: (String) -> Unit,
    ) {
        val task = OllamaCompletionApiTask(selectedModel, chatContext, callback)
        task.setOnSucceeded {
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
            databaseManager.insertModelResponse(
                selectedModel,
                chatContext,
                outputProperty.value
            )
        }
        task.setOnFailed {
            val errorMessage = task.exception.stackTraceToString()
            callback(errorMessage)
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
        }

        chatViewModel.disableNewRequests()
        executorService.submit(task)
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
        executorService.submit(task)
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

    fun close() {
        executorService.shutdownNow()
    }

    fun resendModel1(actionEvent: ActionEvent) {
        chatViewModel.clearModel1Output()
        val chatContext: String = chatViewModel.safePrompt()
        submitTaskFor(
            chatContext,
            selectModel1.selectionModel.selectedItem,
            chatViewModel.outputModel1,
            ::updateChatContext1
        )
    }

    fun resendModel2(actionEvent: ActionEvent) {
        chatViewModel.clearModel2Output()
        val chatContext: String = chatViewModel.safePrompt()
        submitTaskFor(
            chatContext,
            selectModel2.selectionModel.selectedItem,
            chatViewModel.outputModel2,
            ::updateChatContext2
        )
    }

    fun resendModel3(actionEvent: ActionEvent) {
        chatViewModel.clearModel3Output()
        val chatContext: String = chatViewModel.safePrompt()
        submitTaskFor(
            chatContext,
            selectModel3.selectionModel.selectedItem,
            chatViewModel.outputModel3,
            ::updateChatContext3
        )
    }

    fun onQuit(actionEvent: ActionEvent) {
        close()
        Platform.exit()
    }

    fun likeModel1(actionEvent: ActionEvent) {
        val selectedModel1 = selectModel1.selectionModel.selectedItem
        databaseManager.updateScore(selectedModel1)
        chatViewModel.disableModel1Like()
    }

    fun likeModel2(actionEvent: ActionEvent) {
        val selectedModel2 = selectModel2.selectionModel.selectedItem
        databaseManager.updateScore(selectedModel2)
        chatViewModel.disableModel2Like()
    }

    fun likeModel3(actionEvent: ActionEvent) {
        val selectedModel3 = selectModel3.selectionModel.selectedItem
        databaseManager.updateScore(selectedModel3)
        chatViewModel.disableModel3Like()
    }

    fun onMixIt(actionEvent: ActionEvent) {
        val mixtureChatContext: String = chatViewModel.safeMixItPromptWith(
            chatViewModel.outputModel1.get().orEmpty(),
            chatViewModel.outputModel2.get().orEmpty(),
            chatViewModel.outputModel3.get().orEmpty(),
        )
        chatViewModel.clearAllOutputs()
        submitTaskFor(
            mixtureChatContext,
            selectModel1.selectionModel.selectedItem,
            chatViewModel.outputModel1,
            ::updateChatContext1
        )
        submitTaskFor(
            mixtureChatContext,
            selectModel2.selectionModel.selectedItem,
            chatViewModel.outputModel2,
            ::updateChatContext2
        )
        submitTaskFor(
            mixtureChatContext,
            selectModel3.selectionModel.selectedItem,
            chatViewModel.outputModel3,
            ::updateChatContext3
        )
    }
}

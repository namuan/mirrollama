package com.github.namuan.mirrollama.ui

import com.github.namuan.mirrollama.api.OllamaCompletionApiTask
import com.github.namuan.mirrollama.api.OllamaModelsApiTask
import com.github.namuan.mirrollama.config.databaseManager
import com.github.namuan.mirrollama.config.loadSelectedModels
import com.github.namuan.mirrollama.config.logger
import com.github.namuan.mirrollama.config.saveSelectedModels
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import java.util.concurrent.Executors

class MainController {
    private val executorService = Executors.newSingleThreadExecutor()
    private val chatViewModel = ChatViewModel()
    private val chatPanes = mutableListOf<ChatPane>()

    lateinit var txtModel1: TextArea
    lateinit var selectModel1: ComboBox<String>
    lateinit var loadingScreenModel1: ImageView
    lateinit var btnLikeModel1: Button

    lateinit var txtModel2: TextArea
    lateinit var selectModel2: ComboBox<String>
    lateinit var loadingScreenModel2: ImageView
    lateinit var btnLikeModel2: Button

    lateinit var txtModel3: TextArea
    lateinit var selectModel3: ComboBox<String>
    lateinit var loadingScreenModel3: ImageView
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

        chatPanes.clear()
        chatPanes.add(ChatPane(txtModel1, selectModel1, loadingScreenModel1, btnLikeModel1, chatViewModel.modelSessions[0]))
        chatPanes.add(ChatPane(txtModel2, selectModel2, loadingScreenModel2, btnLikeModel2, chatViewModel.modelSessions[1]))
        chatPanes.add(ChatPane(txtModel3, selectModel3, loadingScreenModel3, btnLikeModel3, chatViewModel.modelSessions[2]))

        chatPanes.forEach { it.bind() }
    }

    fun onSendPrompt(actionEvent: ActionEvent) {
        chatViewModel.clearAllOutputs()

        val chatContext: String = chatViewModel.safePrompt()
        chatPanes.forEach { pane ->
            submitTaskFor(chatContext, pane)
        }
    }

    private fun submitTaskFor(
        chatContext: String,
        chatPane: ChatPane
    ) {
        val selectedModel = chatPane.getSelectedModel() ?: return

        val task = OllamaCompletionApiTask(selectedModel, chatContext, chatPane::updateChatContext)
        task.setOnSucceeded {
            chatViewModel.enableNewRequests()
            txtPrompt.selectPositionCaret(txtPrompt.text.length)
            databaseManager.insertModelResponse(
                selectedModel,
                chatContext,
                chatPane.session.output.value
            )
        }
        task.setOnFailed {
            val errorMessage = task.exception.stackTraceToString()
            chatPane.updateChatContext(errorMessage)
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
            chatPanes.forEach { it.populateModels(task.value) }
            chatViewModel.enableNewRequests()
            updateSelectedModels(model1, model2, model3)
        }
        executorService.submit(task)
    }

    private fun updateSelectedModels(model1: String?, model2: String?, model3: String?) {
        val models = listOf(model1, model2, model3)
        chatPanes.forEachIndexed { index, pane ->
            pane.selectModel(models[index])
        }
    }

    fun modelChanged(actionEvent: ActionEvent) {
        saveSelectedModels(
            chatPanes[0].getSelectedModel(),
            chatPanes[1].getSelectedModel(),
            chatPanes[2].getSelectedModel()
        )
    }

    fun close() {
        executorService.shutdownNow()
    }

    fun resendModel1(actionEvent: ActionEvent) {
        resendModel(0)
    }

    fun resendModel2(actionEvent: ActionEvent) {
        resendModel(1)
    }

    fun resendModel3(actionEvent: ActionEvent) {
        resendModel(2)
    }
    
    private fun resendModel(index: Int) {
        val pane = chatPanes[index]
        pane.clearOutput()
        submitTaskFor(chatViewModel.safePrompt(), pane)
    }

    fun onQuit(actionEvent: ActionEvent) {
        close()
        Platform.exit()
    }

    fun likeModel1(actionEvent: ActionEvent) {
        likeModel(0)
    }

    fun likeModel2(actionEvent: ActionEvent) {
        likeModel(1)
    }

    fun likeModel3(actionEvent: ActionEvent) {
        likeModel(2)
    }
    
    private fun likeModel(index: Int) {
        val pane = chatPanes[index]
        val selectedModel = pane.getSelectedModel()
        if (selectedModel != null) {
            databaseManager.updateScore(selectedModel)
        }
        pane.session.disableLike()
    }

    fun onMixIt(actionEvent: ActionEvent) {
        val mixtureChatContext: String = getMixItContext()
        chatViewModel.clearAllOutputs()
        chatPanes.forEach { submitTaskFor(mixtureChatContext, it) }
    }

    fun mixItModel1(actionEvent: ActionEvent) {
        mixItModel(0)
    }

    fun mixItModel2(actionEvent: ActionEvent) {
        mixItModel(1)
    }

    fun mixItModel3(actionEvent: ActionEvent) {
        mixItModel(2)
    }
    
    private fun mixItModel(index: Int) {
        val mixtureChatContext = getMixItContext()
        val pane = chatPanes[index]
        pane.clearOutput()
        submitTaskFor(mixtureChatContext, pane)
    }
    
    private fun getMixItContext(): String {
        return chatViewModel.safeMixItPromptWith(
            chatPanes[0].session.output.get().orEmpty(),
            chatPanes[1].session.output.get().orEmpty(),
            chatPanes[2].session.output.get().orEmpty(),
        )
    }
}

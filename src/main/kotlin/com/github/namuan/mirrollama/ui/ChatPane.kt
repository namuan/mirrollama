package com.github.namuan.mirrollama.ui

import com.github.namuan.mirrollama.service.ChatService
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView

class ChatPane(
    private val txtArea: TextArea,
    private val comboBox: ComboBox<String>,
    private val loading: ImageView,
    private val likeButton: Button,
    private val resendButton: Button,
    private val mixItButton: Button,
    val session: ChatModelSession
) {
    fun bind() {
        txtArea.textProperty().bindBidirectional(session.output)
        loading.visibleProperty().bindBidirectional(session.showProgress)
        likeButton.visibleProperty().bindBidirectional(session.enableLike)
    }

    fun bindActions(
        chatService: ChatService,
        onResend: (String) -> Unit,
        onMixIt: () -> String,
        onModelChanged: () -> Unit
    ) {
        likeButton.setOnAction { onLike(chatService) }
        resendButton.setOnAction { onResend(session.output.get().orEmpty()) }
        mixItButton.setOnAction {
            val prompt = onMixIt()
            onResend(prompt)
        }
        comboBox.setOnAction { onModelChanged() }
    }

    fun populateModels(models: List<String>) {
        comboBox.items.addAll(models)
    }

    fun selectModel(model: String?) {
        comboBox.selectionModel.select(model ?: comboBox.items.firstOrNull())
    }

    fun getSelectedModel(): String? {
        return comboBox.selectionModel.selectedItem
    }
    
    fun clearOutput() {
        session.clearOutput()
    }
    
    fun updateChatContext(response: String) {
        session.updateChatContext(response)
    }

    fun onLike(chatService: ChatService) {
        val selectedModel = getSelectedModel()
        if (selectedModel != null) {
            chatService.likeModel(selectedModel)
        }
        session.disableLike()
    }

    fun processPrompt(
        prompt: String,
        chatService: ChatService,
        onStart: () -> Unit,
        onComplete: () -> Unit
    ) {
        val model = getSelectedModel() ?: return
        clearOutput()
        onStart()

        chatService.submitPrompt(
            model = model,
            prompt = prompt,
            onUpdate = { updateChatContext(it) },
            onSuccess = {
                chatService.logResponse(model, prompt, session.output.value)
                onComplete()
            },
            onError = { error ->
                updateChatContext(error)
                onComplete()
            }
        )
    }
}

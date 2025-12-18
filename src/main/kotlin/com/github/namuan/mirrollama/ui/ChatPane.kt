package com.github.namuan.mirrollama.ui

import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView

class ChatPane(
    private val txtArea: TextArea,
    private val comboBox: ComboBox<String>,
    private val loading: ImageView,
    private val likeButton: Button,
    val session: ChatModelSession
) {
    fun bind() {
        txtArea.textProperty().bindBidirectional(session.output)
        loading.visibleProperty().bindBidirectional(session.showProgress)
        likeButton.visibleProperty().bindBidirectional(session.enableLike)
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
}

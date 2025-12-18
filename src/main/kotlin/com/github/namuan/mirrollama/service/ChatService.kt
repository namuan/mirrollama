package com.github.namuan.mirrollama.service

import com.github.namuan.mirrollama.api.OllamaCompletionApiTask
import com.github.namuan.mirrollama.api.OllamaModelsApiTask
import com.github.namuan.mirrollama.config.databaseManager
import java.util.concurrent.Executors

class ChatService {
    private val executorService = Executors.newSingleThreadExecutor()

    fun submitPrompt(
        model: String,
        prompt: String,
        onUpdate: (String) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val task = OllamaCompletionApiTask(model, prompt, onUpdate)
        task.setOnSucceeded {
            onSuccess()
        }
        task.setOnFailed {
            onError(task.exception.stackTraceToString())
        }
        executorService.submit(task)
    }

    fun logResponse(model: String, prompt: String, response: String) {
        databaseManager.insertModelResponse(model, prompt, response)
    }

    fun likeModel(model: String) {
        databaseManager.updateScore(model)
    }

    fun loadAvailableModels(onLoaded: (List<String>) -> Unit) {
        val task = OllamaModelsApiTask()
        task.setOnSucceeded {
            onLoaded(task.value)
        }
        executorService.submit(task)
    }

    fun shutdown() {
        executorService.shutdownNow()
    }
}

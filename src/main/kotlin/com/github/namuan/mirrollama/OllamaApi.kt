package com.github.namuan.mirrollama

import javafx.concurrent.Task
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


private const val OLLAMA_BASE = "http://localhost:11434"

data class OllamaResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val context: List<Int>,
    val total_duration: Long,
    val load_duration: Long,
    val prompt_eval_count: Int,
    val prompt_eval_duration: Long,
    val eval_count: Int,
    val eval_duration: Long
)

data class StreamingOllamaResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean
)

data class OllamaRequest(
    val model: String,
    val prompt: String,
    val system: String = "default-system",
    val template: String = "default-template",
    val context: List<Int> = emptyList(),
    val stream: Boolean = false,
    val raw: Boolean = false,
    val images: List<String> = emptyList(),
    val format: String = "",
    val options: Map<String, Any> = emptyMap(),
    val keepAlive: Any? = null
)

data class ModelDetails(
    val parent_model: String,
    val format: String,
    val family: String,
    val families: List<String>?,
    val parameter_size: String,
    val quantization_level: String
)

data class ModelInfo(
    val name: String,
    val model: String,
    val modified_at: String,
    val size: Long,
    val digest: String,
    val details: ModelDetails
)

data class ModelsResponse(
    val models: List<ModelInfo>
)

fun generate(
    model: String,
    prompt: String,
    system: String = "",
    template: String = "",
    context: List<Int>? = null,
    stream: Boolean = false,
    raw: Boolean = false,
    format: String = "",
    images: List<String>? = null,
    options: Map<String, Any>? = null,
    keepAlive: Any? = null,
    callback: (String) -> Unit
) {
    require(model.isNotEmpty()) { "must provide a model" }

    val ollamaRequest = OllamaRequest(
        model = model,
        prompt = prompt,
        system = system,
        template = template,
        context = context ?: emptyList(),
        stream = stream,
        raw = raw,
        format = format,
        images = images ?: emptyList(),
        options = options ?: emptyMap(),
        keepAlive = keepAlive
    )
    val requestBody = gson.toJson(ollamaRequest)
    logger.info { "JSON Request: $requestBody" }
    val request = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .uri(URI.create("$OLLAMA_BASE/api/generate"))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    val responseBody = response.body()
    logger.debug { "JSON Response: $responseBody" }
    val completionResponse = gson.fromJson(responseBody, OllamaResponse::class.java)

    callback(completionResponse.response)
}

fun listModels(): List<String> {
    val request = HttpRequest.newBuilder()
        .header("Accept", "application/json")
        .uri(URI.create("$OLLAMA_BASE/api/tags"))
        .GET()
        .build()
    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    val responseBody = response.body()
    logger.debug { "JSON Response: $responseBody" }
    println(responseBody)
    val listModelsResponse = gson.fromJson(responseBody, ModelsResponse::class.java)
    return listModelsResponse.models.map { it.model }
}

class OllamaApiTask(val selectedModel: String, val chatContext: String, val callback: (String) -> Unit) : Task<Unit>() {
    override fun call() {
        return generate(
            model = selectedModel,
            prompt = chatContext,
            callback = callback
        )
    }
}

class OllamaModelsApiTask() : Task<List<String>>() {
    override fun call(): List<String> {
        return listModels()
    }
}

fun main() {

    fun updateChatContext(response: String) {
        println(response)
    }

    generate(
        model = "mistral:latest",
        prompt = "Hello, how are you?",
        callback = ::updateChatContext
    )

    val availableModels = listModels()
    println(availableModels)
}
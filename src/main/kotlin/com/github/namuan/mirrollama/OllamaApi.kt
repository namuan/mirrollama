package com.github.namuan.mirrollama

import com.google.gson.annotations.SerializedName
import javafx.concurrent.Task
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


private const val OLLAMA_BASE = "http://localhost:11434"

data class StreamingOllamaResponse(
    val model: String,
    @SerializedName("created_at")
    val createdAt: String,
    val response: String,
    val done: Boolean,
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
    @SerializedName("parent_model")
    val parentModel: String,
    val format: String,
    val family: String,
    val families: List<String>?,
    @SerializedName("parameter_size")
    val parameterSize: String,
    @SerializedName("quantization_level")
    val quantizationLevel: String,
)

data class ModelInfo(
    val name: String,
    val model: String,
    @SerializedName("modified_at")
    val modifiedAt: String,
    val size: Long,
    val digest: String,
    val details: ModelDetails,
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
    logger.debug { "JSON Request: $requestBody" }

    val request = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .uri(URI.create("$OLLAMA_BASE/api/generate"))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()

    logger.debug { "[${ollamaRequest.model}] ▶\uFE0F Sending request to Ollama" }
    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofLines())
    val responseBody = response.body()
    logger.debug { "[${ollamaRequest.model}] ◀\uFE0F Getting response from Ollama" }
    responseBody.map { it.trim() }.forEach {
        val completionResponse = gson.fromJson(it, StreamingOllamaResponse::class.java)
        callback(completionResponse.response)
    }
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

class OllamaApiTask(
    private val selectedModel: String,
    private val chatContext: String,
    private val callback: (String) -> Unit,
) : Task<Unit>() {
    override fun call() {
        return generate(
            model = selectedModel,
            prompt = chatContext,
            stream = true,
            callback = callback
        )
    }
}

class OllamaModelsApiTask : Task<List<String>>() {
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
        stream = true,
        callback = ::updateChatContext,
    )

    val availableModels = listModels()
    println(availableModels)
}
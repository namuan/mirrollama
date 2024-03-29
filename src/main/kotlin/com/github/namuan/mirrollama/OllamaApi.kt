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
    val system: String,
    val template: String,
    val context: List<Int>,
    val stream: Boolean,
    val raw: Boolean,
    val images: List<String>,
    val format: String,
    val options: Map<String, Any>,
    val keepAlive: Int?,
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
    keepAlive: Int? = null,
    callback: (String) -> Unit,
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
    val request = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .uri(URI.create("$OLLAMA_BASE/api/generate"))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()

    val responseBody = sendRequest(request, HttpResponse.BodyHandlers.ofLines())
    responseBody.map { it.trim() }.forEach {
        val completionResponse = gson.fromJson(it, StreamingOllamaResponse::class.java)
        callback(completionResponse.response)
    }
}

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
    val models: List<ModelInfo>,
)

fun listModels(): List<String> {
    val request = HttpRequest.newBuilder()
        .header("Accept", "application/json")
        .uri(URI.create("$OLLAMA_BASE/api/tags"))
        .GET()
        .build()
    val responseBody = sendRequest(request, HttpResponse.BodyHandlers.ofString())
    val listModelsResponse = gson.fromJson(responseBody, ModelsResponse::class.java)
    return listModelsResponse.models.map { it.model }
}

private fun <T> sendRequest(request: HttpRequest?, bodyHandler: HttpResponse.BodyHandler<T>): T {
    logger.debug { "▶ Sending request to Ollama: $request" }
    val responseBody = HttpClient.newHttpClient().send(request, bodyHandler).body()
    logger.debug { "◀ Getting response from Ollama" }
    return responseBody
}

class OllamaCompletionApiTask(
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
    generate(
        model = "mistral:latest",
        prompt = "Hello, how are you?",
        stream = true,
        callback = { print(it) },
    )
    println("")
    val availableModels = listModels()
    println(availableModels)
}

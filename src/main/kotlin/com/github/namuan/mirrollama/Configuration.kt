package com.github.namuan.mirrollama

import com.google.gson.Gson
import javafx.stage.Stage
import java.io.File

val applicationDirectory: String = System.getProperty("user.home") + "/mirrollama"

private val propertiesFile = "$applicationDirectory/app_properties.json"
private val apiKeyFile = "$applicationDirectory/api_key.json"

data class WindowPosition(val x: Double, val y: Double, val width: Double, val height: Double)

data class ApplicationProperties(val windowPosition: WindowPosition)

fun setupConfig() {
    File(applicationDirectory).mkdirs()
}

fun loadApiKey(): String {
    val apiKeyFile = File(apiKeyFile)
    return if (apiKeyFile.exists()) {
        apiKeyFile.readText().trim()
    } else {
        ""
    }
}

fun saveApiKey(apiKey: String) {
    File(apiKeyFile).writeText(apiKey)
}

fun savePosition(stage: Stage) {
    val windowPosition = WindowPosition(stage.x, stage.y, stage.width, stage.height)
    val applicationProperties = ApplicationProperties(windowPosition = windowPosition)
    val toJson = Gson().toJson(applicationProperties)
    File(propertiesFile).writeText(toJson)
}

fun loadPosition(stage: Stage) {
    if (File(propertiesFile).exists()) {
        val applicationProperties = Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        stage.x = applicationProperties.windowPosition.x
        stage.y = applicationProperties.windowPosition.y
        stage.width = applicationProperties.windowPosition.width
        stage.height = applicationProperties.windowPosition.height
    }
}
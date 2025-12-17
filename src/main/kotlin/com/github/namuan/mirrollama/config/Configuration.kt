package com.github.namuan.mirrollama.config

import com.google.gson.Gson
import javafx.stage.Stage
import java.io.File

val applicationDirectory: String = System.getProperty("user.home") + "/.mirrollama"

private val propertiesFile = "$applicationDirectory/app_properties.json"

data class WindowPosition(val x: Double, val y: Double, val width: Double, val height: Double)

data class ApplicationProperties(
    var windowPosition: WindowPosition? = null,
    var selectedModel1: String? = null,
    var selectedModel2: String? = null,
    var selectedModel3: String? = null,
)

fun setupConfig() {
    File(applicationDirectory).mkdirs()
    val databaseFile = "$applicationDirectory/mirrollama.db"
    setupDatabase(databaseFile)
}

fun savePosition(stage: Stage) {
    val applicationProperties = if (File(propertiesFile).exists()) {
        val applicationProperties = Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        val windowPosition = WindowPosition(stage.x, stage.y, stage.width, stage.height)
        applicationProperties.windowPosition = windowPosition
        applicationProperties
    } else {
        val windowPosition = WindowPosition(stage.x, stage.y, stage.width, stage.height)
        ApplicationProperties(windowPosition = windowPosition)
    }

    val toJson = Gson().toJson(applicationProperties)
    File(propertiesFile).writeText(toJson)
}

fun loadPosition(stage: Stage) {
    if (File(propertiesFile).exists()) {
        val applicationProperties = Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        if (applicationProperties.windowPosition != null) {
            stage.x = applicationProperties.windowPosition!!.x
            stage.y = applicationProperties.windowPosition!!.y
            stage.width = applicationProperties.windowPosition!!.width
            stage.height = applicationProperties.windowPosition!!.height
        }
    }
}

fun saveSelectedModels(model1: String?, model2: String?, model3: String?) {
    val applicationProperties = if (File(propertiesFile).exists()) {
        val applicationProperties = Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        applicationProperties.selectedModel1 = model1
        applicationProperties.selectedModel2 = model2
        applicationProperties.selectedModel3 = model3
        applicationProperties
    } else {
        ApplicationProperties(
            selectedModel1 = model1,
            selectedModel2 = model2,
            selectedModel3 = model3,
        )
    }

    val toJson = Gson().toJson(applicationProperties)
    File(propertiesFile).writeText(toJson)
    logger.debug { "Saved selected models: $model1, $model2, $model3" }
}

fun loadSelectedModels(): Triple<String?, String?, String?> {
    if (File(propertiesFile).exists()) {
        val applicationProperties = Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        return Triple(
            applicationProperties.selectedModel1,
            applicationProperties.selectedModel2,
            applicationProperties.selectedModel3
        )
    }
    logger.debug { "No selected models found" }
    return Triple(null, null, null)
}
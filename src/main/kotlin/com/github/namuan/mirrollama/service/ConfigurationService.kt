package com.github.namuan.mirrollama.service

import com.github.namuan.mirrollama.config.ApplicationProperties
import com.github.namuan.mirrollama.config.WindowPosition
import com.github.namuan.mirrollama.config.logger
import com.google.gson.Gson
import javafx.stage.Stage
import java.io.File

class ConfigurationService(private val propertiesFile: String) {

    fun savePosition(stage: Stage) {
        val applicationProperties = loadProperties()
        applicationProperties.windowPosition = WindowPosition(stage.x, stage.y, stage.width, stage.height)
        saveProperties(applicationProperties)
    }

    fun loadPosition(stage: Stage) {
        if (File(propertiesFile).exists()) {
            val applicationProperties = loadProperties()
            applicationProperties.windowPosition?.let {
                stage.x = it.x
                stage.y = it.y
                stage.width = it.width
                stage.height = it.height
            }
        }
    }

    fun saveSelectedModels(models: List<String?>) {
        val applicationProperties = loadProperties()
        if (models.isNotEmpty()) applicationProperties.selectedModel1 = models.getOrNull(0)
        if (models.size > 1) applicationProperties.selectedModel2 = models.getOrNull(1)
        if (models.size > 2) applicationProperties.selectedModel3 = models.getOrNull(2)
        
        saveProperties(applicationProperties)
        logger.debug { "Saved selected models: $models" }
    }

    fun loadSelectedModels(): List<String?> {
        if (File(propertiesFile).exists()) {
            val props = loadProperties()
            return listOf(props.selectedModel1, props.selectedModel2, props.selectedModel3)
        }
        logger.debug { "No selected models found" }
        return listOf(null, null, null)
    }

    private fun loadProperties(): ApplicationProperties {
        return if (File(propertiesFile).exists()) {
            Gson().fromJson(File(propertiesFile).readText(), ApplicationProperties::class.java)
        } else {
            ApplicationProperties()
        }
    }

    private fun saveProperties(properties: ApplicationProperties) {
        File(propertiesFile).writeText(Gson().toJson(properties))
    }
}

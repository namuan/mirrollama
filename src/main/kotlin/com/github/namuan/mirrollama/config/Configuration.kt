package com.github.namuan.mirrollama.config

import java.io.File

val applicationDirectory: String = System.getProperty("user.home") + "/.mirrollama"

val propertiesFile = "$applicationDirectory/app_properties.json"

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
package com.github.namuan.mirrollama.config

import com.github.namuan.mirrollama.data.DatabaseManager
import com.google.gson.Gson
import mu.KotlinLogging

val logger = KotlinLogging.logger {}

val gson: Gson = Gson()

lateinit var databaseManager: DatabaseManager

fun setupDatabase(databaseFile: String) {
    logger.info { "Setting up database in $databaseFile" }
    databaseManager = DatabaseManager(databaseFile)
    databaseManager.createTable()
}

package com.github.namuan.mirrollama

import javafx.application.Application
import javafx.stage.Stage

class MainApp : Application() {

    private fun showMainView(stage: Stage) {
        MainView("MirrOllama :: Multiplying the Power of Ollama models").setup(stage)
        stage.show()
    }

    override fun start(stage: Stage) {
        setupConfig()
        showMainView(stage)
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}
package com.github.namuan.mirrollama.ui

import com.github.namuan.mirrollama.MainApp
import com.github.namuan.mirrollama.config.loadPosition
import com.github.namuan.mirrollama.config.savePosition
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class MainView(private val title: String) {
    fun setup(stage: Stage) {
        val fxmlLoader = FXMLLoader(MainApp::class.java.getResource("mirrollama-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 800.0, 600.0)
        MainApp::class.java.getResource("/styles.css")?.toExternalForm()?.let {
            scene.stylesheets.add(it)
        }
        stage.title = title
        stage.scene = scene
        val image = Image("app.png")
        stage.icons.add(image)

        loadPosition(stage)

        val mainController = fxmlLoader.getController<MainController>()
        mainController.bindShortcuts()
        mainController.bindViewModel()
        mainController.init()

        stage.setOnCloseRequest { _ ->
            mainController.close()
            savePosition(stage)
        }
    }
}

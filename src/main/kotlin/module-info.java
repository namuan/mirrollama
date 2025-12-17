module com.github.namuan.mirrollama {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires com.google.gson;
    requires java.net.http;
    requires org.slf4j;
    requires io.github.microutils.kotlinlogging;
    requires java.sql;

    opens com.github.namuan.mirrollama to javafx.fxml, com.google.gson;
    opens com.github.namuan.mirrollama.ui to javafx.fxml;
    opens com.github.namuan.mirrollama.config to com.google.gson;
    opens com.github.namuan.mirrollama.api to com.google.gson;
    exports com.github.namuan.mirrollama;
}
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
    exports com.github.namuan.mirrollama;
}
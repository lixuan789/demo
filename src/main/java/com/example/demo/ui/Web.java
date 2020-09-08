package com.example.demo.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Web extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        WebView web=new WebView();
        WebEngine engine=web.getEngine();
        engine.load("http://localhost:1234");

        AnchorPane root=new AnchorPane();
        root.getChildren().add(web);

        Scene scene=new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("联盟链演示Demo");
        primaryStage.show();


        web.prefHeightProperty().bind(root.heightProperty());
        web.prefWidthProperty().bind(root.widthProperty());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.example.demo.utils;
import com.example.demo.DemoApplication;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class viewDemo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        javafx.scene.web.WebView web=new javafx.scene.web.WebView();
        WebEngine engine=web.getEngine();
        String port = DemoApplication.port;
        System.out.println(port);
        engine.load("http://localhost:"+port+"/a");
        //engine.load("http://www.baidu.com");
        AnchorPane root=new AnchorPane();
        root.getChildren().add(web);

        Scene scene=new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("联盟链演示");
        primaryStage.show();


        web.prefHeightProperty().bind(root.heightProperty());
        web.prefWidthProperty().bind(root.widthProperty());

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

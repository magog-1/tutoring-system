package com.tutoring.client;

import com.tutoring.client.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TutoringClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Система онлайн-репетиторства");
        primaryStage.setMaximized(true);
        
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getView(), 400, 500);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

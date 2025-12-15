package com.tutoring.client.view.tutor;

import com.tutoring.client.api.Session;
import com.tutoring.client.view.LoginView;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class TutorDashboard {
    private BorderPane view;
    private Stage primaryStage;
    
    public TutorDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new BorderPane();
        
        // Top
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #4CAF50;");
        
        Label titleLabel = new Label("Личный кабинет репетитора");
        titleLabel.setFont(new Font(20));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        Label userLabel = new Label("Пользователь: " + Session.getInstance().getCurrentUser().getFullName());
        userLabel.setStyle("-fx-text-fill: white;");
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> logout());
        
        topBox.getChildren().addAll(titleLabel, userLabel, logoutButton);
        view.setTop(topBox);
        
        // Center
        VBox center = new VBox(20);
        center.setPadding(new Insets(30));
        center.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label infoLabel = new Label("Добро пожаловать в кабинет репетитора!");
        infoLabel.setFont(new Font(16));
        
        Label statusLabel = new Label("Статус: Ожидает верификации");
        statusLabel.setStyle("-fx-text-fill: orange;");
        
        Button viewLessonsBtn = new Button("Просмотреть занятия");
        viewLessonsBtn.setPrefWidth(200);
        viewLessonsBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Функционал в разработке");
            alert.showAndWait();
        });
        
        center.getChildren().addAll(infoLabel, statusLabel, viewLessonsBtn);
        view.setCenter(center);
    }
    
    private void logout() {
        Session.getInstance().logout();
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getView(), 400, 500);
        primaryStage.setScene(scene);
    }
    
    public BorderPane getView() {
        return view;
    }
}

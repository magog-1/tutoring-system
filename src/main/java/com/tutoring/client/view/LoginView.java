package com.tutoring.client.view;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tutoring.client.api.ApiClient;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.UserDTO;
import com.tutoring.client.view.student.StudentDashboard;
import com.tutoring.client.view.tutor.TutorDashboard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginView {
    private VBox view;
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    
    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new VBox(20);
        view.setPadding(new Insets(40));
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #f5f5f5;");
        
        Label titleLabel = new Label("Вход в систему");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #333;");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        Label usernameLabel = new Label("Имя пользователя:");
        usernameField = new TextField();
        usernameField.setPromptText("Введите имя пользователя");
        usernameField.setPrefWidth(250);
        
        Label passwordLabel = new Label("Пароль:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setPrefWidth(250);
        
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 0, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 0, 3);
        
        Button loginButton = new Button("Войти");
        loginButton.setPrefWidth(250);
        loginButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setOnAction(e -> login());
        
        passwordField.setOnAction(e -> login());
        
        view.getChildren().addAll(titleLabel, grid, loginButton);
    }
    
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Предупреждение");
            alert.setHeaderText("Незаполненные поля");
            alert.setContentText("Пожалуйста, заполните все поля");
            alert.showAndWait();
            return;
        }
        
        new Thread(() -> {
            try {
                ApiClient apiClient = new ApiClient("http://localhost:8080/api");
                String response = apiClient.login(username, password);
                
                Gson gson = GsonProvider.getGson();
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                String token = jsonResponse.get("token").getAsString();
                
                apiClient.setAuthToken(token);
                
                String userResponse = apiClient.get("/users/me", String.class);
                UserDTO user = gson.fromJson(userResponse, UserDTO.class);
                
                Session.getInstance().setCurrentUser(user);
                Session.getInstance().setApiClient(apiClient);
                
                Platform.runLater(() -> {
                    primaryStage.setMaximized(true);
                    
                    if ("TUTOR".equals(user.getRole())) {
                        TutorDashboard dashboard = new TutorDashboard(primaryStage);
                        Scene scene = new Scene(dashboard.getView(), 1200, 800);
                        primaryStage.setScene(scene);
                    } else if ("STUDENT".equals(user.getRole())) {
                        StudentDashboard dashboard = new StudentDashboard(primaryStage);
                        Scene scene = new Scene(dashboard.getView(), 1200, 800);
                        primaryStage.setScene(scene);
                    }
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка входа");
                    alert.setContentText("Неверное имя пользователя или пароль");
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    public VBox getView() {
        return view;
    }
}

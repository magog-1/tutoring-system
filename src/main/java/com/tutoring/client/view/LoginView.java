package com.tutoring.client.view;

import com.google.gson.Gson;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.UserDTO;
import com.tutoring.client.view.student.StudentDashboard;
import com.tutoring.client.view.tutor.TutorDashboard;
import com.tutoring.client.view.manager.ManagerDashboard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginView {
    private VBox view;
    private Stage primaryStage;
    
    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Вход в систему");
        titleLabel.setFont(new Font(24));
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя или email");
        usernameField.setMaxWidth(300);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        passwordField.setMaxWidth(300);
        
        Button loginButton = new Button("Войти");
        loginButton.setPrefWidth(300);
        loginButton.setDefaultButton(true);
        
        Button registerButton = new Button("Регистрация");
        registerButton.setPrefWidth(300);
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(300);
        statusLabel.setAlignment(Pos.CENTER);
        
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setMaxSize(30, 30);
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Заполните все поля");
                return;
            }
            
            // Показываем индикатор
            loginButton.setDisable(true);
            progressIndicator.setVisible(true);
            statusLabel.setText("Вход...");
            statusLabel.setStyle("-fx-text-fill: #2196F3;");
            
            // Выполняем в отдельном потоке
            new Thread(() -> {
                try {
                    Session session = Session.getInstance();
                    
                    // Шаг 1: Логин (Basic Auth)
                    session.login(username, password);
                    System.out.println("[DEBUG] Логин успешен, получаем данные пользователя...");
                    
                    // Шаг 2: Получаем данные пользователя с сервера
                    String userJsonResponse = session.getApiClient().get("/user/me", String.class);
                    System.out.println("[DEBUG] Ответ сервера: " + userJsonResponse);
                    
                    // Шаг 3: Парсим JSON в UserDTO
                    Gson gson = GsonProvider.getGson();
                    UserDTO user = gson.fromJson(userJsonResponse, UserDTO.class);
                    
                    System.out.println("[DEBUG] Пользователь: " + user.getUsername() + ", роль: " + user.getRole());
                    
                    // Шаг 4: Сохраняем в сессию
                    session.setCurrentUser(user);
                    
                    // Шаг 5: Открываем дашборд
                    final String role = user.getRole();
                    Platform.runLater(() -> {
                        openDashboard(role);
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        progressIndicator.setVisible(false);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        
                        String errorMsg = ex.getMessage();
                        if (errorMsg != null) {
                            if (errorMsg.contains("401") || errorMsg.contains("403")) {
                                statusLabel.setText("Неправильное имя пользователя или пароль");
                            } else if (errorMsg.contains("not verified") || errorMsg.contains("не верифицирован")) {
                                statusLabel.setText("Ваш аккаунт ожидает верификации.\nПожалуйста, дождитесь подтверждения.");
                            } else {
                                statusLabel.setText("Ошибка входа: " + errorMsg);
                            }
                        } else {
                            statusLabel.setText("Ошибка входа");
                        }
                    });
                }
            }).start();
        });
        
        registerButton.setOnAction(e -> {
            RegisterView registerView = new RegisterView(primaryStage);
            Scene scene = new Scene(registerView.getView(), 450, 650);
            primaryStage.setScene(scene);
        });
        
        view.getChildren().addAll(
            titleLabel,
            new Label("Имя пользователя:"),
            usernameField,
            new Label("Пароль:"),
            passwordField,
            loginButton,
            progressIndicator,
            registerButton,
            statusLabel
        );
    }
    
    private void openDashboard(String role) {
        primaryStage.setMaximized(true);
        
        Scene scene;
        switch (role) {
            case "STUDENT":
                StudentDashboard studentDashboard = new StudentDashboard(primaryStage);
                scene = new Scene(studentDashboard.getView(), 900, 650);
                break;
            case "TUTOR":
                TutorDashboard tutorDashboard = new TutorDashboard(primaryStage);
                scene = new Scene(tutorDashboard.getView(), 900, 650);
                break;
            case "MANAGER":
            case "ADMIN":
                ManagerDashboard managerDashboard = new ManagerDashboard(primaryStage);
                scene = new Scene(managerDashboard.getView(), 1000, 700);
                break;
            default:
                return;
        }
        primaryStage.setScene(scene);
    }
    
    public VBox getView() {
        return view;
    }
}

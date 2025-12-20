package com.tutoring.client.view;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import java.util.HashMap;
import java.util.Map;

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
            String usernameOrEmail = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Заполните все поля");
                return;
            }
            
            // Показываем индикатор загрузки
            loginButton.setDisable(true);
            progressIndicator.setVisible(true);
            statusLabel.setText("Вход...");
            statusLabel.setStyle("-fx-text-fill: #2196F3;");
            
            new Thread(() -> {
                try {
                    Session session = Session.getInstance();
                    
                    // Отправляем запрос на логин как form data
                    Map<String, String> loginForm = new HashMap<>();
                    
                    // Проверяем, это email или username
                    if (usernameOrEmail.contains("@")) {
                        // Это email
                        loginForm.put("email", usernameOrEmail);
                        loginForm.put("username", ""); // Пустой username
                    } else {
                        // Это username
                        loginForm.put("username", usernameOrEmail);
                        loginForm.put("email", ""); // Пустой email
                    }
                    loginForm.put("password", password);
                    
                    String loginResponse = session.getApiClient().postForm("/auth/login", loginForm, String.class);
                    
                    System.out.println("[DEBUG] Login response: " + loginResponse);
                    
                    // Парсим ответ
                    Gson gson = GsonProvider.getGson();
                    JsonElement responseElement = JsonParser.parseString(loginResponse);
                    
                    String token;
                    UserDTO user;
                    
                    if (responseElement.isJsonPrimitive()) {
                        // Сервер вернул только JWT токен
                        token = responseElement.getAsString();
                        System.out.println("[DEBUG] Получен только токен: " + token);
                        
                        // Устанавливаем токен
                        session.getApiClient().setAuthToken(token);
                        
                        // Создаём mock user с основными данными
                        // TODO: Получить реальную информацию с сервера через /users/me или похожий эндпоинт
                        user = new UserDTO();
                        user.setUsername(usernameOrEmail);
                        
                        // Пытаемся распарсить JWT токен чтобы получить роль
                        try {
                            String[] parts = token.split("\\.");
                            if (parts.length >= 2) {
                                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                                JsonObject payloadJson = gson.fromJson(payload, JsonObject.class);
                                
                                if (payloadJson.has("role")) {
                                    user.setRole(payloadJson.get("role").getAsString());
                                } else if (payloadJson.has("authorities")) {
                                    // Spring Security может использовать authorities
                                    String authorities = payloadJson.get("authorities").getAsString();
                                    user.setRole(authorities.replace("ROLE_", ""));
                                }
                                
                                if (payloadJson.has("sub")) {
                                    user.setUsername(payloadJson.get("sub").getAsString());
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("[WARNING] Не удалось распарсить JWT: " + ex.getMessage());
                            // По умолчанию - student
                            user.setRole("STUDENT");
                        }
                        
                    } else if (responseElement.isJsonObject()) {
                        // Сервер вернул JSON объект
                        JsonObject responseJson = responseElement.getAsJsonObject();
                        
                        // Извлекаем токен
                        token = responseJson.get("token").getAsString();
                        session.getApiClient().setAuthToken(token);
                        
                        // Извлекаем информацию о пользователе из ответа
                        JsonObject userJson = responseJson.getAsJsonObject("user");
                        user = gson.fromJson(userJson, UserDTO.class);
                    } else {
                        throw new Exception("Неподдерживаемый формат ответа сервера");
                    }
                    
                    System.out.println("[DEBUG] Получена информация о пользователе: " + user.getUsername() + ", роль: " + user.getRole());
                    
                    session.setCurrentUser(user);
                    
                    final UserDTO finalUser = user;
                    Platform.runLater(() -> {
                        openDashboard(finalUser.getRole());
                    });
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        progressIndicator.setVisible(false);
                        statusLabel.setStyle("-fx-text-fill: red;");
                        
                        String errorMessage = ex.getMessage();
                        if (errorMessage != null && (errorMessage.contains("403") || errorMessage.contains("401"))) {
                            statusLabel.setText("Неправильное имя пользователя или пароль");
                        } else if (errorMessage != null && errorMessage.contains("not verified")) {
                            statusLabel.setText("Ваш аккаунт ещё не прошёл верификацию.\nПожалуйста, обратитесь к менеджеру.");
                        } else if (errorMessage != null && errorMessage.contains("Account not verified")) {
                            statusLabel.setText("Ваш аккаунт репетитора ожидает верификации.\nПожалуйста, дождитесь подтверждения от менеджера.");
                        } else {
                            statusLabel.setText("Ошибка входа: " + errorMessage);
                        }
                    });
                }
            }).start();
        });
        
        registerButton.setOnAction(e -> {
            RegisterView registerView = new RegisterView(primaryStage);
            Scene scene = new Scene(registerView.getView(), 450, 700);
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

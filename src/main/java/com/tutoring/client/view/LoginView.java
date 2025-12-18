package com.tutoring.client.view;

import com.tutoring.client.api.Session;
import com.tutoring.client.model.UserDTO;
import com.tutoring.client.view.student.StudentDashboard;
import com.tutoring.client.view.tutor.TutorDashboard;
import com.tutoring.client.view.manager.ManagerDashboard;
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
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Заполните все поля");
                return;
            }
            
            try {
                Session session = Session.getInstance();
                session.login(username, password);
                
                // Получаем информацию о пользователе (mock)
                UserDTO user = new UserDTO();
                user.setUsername(username);
                user.setFirstName(username);
                user.setLastName("");
                // Определяем роль по username
                if (username.contains("student")) {
                    user.setRole("STUDENT");
                } else if (username.contains("tutor")) {
                    user.setRole("TUTOR");
                } else if (username.contains("manager")) {
                    user.setRole("MANAGER");
                } else {
                    user.setRole("STUDENT");
                }
                
                session.setCurrentUser(user);
                openDashboard(user.getRole());
                
            } catch (Exception ex) {
                statusLabel.setText("Ошибка входа: " + ex.getMessage());
                ex.printStackTrace();
            }
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
            registerButton,
            statusLabel
        );
    }
    
    private void openDashboard(String role) {
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

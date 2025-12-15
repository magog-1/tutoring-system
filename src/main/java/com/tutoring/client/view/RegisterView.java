package com.tutoring.client.view;

import com.tutoring.client.api.Session;
import com.tutoring.client.model.UserDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class RegisterView {
    private VBox view;
    private Stage primaryStage;
    
    public RegisterView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Регистрация");
        titleLabel.setFont(new Font(24));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Имя");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Фамилия");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Телефон");
        
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Студент", "Репетитор");
        roleCombo.setValue("Студент");
        
        grid.add(new Label("Имя пользователя:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Пароль:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Подтвердите пароль:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        grid.add(new Label("Имя:"), 0, 4);
        grid.add(firstNameField, 1, 4);
        grid.add(new Label("Фамилия:"), 0, 5);
        grid.add(lastNameField, 1, 5);
        grid.add(new Label("Телефон:"), 0, 6);
        grid.add(phoneField, 1, 6);
        grid.add(new Label("Роль:"), 0, 7);
        grid.add(roleCombo, 1, 7);
        
        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setPrefWidth(200);
        
        Button backButton = new Button("Назад");
        backButton.setPrefWidth(200);
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        registerButton.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                statusLabel.setText("Пароли не совпадают");
                return;
            }
            
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("username", usernameField.getText());
                request.put("email", emailField.getText());
                request.put("password", passwordField.getText());
                request.put("firstName", firstNameField.getText());
                request.put("lastName", lastNameField.getText());
                request.put("phoneNumber", phoneField.getText());
                request.put("role", roleCombo.getValue().equals("Студент") ? "STUDENT" : "TUTOR");
                
                UserDTO user = Session.getInstance().getApiClient()
                    .post("/auth/register", request, UserDTO.class);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успех");
                alert.setHeaderText(null);
                alert.setContentText("Регистрация успешна! Теперь вы можете войти.");
                alert.showAndWait();
                
                // Возврат на экран входа
                LoginView loginView = new LoginView(primaryStage);
                Scene scene = new Scene(loginView.getView(), 400, 500);
                primaryStage.setScene(scene);
                
            } catch (Exception ex) {
                statusLabel.setText("Ошибка: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        backButton.setOnAction(e -> {
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView.getView(), 400, 500);
            primaryStage.setScene(scene);
        });
        
        view.getChildren().addAll(
            titleLabel,
            grid,
            registerButton,
            backButton,
            statusLabel
        );
    }
    
    public VBox getView() {
        return view;
    }
}

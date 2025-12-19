package com.tutoring.client.view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChangePasswordDialog {
    
    public Optional<Map<String, String>> show() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Изменение пароля");
        dialog.setHeaderText("Введите текущий и новый пароль");
        
        ButtonType changeButtonType = new ButtonType("Изменить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Текущий пароль");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Новый пароль");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите новый пароль");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        errorLabel.setVisible(false);
        
        grid.add(new Label("Текущий пароль:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("Новый пароль:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Подтвердите пароль:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(errorLabel, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        Button okButton = (Button) dialog.getDialogPane().lookupButton(changeButtonType);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText("Заполните все поля");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            
            if (newPassword.length() < 6) {
                errorLabel.setText("Пароль должен быть минимум 6 символов");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("Пароли не совпадают");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            
            errorLabel.setVisible(false);
        });
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("currentPassword", currentPasswordField.getText());
                result.put("newPassword", newPasswordField.getText());
                return result;
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
}

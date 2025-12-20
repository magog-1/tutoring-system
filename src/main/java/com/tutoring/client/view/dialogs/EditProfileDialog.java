package com.tutoring.client.view.dialogs;

import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EditProfileDialog {
    private JsonObject profileData;
    private boolean isTutor;
    
    public EditProfileDialog(JsonObject profileData, boolean isTutor) {
        this.profileData = profileData;
        this.isTutor = isTutor;
    }
    
    public Optional<Map<String, String>> show() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Редактирование профиля");
        dialog.setHeaderText("Измените данные профиля");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);
        
        TextField firstNameField = new TextField();
        firstNameField.setText(getJsonString(profileData, "firstName"));
        firstNameField.setPromptText("Имя");
        
        TextField lastNameField = new TextField();
        lastNameField.setText(getJsonString(profileData, "lastName"));
        lastNameField.setPromptText("Фамилия");
        
        TextField phoneField = new TextField();
        phoneField.setText(getJsonString(profileData, "phoneNumber"));
        phoneField.setPromptText("Телефон");
        
        int row = 0;
        grid.add(new Label("Имя:"), 0, row);
        grid.add(firstNameField, 1, row++);
        grid.add(new Label("Фамилия:"), 0, row);
        grid.add(lastNameField, 1, row++);
        grid.add(new Label("Телефон:"), 0, row);
        grid.add(phoneField, 1, row++);
        
        TextField educationField = null;
        TextField experienceField = null;
        TextField hourlyRateField = null;
        TextArea bioArea = null;
        TextArea subjectsArea = null;
        
        if (isTutor) {
            educationField = new TextField();
            educationField.setText(getJsonString(profileData, "education"));
            educationField.setPromptText("Образование");
            
            experienceField = new TextField();
            if (profileData.has("experienceYears") && !profileData.get("experienceYears").isJsonNull()) {
                experienceField.setText(String.valueOf(profileData.get("experienceYears").getAsInt()));
            }
            experienceField.setPromptText("Опыт работы (годы)");
            
            hourlyRateField = new TextField();
            if (profileData.has("hourlyRate") && !profileData.get("hourlyRate").isJsonNull()) {
                hourlyRateField.setText(profileData.get("hourlyRate").getAsString());
            }
            hourlyRateField.setPromptText("Ставка за час (₽)");
            
            bioArea = new TextArea();
            bioArea.setText(getJsonString(profileData, "bio"));
            bioArea.setPromptText("О себе...");
            bioArea.setPrefRowCount(3);
            bioArea.setWrapText(true);
            
            // НОВОЕ ПОЛЕ - Предметы
            subjectsArea = new TextArea();
            subjectsArea.setText(getJsonString(profileData, "subjects"));
            subjectsArea.setPromptText("Предметы (по одному на строку)\nНапример:\nМатематика\nФизика\nАнглийский язык");
            subjectsArea.setPrefRowCount(4);
            subjectsArea.setWrapText(true);
            
            grid.add(new Label("Образование:"), 0, row);
            grid.add(educationField, 1, row++);
            grid.add(new Label("Опыт (годы):"), 0, row);
            grid.add(experienceField, 1, row++);
            grid.add(new Label("Ставка за час:"), 0, row);
            grid.add(hourlyRateField, 1, row++);
            grid.add(new Label("О себе:"), 0, row);
            grid.add(bioArea, 1, row++);
            
            // Добавляем поле предметов
            Label subjectsLabel = new Label("Преподаваемые предметы:");
            subjectsLabel.setStyle("-fx-alignment: top-left;");
            grid.add(subjectsLabel, 0, row);
            grid.add(subjectsArea, 1, row++);
            
            // Подсказка
            Label hintLabel = new Label("⚠️ Укажите предметы, которые вы преподаёте");
            hintLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-font-style: italic;");
            hintLabel.setWrapText(true);
            grid.add(hintLabel, 1, row++);
            
        } else {
            ComboBox<String> educationLevelCombo = new ComboBox<>();
            educationLevelCombo.getItems().addAll(
                "Начальное", 
                "Средняя школа", 
                "Старшая школа", 
                "Бакалавриат", 
                "Магистратура", 
                "Профессиональное"
            );
            
            String currentLevel = getJsonString(profileData, "educationLevel");
            if (!currentLevel.equals("N/A")) {
                educationLevelCombo.setValue(translateEducationLevel(currentLevel));
            }
            
            TextArea learningGoalsArea = new TextArea();
            learningGoalsArea.setText(getJsonString(profileData, "learningGoals"));
            learningGoalsArea.setPromptText("Цели обучения...");
            learningGoalsArea.setPrefRowCount(3);
            learningGoalsArea.setWrapText(true);
            
            grid.add(new Label("Уровень образования:"), 0, row);
            grid.add(educationLevelCombo, 1, row++);
            grid.add(new Label("Цели обучения:"), 0, row);
            grid.add(learningGoalsArea, 1, row++);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        final TextField finalEducationField = educationField;
        final TextField finalExperienceField = experienceField;
        final TextField finalHourlyRateField = hourlyRateField;
        final TextArea finalBioArea = bioArea;
        final TextArea finalSubjectsArea = subjectsArea;
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("firstName", firstNameField.getText());
                result.put("lastName", lastNameField.getText());
                result.put("phoneNumber", phoneField.getText());
                
                if (isTutor) {
                    result.put("education", finalEducationField.getText());
                    result.put("experienceYears", finalExperienceField.getText());
                    result.put("hourlyRate", finalHourlyRateField.getText());
                    result.put("bio", finalBioArea.getText());
                    
                    // Добавляем предметы
                    result.put("subjects", finalSubjectsArea.getText());
                } else {
                    ComboBox<String> combo = (ComboBox<String>) grid.getChildren().get(7);
                    String selectedLevel = combo.getValue();
                    if (selectedLevel != null) {
                        result.put("educationLevel", translateEducationLevelToEn(selectedLevel));
                    }
                    
                    TextArea goalsArea = (TextArea) grid.getChildren().get(9);
                    result.put("learningGoals", goalsArea.getText());
                }
                
                return result;
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    private String getJsonString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            String value = json.get(key).getAsString();
            return value.equals("N/A") ? "" : value;
        }
        return "";
    }
    
    private String translateEducationLevel(String level) {
        if (level == null) return "Начальное";
        switch (level) {
            case "ELEMENTARY": return "Начальное";
            case "MIDDLE_SCHOOL": return "Средняя школа";
            case "HIGH_SCHOOL": return "Старшая школа";
            case "UNDERGRADUATE": return "Бакалавриат";
            case "GRADUATE": return "Магистратура";
            case "PROFESSIONAL": return "Профессиональное";
            default: return "Начальное";
        }
    }
    
    private String translateEducationLevelToEn(String level) {
        switch (level) {
            case "Начальное": return "ELEMENTARY";
            case "Средняя школа": return "MIDDLE_SCHOOL";
            case "Старшая школа": return "HIGH_SCHOOL";
            case "Бакалавриат": return "UNDERGRADUATE";
            case "Магистратура": return "GRADUATE";
            case "Профессиональное": return "PROFESSIONAL";
            default: return "ELEMENTARY";
        }
    }
}

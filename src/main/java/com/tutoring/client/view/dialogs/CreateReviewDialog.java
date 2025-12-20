package com.tutoring.client.view.dialogs;

import com.tutoring.client.model.TutorDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateReviewDialog {
    private final TutorDTO tutor;
    private final Long lessonId;

    public CreateReviewDialog(TutorDTO tutor, Long lessonId) {
        this.tutor = tutor;
        this.lessonId = lessonId;
    }

    public Optional<Map<String, Object>> show() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Оставить отзыв");
        dialog.setHeaderText("Отзыв о репетиторе: " + tutor.getFullName());

        ButtonType submitButtonType = new ButtonType("Отправить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        // Рейтинг
        Label ratingLabel = new Label("Оценка:");
        ratingLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup ratingGroup = new ToggleGroup();

        RadioButton[] stars = new RadioButton[5];
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i] = new RadioButton();
            stars[i].setToggleGroup(ratingGroup);
            stars[i].setUserData(rating);
            
            // Создаём звёздочки
            Label starLabel = new Label("★");
            starLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #ddd;");
            starLabel.setOnMouseEntered(e -> {
                // Подсветка при наведении
                for (int j = 0; j < rating; j++) {
                    ((Label) ratingBox.getChildren().get(j)).setStyle("-fx-font-size: 24px; -fx-text-fill: #FFD700;");
                }
            });
            starLabel.setOnMouseExited(e -> {
                // Возвращаем цвет в зависимости от выбора
                updateStarColors(ratingBox, ratingGroup);
            });
            starLabel.setOnMouseClicked(e -> {
                stars[rating - 1].setSelected(true);
                updateStarColors(ratingBox, ratingGroup);
            });
            
            ratingBox.getChildren().add(starLabel);
        }

        // Устанавливаем 5 звёзд по умолчанию
        stars[4].setSelected(true);
        updateStarColors(ratingBox, ratingGroup);

        // Комментарий
        Label commentLabel = new Label("Комментарий:");
        commentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Напишите ваш отзыв о репетиторе...");
        commentArea.setPrefRowCount(5);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-font-size: 13px;");

        // Информация
        Label infoLabel = new Label("⚠ Отзыв будет виден всем пользователям");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");

        content.getChildren().addAll(ratingLabel, ratingBox, commentLabel, commentArea, infoLabel);
        dialog.getDialogPane().setContent(content);

        // Валидация
        Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(false);

        commentArea.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isEmpty = newValue.trim().isEmpty();
            if (isEmpty) {
                commentArea.setStyle("-fx-border-color: #ff9800; -fx-border-width: 2px; -fx-font-size: 13px;");
            } else {
                commentArea.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-font-size: 13px;");
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String comment = commentArea.getText().trim();
                if (comment.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Предупреждение");
                    alert.setHeaderText("Пустой комментарий");
                    alert.setContentText("Пожалуйста, напишите комментарий к отзыву.");
                    alert.showAndWait();
                    return null;
                }

                Toggle selectedToggle = ratingGroup.getSelectedToggle();
                if (selectedToggle == null) {
                    return null;
                }

                Integer rating = (Integer) selectedToggle.getUserData();

                Map<String, Object> result = new HashMap<>();
                result.put("tutorId", tutor.getId());
                result.put("rating", rating);
                result.put("comment", comment);
                if (lessonId != null) {
                    result.put("lessonId", lessonId);
                }

                return result;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void updateStarColors(HBox ratingBox, ToggleGroup ratingGroup) {
        Toggle selected = ratingGroup.getSelectedToggle();
        int selectedRating = selected != null ? (Integer) selected.getUserData() : 0;

        for (int i = 0; i < ratingBox.getChildren().size(); i++) {
            Label star = (Label) ratingBox.getChildren().get(i);
            if (i < selectedRating) {
                star.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFD700;");
            } else {
                star.setStyle("-fx-font-size: 24px; -fx-text-fill: #ddd;");
            }
        }
    }
}

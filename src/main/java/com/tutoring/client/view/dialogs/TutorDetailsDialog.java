package com.tutoring.client.view.dialogs;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.TutorDTO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TutorDetailsDialog {
    private final TutorDTO tutor;

    public TutorDetailsDialog(TutorDTO tutor) {
        this.tutor = tutor;
    }

    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Профиль репетитора");

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(25));
        mainBox.setStyle("-fx-background-color: #f9f9f9;");

        // Заголовок с именем репетитора
        Label nameLabel = new Label(tutor.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        nameLabel.setStyle("-fx-text-fill: #2196F3;");

        // Рейтинг
        HBox ratingBox = new HBox(10);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        
        String stars = getStars(tutor.getRating() != null ? tutor.getRating().intValue() : 0);
        Label starsLabel = new Label(stars);
        starsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFD700;");
        
        Label ratingLabel = new Label(
            String.format("%.1f / 5.0", tutor.getRating() != null ? tutor.getRating() : 0.0)
        );
        ratingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #666;");
        
        Label reviewCountLabel = new Label(
            "(" + (tutor.getTotalReviews() != null ? tutor.getTotalReviews() : 0) + " отзывов)"
        );
        reviewCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        
        ratingBox.getChildren().addAll(starsLabel, ratingLabel, reviewCountLabel);

        // Карточка с информацией
        VBox profileCard = createProfileCard();

        // Разделитель
        Separator separator = new Separator();

        // Заголовок отзывов
        Label reviewsTitle = new Label("Отзывы");
        reviewsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        reviewsTitle.setStyle("-fx-text-fill: #333;");

        // Контейнер для отзывов
        VBox reviewsContainer = new VBox(15);
        reviewsContainer.setPadding(new Insets(10));

        // Индикатор загрузки
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        reviewsContainer.getChildren().add(loadingBox);

        ScrollPane reviewsScroll = new ScrollPane(reviewsContainer);
        reviewsScroll.setFitToWidth(true);
        reviewsScroll.setStyle("-fx-background-color: transparent;");
        reviewsScroll.setPrefHeight(300);
        VBox.setVgrow(reviewsScroll, Priority.ALWAYS);

        // Кнопка закрытия
        Button closeButton = new Button("Закрыть");
        closeButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 30;");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        mainBox.getChildren().addAll(
            nameLabel, 
            ratingBox, 
            profileCard, 
            separator, 
            reviewsTitle, 
            reviewsScroll,
            buttonBox
        );

        Scene scene = new Scene(mainBox, 750, 700);
        dialog.setScene(scene);

        // Загрузка отзывов
        loadReviews(reviewsContainer);

        dialog.show();
    }

    private VBox createProfileCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Образование
        if (tutor.getEducation() != null && !tutor.getEducation().isEmpty()) {
            addInfoRow(card, "🎓 Образование:", tutor.getEducation());
        }

        // Опыт работы
        if (tutor.getExperienceYears() != null && tutor.getExperienceYears() > 0) {
            addInfoRow(card, "💼 Опыт работы:", 
                tutor.getExperienceYears() + " " + getYearsWord(tutor.getExperienceYears()));
        }

        // Ставка
        if (tutor.getHourlyRate() != null) {
            addInfoRow(card, "💵 Ставка:", tutor.getHourlyRate() + " ₽/час");
        }

        // О себе
        if (tutor.getBio() != null && !tutor.getBio().isEmpty()) {
            Separator sep = new Separator();
            card.getChildren().add(sep);

            Label bioTitle = new Label("📝 О себе:");
            bioTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            bioTitle.setStyle("-fx-text-fill: #333;");

            Label bioText = new Label(tutor.getBio());
            bioText.setWrapText(true);
            bioText.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");

            card.getChildren().addAll(bioTitle, bioText);
        }

        // Контакты
        if (tutor.getEmail() != null || tutor.getPhoneNumber() != null) {
            Separator sep = new Separator();
            card.getChildren().add(sep);

            Label contactTitle = new Label("📞 Контакты:");
            contactTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            contactTitle.setStyle("-fx-text-fill: #333;");
            card.getChildren().add(contactTitle);

            if (tutor.getEmail() != null) {
                addInfoRow(card, "Email:", tutor.getEmail());
            }
            if (tutor.getPhoneNumber() != null) {
                addInfoRow(card, "Телефон:", tutor.getPhoneNumber());
            }
        }

        return card;
    }

    private void addInfoRow(VBox parent, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.setFont(Font.font("System", FontWeight.BOLD, 13));
        labelText.setStyle("-fx-text-fill: #666;");
        labelText.setMinWidth(180);

        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: #333; -fx-font-size: 13px;");
        valueText.setWrapText(true);

        row.getChildren().addAll(labelText, valueText);
        parent.getChildren().add(row);
    }

    private void loadReviews(VBox container) {
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient()
                    .get("/tutors/" + tutor.getId() + "/reviews", String.class);

                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> showNoReviews(container));
                    return;
                }

                Gson gson = GsonProvider.getGson();
                JsonArray reviewsArray = gson.fromJson(response, JsonArray.class);

                if (reviewsArray == null || reviewsArray.size() == 0) {
                    Platform.runLater(() -> showNoReviews(container));
                    return;
                }

                Platform.runLater(() -> {
                    container.getChildren().clear();

                    for (int i = 0; i < reviewsArray.size(); i++) {
                        JsonObject review = reviewsArray.get(i).getAsJsonObject();
                        VBox reviewCard = createReviewCard(review);
                        container.getChildren().add(reviewCard);
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> showErrorMessage(container, ex.getMessage()));
            }
        }).start();
    }

    private VBox createReviewCard(JsonObject review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");

        // Шапка с именем студента и рейтингом
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String studentName = "N/A";
        if (review.has("student") && !review.get("student").isJsonNull()) {
            JsonObject student = review.getAsJsonObject("student");
            String firstName = student.has("firstName") ? student.get("firstName").getAsString() : "";
            String lastName = student.has("lastName") ? student.get("lastName").getAsString() : "";
            studentName = (firstName + " " + lastName).trim();
        }

        Label nameLabel = new Label("👤 " + studentName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #333;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int rating = review.has("rating") ? review.get("rating").getAsInt() : 0;
        Label ratingLabel = new Label(getStars(rating));
        ratingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFD700;");

        header.getChildren().addAll(nameLabel, spacer, ratingLabel);

        // Комментарий
        String comment = review.has("comment") && !review.get("comment").isJsonNull() ? 
            review.get("comment").getAsString() : "Комментарий не оставлен";

        Label commentLabel = new Label(comment);
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        commentLabel.setPadding(new Insets(5, 0, 0, 0));

        // Дата
        String dateStr = "";
        if (review.has("createdAt") && !review.get("createdAt").isJsonNull()) {
            try {
                LocalDateTime date = LocalDateTime.parse(review.get("createdAt").getAsString());
                dateStr = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            } catch (Exception e) {
                dateStr = review.get("createdAt").getAsString();
            }
        }

        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-style: italic;");

        card.getChildren().addAll(header, commentLabel, dateLabel);

        return card;
    }

    private void showNoReviews(VBox container) {
        container.getChildren().clear();
        Label noReviewsLabel = new Label("Отзывов пока нет");
        noReviewsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 14px;");
        VBox emptyBox = new VBox(noReviewsLabel);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(50));
        container.getChildren().add(emptyBox);
    }

    private void showErrorMessage(VBox container, String message) {
        container.getChildren().clear();
        Label errorLabel = new Label("Ошибка загрузки: " + message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));
        container.getChildren().add(errorBox);
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }

    private String getYearsWord(int years) {
        if (years % 10 == 1 && years % 100 != 11) {
            return "год";
        } else if (years % 10 >= 2 && years % 10 <= 4 && (years % 100 < 10 || years % 100 >= 20)) {
            return "года";
        } else {
            return "лет";
        }
    }
}

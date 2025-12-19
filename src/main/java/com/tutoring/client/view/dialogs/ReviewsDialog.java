package com.tutoring.client.view.dialogs;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
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

public class ReviewsDialog {
    
    public void show(Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Мои отзывы");
        
        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #f9f9f9;");
        
        Label titleLabel = new Label("Отзывы о работе");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #4CAF50;");
        
        Label loadingLabel = new Label("Загрузка отзывов...");
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        
        VBox loadingBox = new VBox(15, progress, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        mainBox.getChildren().addAll(titleLabel, loadingBox);
        
        Button closeButton = new Button("Закрыть");
        closeButton.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-padding: 8 20;");
        closeButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        mainBox.getChildren().add(buttonBox);
        
        Scene scene = new Scene(mainBox, 700, 600);
        dialog.setScene(scene);
        
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient().get("/tutor/reviews", String.class);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> {
                        mainBox.getChildren().remove(loadingBox);
                        Label noReviewsLabel = new Label("У вас пока нет отзывов");
                        noReviewsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 14px;");
                        VBox emptyBox = new VBox(noReviewsLabel);
                        emptyBox.setAlignment(Pos.CENTER);
                        emptyBox.setPadding(new Insets(50));
                        mainBox.getChildren().add(1, emptyBox);
                    });
                    return;
                }
                
                Gson gson = GsonProvider.getGson();
                JsonArray reviewsArray = gson.fromJson(response, JsonArray.class);
                
                if (reviewsArray == null || reviewsArray.size() == 0) {
                    Platform.runLater(() -> {
                        mainBox.getChildren().remove(loadingBox);
                        Label noReviewsLabel = new Label("У вас пока нет отзывов");
                        noReviewsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 14px;");
                        VBox emptyBox = new VBox(noReviewsLabel);
                        emptyBox.setAlignment(Pos.CENTER);
                        emptyBox.setPadding(new Insets(50));
                        mainBox.getChildren().add(1, emptyBox);
                    });
                    return;
                }
                
                VBox reviewsBox = new VBox(15);
                reviewsBox.setPadding(new Insets(10));
                
                for (int i = 0; i < reviewsArray.size(); i++) {
                    JsonObject review = reviewsArray.get(i).getAsJsonObject();
                    VBox reviewCard = createReviewCard(review);
                    reviewsBox.getChildren().add(reviewCard);
                }
                
                scrollPane.setContent(reviewsBox);
                
                Platform.runLater(() -> {
                    mainBox.getChildren().remove(loadingBox);
                    mainBox.getChildren().add(1, scrollPane);
                });
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    mainBox.getChildren().remove(loadingBox);
                    Label errorLabel = new Label("Ошибка загрузки отзывов: " + ex.getMessage());
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                    VBox errorBox = new VBox(errorLabel);
                    errorBox.setAlignment(Pos.CENTER);
                    errorBox.setPadding(new Insets(50));
                    mainBox.getChildren().add(1, errorBox);
                });
            }
        }).start();
        
        dialog.show();
    }
    
    private VBox createReviewCard(JsonObject review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        String studentName = "N/A";
        if (review.has("student") && !review.get("student").isJsonNull()) {
            JsonObject student = review.getAsJsonObject("student");
            String firstName = student.has("firstName") ? student.get("firstName").getAsString() : "";
            String lastName = student.has("lastName") ? student.get("lastName").getAsString() : "";
            studentName = (firstName + " " + lastName).trim();
        }
        
        Label studentLabel = new Label("👤 " + studentName);
        studentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        studentLabel.setStyle("-fx-text-fill: #333;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        int rating = review.has("rating") ? review.get("rating").getAsInt() : 0;
        Label ratingLabel = new Label(getStars(rating) + " " + rating + "/5");
        ratingLabel.setFont(new Font(12));
        ratingLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(studentLabel, spacer, ratingLabel);
        
        String comment = review.has("comment") && !review.get("comment").isJsonNull() ? 
            review.get("comment").getAsString() : "Комментарий не оставлен";
        
        Label commentLabel = new Label(comment);
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");
        commentLabel.setPadding(new Insets(5, 0, 0, 0));
        
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
        
        card.getChildren().addAll(headerBox, commentLabel, dateLabel);
        
        return card;
    }
    
    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}

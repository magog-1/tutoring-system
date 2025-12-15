package com.tutoring.client.view.student;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.*;
import com.tutoring.client.view.LoginView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StudentDashboard {
    private BorderPane view;
    private Stage primaryStage;
    private TableView<TutorDTO> tutorTable;
    private TableView<LessonDTO> lessonTable;
    
    public StudentDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new BorderPane();
        
        // Top - заголовок
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #2196F3;");
        
        Label titleLabel = new Label("Личный кабинет студента");
        titleLabel.setFont(new Font(20));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        Label userLabel = new Label("Пользователь: " + Session.getInstance().getCurrentUser().getFullName());
        userLabel.setStyle("-fx-text-fill: white;");
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> logout());
        
        HBox topContent = new HBox(20);
        topContent.getChildren().addAll(titleLabel, userLabel);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        topBox.getChildren().addAll(topContent, logoutButton);
        view.setTop(topBox);
        
        // Left - меню
        VBox leftMenu = createMenu();
        view.setLeft(leftMenu);
        
        // Center - контент
        showTutorSearch();
    }
    
    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(15));
        menu.setStyle("-fx-background-color: #f5f5f5;");
        menu.setPrefWidth(200);
        
        Button searchTutorsBtn = new Button("Поиск репетиторов");
        searchTutorsBtn.setPrefWidth(180);
        searchTutorsBtn.setOnAction(e -> showTutorSearch());
        
        Button myLessonsBtn = new Button("Мои занятия");
        myLessonsBtn.setPrefWidth(180);
        myLessonsBtn.setOnAction(e -> showMyLessons());
        
        menu.getChildren().addAll(searchTutorsBtn, myLessonsBtn);
        return menu;
    }
    
    private void showTutorSearch() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Поиск репетиторов");
        titleLabel.setFont(new Font(18));
        
        // Таблица репетиторов
        tutorTable = new TableView<>();
        
        TableColumn<TutorDTO, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<TutorDTO, String> educationCol = new TableColumn<>("Образование");
        educationCol.setCellValueFactory(new PropertyValueFactory<>("education"));
        
        TableColumn<TutorDTO, Double> ratingCol = new TableColumn<>("Рейтинг");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        
        TableColumn<TutorDTO, String> rateCol = new TableColumn<>("Цена/час");
        rateCol.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));
        
        tutorTable.getColumns().addAll(nameCol, educationCol, ratingCol, rateCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadTutors());
        
        Button bookButton = new Button("Забронировать занятие");
        bookButton.setOnAction(e -> bookLesson());
        
        HBox buttonBox = new HBox(10, refreshButton, bookButton);
        
        content.getChildren().addAll(titleLabel, tutorTable, buttonBox);
        view.setCenter(content);
        
        loadTutors();
    }
    
    private void loadTutors() {
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient()
                    .get("/tutors", String.class);
                
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TutorDTO>>(){}.getType();
                List<TutorDTO> tutors = gson.fromJson(response, listType);
                
                Platform.runLater(() -> {
                    ObservableList<TutorDTO> data = FXCollections.observableArrayList(tutors);
                    tutorTable.setItems(data);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка загрузки: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void bookLesson() {
        TutorDTO selectedTutor = tutorTable.getSelectionModel().getSelectedItem();
        if (selectedTutor == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Выберите репетитора");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Бронирование");
        alert.setHeaderText("Бронирование занятия с " + selectedTutor.getFirstName());
        alert.setContentText("Функционал в разработке. Используйте API для бронирования.");
        alert.showAndWait();
    }
    
    private void showMyLessons() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Мои занятия");
        titleLabel.setFont(new Font(18));
        
        lessonTable = new TableView<>();
        
        TableColumn<LessonDTO, String> tutorCol = new TableColumn<>("Репетитор");
        tutorCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTutor() != null ? data.getValue().getTutor().getFullName() : ""
            )
        );
        
        TableColumn<LessonDTO, String> subjectCol = new TableColumn<>("Предмет");
        subjectCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getSubject() != null ? data.getValue().getSubject().getName() : ""
            )
        );
        
        TableColumn<LessonDTO, String> timeCol = new TableColumn<>("Время");
        timeCol.setCellValueFactory(data -> {
            LocalDateTime time = data.getValue().getScheduledTime();
            String formatted = time != null ? time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        
        TableColumn<LessonDTO, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        lessonTable.getColumns().addAll(tutorCol, subjectCol, timeCol, statusCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadMyLessons());
        
        content.getChildren().addAll(titleLabel, lessonTable, refreshButton);
        view.setCenter(content);
        
        loadMyLessons();
    }
    
    private void loadMyLessons() {
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient()
                    .get("/student/lessons", String.class);
                
                Gson gson = new Gson();
                Type listType = new TypeToken<List<LessonDTO>>(){}.getType();
                List<LessonDTO> lessons = gson.fromJson(response, listType);
                
                Platform.runLater(() -> {
                    ObservableList<LessonDTO> data = FXCollections.observableArrayList(lessons);
                    lessonTable.setItems(data);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    private void logout() {
        Session.getInstance().logout();
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getView(), 400, 500);
        primaryStage.setScene(scene);
    }
    
    public BorderPane getView() {
        return view;
    }
}

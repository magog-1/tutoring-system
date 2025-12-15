package com.tutoring.client.view.student;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
        
        VBox leftMenu = createMenu();
        view.setLeft(leftMenu);
        
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
        
        tutorTable = new TableView<>();
        
        TableColumn<TutorDTO, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getFirstName() != null ? data.getValue().getFirstName() : "") + " " + 
                (data.getValue().getLastName() != null ? data.getValue().getLastName() : "")
            )
        );
        
        TableColumn<TutorDTO, String> educationCol = new TableColumn<>("Образование");
        educationCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEducation() != null ? data.getValue().getEducation() : "N/A"
            )
        );
        
        TableColumn<TutorDTO, String> ratingCol = new TableColumn<>("Рейтинг");
        ratingCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRating() != null ? String.format("%.1f", data.getValue().getRating()) : "0.0"
            )
        );
        
        TableColumn<TutorDTO, String> rateCol = new TableColumn<>("Цена/час");
        rateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getHourlyRate() != null ? data.getValue().getHourlyRate().toString() + " ₽" : "N/A"
            )
        );
        
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
                System.out.println("[DEBUG] Запрашиваем /tutors...");
                String response = Session.getInstance().getApiClient().get("/tutors", String.class);
                System.out.println("[DEBUG] Получен ответ: " + response);
                
                if (response == null || response.trim().isEmpty()) {
                    System.out.println("[DEBUG] Пустой ответ от сервера");
                    Platform.runLater(() -> {
                        tutorTable.setItems(FXCollections.observableArrayList());
                    });
                    return;
                }
                
                Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
                
                List<TutorDTO> tutors = new ArrayList<>();
                
                try {
                    TutorDTO[] tutorsArray = gson.fromJson(response, TutorDTO[].class);
                    if (tutorsArray != null) {
                        tutors = Arrays.asList(tutorsArray);
                        System.out.println("[DEBUG] Успешно распарсено " + tutors.size() + " репетиторов");
                    }
                } catch (JsonSyntaxException jsonEx) {
                    System.err.println("[ERROR] Ошибка парсинга JSON: " + jsonEx.getMessage());
                    jsonEx.printStackTrace();
                    throw jsonEx;
                }
                
                final List<TutorDTO> finalTutors = tutors;
                Platform.runLater(() -> {
                    ObservableList<TutorDTO> data = FXCollections.observableArrayList(finalTutors);
                    tutorTable.setItems(data);
                    System.out.println("[DEBUG] Данные загружены в таблицу");
                });
            } catch (Exception ex) {
                System.err.println("[ERROR] Ошибка при загрузке репетиторов:");
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка загрузки данных");
                    alert.setContentText("Не удалось загрузить список репетиторов.\n\n" + 
                        ex.getClass().getSimpleName() + ": " + ex.getMessage());
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
        alert.setHeaderText("Бронирование занятия");
        alert.setContentText("Функционал в разработке.\nИспользуйте HTTP запросы для бронирования.");
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
                data.getValue().getTutor() != null ? data.getValue().getTutor().getFullName() : "N/A"
            )
        );
        
        TableColumn<LessonDTO, String> subjectCol = new TableColumn<>("Предмет");
        subjectCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getSubject() != null ? data.getValue().getSubject().getName() : "N/A"
            )
        );
        
        TableColumn<LessonDTO, String> timeCol = new TableColumn<>("Время");
        timeCol.setCellValueFactory(data -> {
            LocalDateTime time = data.getValue().getScheduledTime();
            String formatted = time != null ? time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });
        
        TableColumn<LessonDTO, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus() : "N/A"
            )
        );
        
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
                System.out.println("[DEBUG] Запрашиваем /student/lessons...");
                String response = Session.getInstance().getApiClient().get("/student/lessons", String.class);
                System.out.println("[DEBUG] Получен ответ: " + response);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> lessonTable.setItems(FXCollections.observableArrayList()));
                    return;
                }
                
                Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();
                
                LessonDTO[] lessonsArray = gson.fromJson(response, LessonDTO[].class);
                List<LessonDTO> lessons = lessonsArray != null ? Arrays.asList(lessonsArray) : new ArrayList<>();
                
                Platform.runLater(() -> {
                    ObservableList<LessonDTO> data = FXCollections.observableArrayList(lessons);
                    lessonTable.setItems(data);
                });
            } catch (Exception ex) {
                System.err.println("[ERROR] Ошибка при загрузке занятий:");
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка загрузки: " + ex.getMessage());
                    alert.showAndWait();
                });
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

package com.tutoring.client.view.tutor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.LessonDTO;
import com.tutoring.client.view.LoginView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class TutorDashboard {
    private BorderPane view;
    private Stage primaryStage;
    private TableView<LessonDTO> lessonsTable;
    private ComboBox<String> statusFilter;
    private List<LessonDTO> allLessons = new ArrayList<>();
    private LocalDate selectedDate = LocalDate.now();
    private YearMonth currentYearMonth = YearMonth.now();
    
    public TutorDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new BorderPane();
        
        // Top - заголовок
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #4CAF50;");
        
        Label titleLabel = new Label("Личный кабинет репетитора");
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
        
        // Center - по умолчанию показываем занятия
        showMyLessons();
    }
    
    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(15));
        menu.setStyle("-fx-background-color: #f5f5f5;");
        menu.setPrefWidth(200);
        
        Button myLessonsBtn = new Button("Мои занятия");
        myLessonsBtn.setPrefWidth(180);
        myLessonsBtn.setOnAction(e -> showMyLessons());
        
        Button scheduleBtn = new Button("Расписание");
        scheduleBtn.setPrefWidth(180);
        scheduleBtn.setOnAction(e -> showSchedule());
        
        Button profileBtn = new Button("Мой профиль");
        profileBtn.setPrefWidth(180);
        profileBtn.setOnAction(e -> showProfile());
        
        menu.getChildren().addAll(myLessonsBtn, scheduleBtn, profileBtn);
        return menu;
    }
    
    private void showMyLessons() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Мои занятия");
        titleLabel.setFont(new Font(18));
        
        // Фильтры
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Фильтр:");
        
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Все", "Согласование преподавателем", "Подтвержден преподавателем", "Завершен", "Отменено");
        statusFilter.setValue("Все");
        statusFilter.setOnAction(e -> loadMyLessons(statusFilter.getValue()));
        
        filterBox.getChildren().addAll(filterLabel, statusFilter);
        
        // Таблица занятий
        lessonsTable = new TableView<>();
        lessonsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<LessonDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(60);
        
        TableColumn<LessonDTO, String> studentCol = new TableColumn<>("Студент");
        studentCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStudent() != null ? data.getValue().getStudent().getFullName() : "N/A"
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
        
        TableColumn<LessonDTO, String> durationCol = new TableColumn<>("Длительность");
        durationCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDurationMinutes() != null ? data.getValue().getDurationMinutes() + " мин" : "N/A"
            )
        );
        
        TableColumn<LessonDTO, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus() != null ? translateStatus(data.getValue().getStatus()) : "N/A"
            )
        );
        
        TableColumn<LessonDTO, String> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPrice() != null ? data.getValue().getPrice() + " ₽" : "N/A"
            )
        );
        
        lessonsTable.getColumns().addAll(idCol, studentCol, subjectCol, timeCol, durationCol, statusCol, priceCol);
        
        // Кнопки управления
        HBox buttonBox = new HBox(10);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadMyLessons(statusFilter.getValue()));
        
        Button viewDetailsButton = new Button("Подробности");
        viewDetailsButton.setOnAction(e -> viewLessonDetails());
        
        Button confirmButton = new Button("Подтвердить");
        confirmButton.setOnAction(e -> confirmLesson());
        
        Button completeButton = new Button("Завершить");
        completeButton.setOnAction(e -> completeLesson());
        
        buttonBox.getChildren().addAll(refreshButton, viewDetailsButton, confirmButton, completeButton);
        
        content.getChildren().addAll(titleLabel, filterBox, lessonsTable, buttonBox);
        view.setCenter(content);
        
        // Загрузка данных
        loadMyLessons("Все");
    }
    
    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "Согласование преподавателем";
            case "CONFIRMED": return "Подтвержден преподавателем";
            case "COMPLETED": return "Завершен";
            case "CANCELLED": return "Отменено";
            default: return status;
        }
    }
    
    private void loadMyLessons(String statusFilter) {
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] Запрашиваем /tutor/lessons...");
                String response = Session.getInstance().getApiClient().get("/tutor/lessons", String.class);
                System.out.println("[DEBUG] Получен ответ: " + response);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> lessonsTable.setItems(FXCollections.observableArrayList()));
                    return;
                }
                
                Gson gson = GsonProvider.getGson();
                LessonDTO[] lessonsArray = gson.fromJson(response, LessonDTO[].class);
                List<LessonDTO> lessons = lessonsArray != null ? Arrays.asList(lessonsArray) : new ArrayList<>();
                
                allLessons = new ArrayList<>(lessons); // Сохраняем для календаря
                
                // Фильтрация по статусу
                if (!"Все".equals(statusFilter)) {
                    String statusEn;
                    switch (statusFilter) {
                        case "Согласование преподавателем":
                            statusEn = "PENDING";
                            break;
                        case "Подтвержден преподавателем":
                            statusEn = "CONFIRMED";
                            break;
                        case "Завершен":
                            statusEn = "COMPLETED";
                            break;
                        case "Отменено":
                            statusEn = "CANCELLED";
                            break;
                        default:
                            statusEn = null;
                    }
                    if (statusEn != null) {
                        String finalStatusEn = statusEn;
                        lessons = lessons.stream()
                            .filter(l -> finalStatusEn.equalsIgnoreCase(l.getStatus()))
                            .toList();
                    }
                }
                
                System.out.println("[DEBUG] Распарсено " + lessons.size() + " занятий");
                
                final List<LessonDTO> finalLessons = lessons;
                Platform.runLater(() -> {
                    ObservableList<LessonDTO> data = FXCollections.observableArrayList(finalLessons);
                    lessonsTable.setItems(data);
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
    
    private void viewLessonDetails() {
        LessonDTO selected = lessonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите занятие из списка");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Подробности занятия");
        alert.setHeaderText("Занятие #" + selected.getId());
        
        StringBuilder details = new StringBuilder();
        details.append("Студент: ").append(selected.getStudent() != null ? selected.getStudent().getFullName() : "N/A").append("\n");
        details.append("Предмет: ").append(selected.getSubject() != null ? selected.getSubject().getName() : "N/A").append("\n");
        details.append("Время: ").append(selected.getScheduledTime() != null ? 
            selected.getScheduledTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "N/A").append("\n");
        details.append("Длительность: ").append(selected.getDurationMinutes() != null ? selected.getDurationMinutes() + " мин" : "N/A").append("\n");
        details.append("Статус: ").append(translateStatus(selected.getStatus())).append("\n");
        details.append("Цена: ").append(selected.getPrice() != null ? selected.getPrice() + " ₽" : "N/A").append("\n\n");
        
        if (selected.getNotes() != null && !selected.getNotes().isEmpty()) {
            details.append("Заметки: ").append(selected.getNotes()).append("\n");
        }
        if (selected.getHomework() != null && !selected.getHomework().isEmpty()) {
            details.append("Домашнее задание: ").append(selected.getHomework());
        }
        
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
    
    private void confirmLesson() {
        LessonDTO selected = lessonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите занятие");
            return;
        }
        
        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            showWarning("Подтверждать можно только заявки в статусе 'Согласование преподавателем'");
            return;
        }
        
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient()
                    .post("/tutor/lessons/" + selected.getId() + "/confirm", null);
                
                Platform.runLater(() -> {
                    showInfo("Занятие подтверждено преподавателем");
                    loadMyLessons(statusFilter.getValue());
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void completeLesson() {
        LessonDTO selected = lessonsTable.getSelectionModel().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите занятие");
            return;
        }
        
        if (!"CONFIRMED".equalsIgnoreCase(selected.getStatus())) {
            showWarning("Завершать можно только подтвержденные занятия");
            return;
        }
        
        // Диалог для ввода заметок и ДЗ
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Завершение занятия");
        dialog.setHeaderText("Занятие #" + selected.getId());
        
        ButtonType completeButtonType = new ButtonType("Завершить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(completeButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Заметки о занятии...");
        notesArea.setPrefRowCount(3);
        notesArea.setText(selected.getNotes() != null ? selected.getNotes() : "");
        
        TextArea homeworkArea = new TextArea();
        homeworkArea.setPromptText("Домашнее задание...");
        homeworkArea.setPrefRowCount(3);
        homeworkArea.setText(selected.getHomework() != null ? selected.getHomework() : "");
        
        grid.add(new Label("Заметки:"), 0, 0);
        grid.add(notesArea, 0, 1);
        grid.add(new Label("Домашнее задание:"), 0, 2);
        grid.add(homeworkArea, 0, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == completeButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("notes", notesArea.getText());
                result.put("homework", homeworkArea.getText());
                return result;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(data -> {
            new Thread(() -> {
                try {
                    Session.getInstance().getApiClient()
                        .put("/tutor/lessons/" + selected.getId() + "/complete", data);
                    
                    Platform.runLater(() -> {
                        showInfo("Занятие успешно завершено!");
                        loadMyLessons(statusFilter.getValue());
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка: " + ex.getMessage());
                        alert.showAndWait();
                    });
                }
            }).start();
        });
    }
    
    // ... остальная часть класса (расписание, профиль и т.д.) без изменений ...
}

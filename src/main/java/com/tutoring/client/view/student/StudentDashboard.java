package com.tutoring.client.view.student;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.*;
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

import java.math.BigDecimal;
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
        
        Button profileBtn = new Button("Мой профиль");
        profileBtn.setPrefWidth(180);
        profileBtn.setOnAction(e -> showProfile());
        
        menu.getChildren().addAll(searchTutorsBtn, myLessonsBtn, profileBtn);
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
                
                Gson gson = GsonProvider.getGson();
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
        
        BookLessonDialog dialog = new BookLessonDialog(selectedTutor);
        dialog.show().ifPresent(bookingData -> {
            new Thread(() -> {
                try {
                    // Расчет цены на основе длительности и ставки репетитора
                    BigDecimal hourlyRate = selectedTutor.getHourlyRate() != null ? 
                        selectedTutor.getHourlyRate() : BigDecimal.valueOf(1000);
                    BigDecimal price = hourlyRate.multiply(
                        BigDecimal.valueOf(bookingData.getDuration())
                    ).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                    
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("tutorId", selectedTutor.getId());
                    requestBody.put("subjectId", bookingData.getSubject().getId());
                    requestBody.put("scheduledTime", bookingData.getScheduledTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                    requestBody.put("durationMinutes", bookingData.getDuration());
                    requestBody.put("price", price.toString());
                    // notes можно оставить для будущего использования
                    if (bookingData.getNotes() != null && !bookingData.getNotes().trim().isEmpty()) {
                        requestBody.put("notes", bookingData.getNotes());
                    }
                    
                    System.out.println("[DEBUG] Бронируем занятие: " + requestBody);
                    
                    String response = Session.getInstance().getApiClient()
                        .post("/student/lessons/book", requestBody, String.class);
                    
                    System.out.println("[DEBUG] Ответ от сервера: " + response);
                    
                    Platform.runLater(() -> {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Успех");
                        successAlert.setHeaderText("Занятие забронировано!");
                        successAlert.setContentText(
                            "Занятие успешно забронировано.\n" +
                            "Репетитор: " + selectedTutor.getFullName() + "\n" +
                            "Предмет: " + bookingData.getSubject().getName() + "\n" +
                            "Время: " + bookingData.getScheduledTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "\n" +
                            "Длительность: " + bookingData.getDuration() + " мин\n" +
                            "Стоимость: " + price + " ₽"
                        );
                        successAlert.showAndWait();
                    });
                    
                } catch (Exception ex) {
                    System.err.println("[ERROR] Ошибка при бронировании:");
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Ошибка");
                        errorAlert.setHeaderText("Ошибка бронирования");
                        errorAlert.setContentText("Не удалось забронировать занятие.\n\nОшибка 500: " + ex.getMessage());
                        errorAlert.showAndWait();
                    });
                }
            }).start();
        });
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
                data.getValue().getStatus() != null ? translateStatus(data.getValue().getStatus()) : "N/A"
            )
        );
        
        lessonTable.getColumns().addAll(tutorCol, subjectCol, timeCol, statusCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadMyLessons());
        
        content.getChildren().addAll(titleLabel, lessonTable, refreshButton);
        view.setCenter(content);
        
        loadMyLessons();
    }

    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "На согласовании преподавателем";
            case "CONFIRMED": return "Подтверждено преподавателем";
            case "COMPLETED": return "Завершено";
            case "CANCELLED": return "Отклонено";
            default: return status;
        }
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
                
                Gson gson = GsonProvider.getGson();
                
                LessonDTO[] lessonsArray = gson.fromJson(response, LessonDTO[].class);
                List<LessonDTO> lessons = lessonsArray != null ? Arrays.asList(lessonsArray) : new ArrayList<>();
                
                System.out.println("[DEBUG] Распарсено " + lessons.size() + " занятий");
                
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
    
    private void showProfile() {
        // Показываем загрузчик
        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        Label loadingLabel = new Label("Загрузка профиля...");
        loadingLabel.setFont(new Font(16));
        ProgressIndicator progress = new ProgressIndicator();
        loadingBox.getChildren().addAll(progress, loadingLabel);
        view.setCenter(loadingBox);
        
        // Загружаем данные профиля с сервера
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient().get("/student/profile", String.class);
                Gson gson = GsonProvider.getGson();
                JsonObject profileData = gson.fromJson(response, JsonObject.class);
                
                Platform.runLater(() -> displayProfile(profileData));
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка загрузки профиля");
                    alert.setContentText("Не удалось загрузить данные профиля: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void displayProfile(JsonObject profileData) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #f9f9f9;");
        
        // Заголовок
        Label titleLabel = new Label("Мой профиль");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");
        
        // Карточка профиля
        VBox profileCard = new VBox(15);
        profileCard.setPadding(new Insets(25));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        profileCard.setMaxWidth(600);
        
        String firstName = getJsonString(profileData, "firstName");
        String lastName = getJsonString(profileData, "lastName");
        String fullName = firstName + " " + lastName;
        
        // Полное имя
        Label nameLabel = new Label(fullName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        // Роль
        Label roleLabel = new Label("Роль: Студент");
        roleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Separator separator1 = new Separator();
        
        // Основная информация
        Label basicInfoHeader = new Label("Основная информация");
        basicInfoHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        basicInfoHeader.setStyle("-fx-text-fill: #333;");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));
        
        int row = 0;
        
        // Username
        addInfoRow(infoGrid, row++, "Имя пользователя:", getJsonString(profileData, "username"));
        
        // Email
        addInfoRow(infoGrid, row++, "Email:", getJsonString(profileData, "email"));
        
        // Телефон
        String phone = getJsonString(profileData, "phoneNumber");
        if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) {
            addInfoRow(infoGrid, row++, "Телефон:", phone);
        }
        
        // Имя
        addInfoRow(infoGrid, row++, "Имя:", firstName);
        
        // Фамилия
        addInfoRow(infoGrid, row++, "Фамилия:", lastName);
        
        profileCard.getChildren().addAll(nameLabel, roleLabel, separator1, basicInfoHeader, infoGrid);
        
        // Дополнительная информация студента
        String educationLevel = getJsonString(profileData, "educationLevel");
        String learningGoals = getJsonString(profileData, "learningGoals");
        
        if ((educationLevel != null && !educationLevel.equals("N/A")) || 
            (learningGoals != null && !learningGoals.equals("N/A"))) {
            
            Separator separator2 = new Separator();
            profileCard.getChildren().add(separator2);
            
            Label studentInfoHeader = new Label("Информация об обучении");
            studentInfoHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
            studentInfoHeader.setStyle("-fx-text-fill: #333;");
            profileCard.getChildren().add(studentInfoHeader);
            
            GridPane studentGrid = new GridPane();
            studentGrid.setHgap(15);
            studentGrid.setVgap(12);
            studentGrid.setPadding(new Insets(10, 0, 0, 0));
            
            int sRow = 0;
            
            if (educationLevel != null && !educationLevel.equals("N/A")) {
                String levelRu = translateEducationLevel(educationLevel);
                addInfoRow(studentGrid, sRow++, "Уровень образования:", levelRu);
            }
            
            if (learningGoals != null && !learningGoals.equals("N/A")) {
                addInfoRow(studentGrid, sRow++, "Цели обучения:", learningGoals);
            }
            
            profileCard.getChildren().add(studentGrid);
        }
        
        // Кнопки действий
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button editButton = new Button("Редактировать профиль");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        editButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText("Функционал в разработке");
            alert.setContentText("Редактирование профиля будет добавлено в следующей версии.");
            alert.showAndWait();
        });
        
        Button changePasswordButton = new Button("Изменить пароль");
        changePasswordButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        changePasswordButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация");
            alert.setHeaderText("Функционал в разработке");
            alert.setContentText("Изменение пароля будет добавлено в следующей версии.");
            alert.showAndWait();
        });
        
        buttonBox.getChildren().addAll(editButton, changePasswordButton);
        
        content.getChildren().addAll(titleLabel, profileCard, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f9f9f9;");
        
        view.setCenter(scrollPane);
    }
    
    private String getJsonString(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return "N/A";
    }
    
    private String translateEducationLevel(String level) {
        if (level == null) return "N/A";
        switch (level) {
            case "ELEMENTARY": return "Начальное";
            case "MIDDLE_SCHOOL": return "Средняя школа";
            case "HIGH_SCHOOL": return "Старшая школа";
            case "UNDERGRADUATE": return "Бакалавриат";
            case "GRADUATE": return "Магистратура";
            case "PROFESSIONAL": return "Профессиональное";
            default: return level;
        }
    }
    
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        
        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setStyle("-fx-text-fill: #333;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(350);
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
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

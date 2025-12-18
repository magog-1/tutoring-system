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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class StudentDashboard {
    private BorderPane view;
    private Stage primaryStage;
    private TableView<TutorDTO> tutorTable;
    private TableView<LessonDTO> lessonTable;
    private List<LessonDTO> allLessons = new ArrayList<>();
    private LocalDate selectedDate = LocalDate.now();
    private YearMonth currentYearMonth = YearMonth.now();
    
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
        // Загружаем занятия, если еще не загружены
        if (allLessons.isEmpty()) {
            new Thread(() -> {
                try {
                    String response = Session.getInstance().getApiClient().get("/student/lessons", String.class);
                    if (response != null && !response.trim().isEmpty()) {
                        Gson gson = GsonProvider.getGson();
                        LessonDTO[] lessonsArray = gson.fromJson(response, LessonDTO[].class);
                        allLessons = lessonsArray != null ? Arrays.asList(lessonsArray) : new ArrayList<>();
                        Platform.runLater(this::displaySchedule);
                    } else {
                        Platform.runLater(this::displaySchedule);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(this::displaySchedule);
                }
            }).start();
        } else {
            displaySchedule();
        }
    }

    private void displaySchedule() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f9f9f9;");

        Label titleLabel = new Label("Расписание занятий");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");

        HBox navigationBox = new HBox(15);
        navigationBox.setAlignment(Pos.CENTER);
        navigationBox.setPadding(new Insets(10));

        Button prevMonthBtn = new Button("◄ Предыдущий");
        prevMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            displaySchedule();
        });

        Label monthLabel = new Label(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")) + " " + currentYearMonth.getYear());
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        monthLabel.setStyle("-fx-text-fill: #333;");
        monthLabel.setPrefWidth(200);
        monthLabel.setAlignment(Pos.CENTER);

        Button nextMonthBtn = new Button("Следующий ►");
        nextMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            displaySchedule();
        });

        Button todayBtn = new Button("Сегодня");
        todayBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        todayBtn.setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            displaySchedule();
        });

        navigationBox.getChildren().addAll(prevMonthBtn, monthLabel, nextMonthBtn, todayBtn);

        GridPane calendar = createCalendarGrid();
        VBox dayInfoBox = createDayInfoBox();

        content.getChildren().addAll(titleLabel, navigationBox, calendar, dayInfoBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f9f9f9;");

        view.setCenter(scrollPane);
    }

    private GridPane createCalendarGrid() {
        GridPane calendar = new GridPane();
        calendar.setHgap(5);
        calendar.setVgap(5);
        calendar.setPadding(new Insets(10));
        calendar.setAlignment(Pos.CENTER);
        calendar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setStyle("-fx-text-fill: #666; -fx-alignment: center;");
            dayLabel.setPrefWidth(80);
            dayLabel.setAlignment(Pos.CENTER);
            calendar.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox dayCell = createDayCell(date);
            calendar.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        return calendar;
    }

    private VBox createDayCell(LocalDate date) {
        VBox cell = new VBox(3);
        cell.setPrefSize(80, 70);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setPadding(new Insets(5));

        String baseStyle = "-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;";

        long lessonsCount = allLessons.stream()
                .filter(l -> l.getScheduledTime() != null && l.getScheduledTime().toLocalDate().equals(date))
                .count();

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean isPast = date.isBefore(LocalDate.now());

        if (isSelected) {
            cell.setStyle(baseStyle + " -fx-background-color: #2196F3; -fx-cursor: hand;");
        } else if (isToday) {
            cell.setStyle(baseStyle + " -fx-background-color: #E3F2FD; -fx-cursor: hand;");
        } else if (lessonsCount > 0) {
            cell.setStyle(baseStyle + " -fx-background-color: #FFF9C4; -fx-cursor: hand;");
        } else if (isPast) {
            cell.setStyle(baseStyle + " -fx-background-color: #f5f5f5; -fx-cursor: hand;");
        } else {
            cell.setStyle(baseStyle + " -fx-background-color: white; -fx-cursor: hand;");
        }

        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setFont(Font.font("System", FontWeight.BOLD, 14));
        dayNumber.setStyle("-fx-text-fill: " + (isSelected ? "white" : isPast ? "#999" : "#333") + ";");

        cell.getChildren().add(dayNumber);

        if (lessonsCount > 0) {
            Label lessonsLabel = new Label(lessonsCount + " зан.");
            lessonsLabel.setFont(new Font(9));
            lessonsLabel.setStyle("-fx-text-fill: " + (isSelected ? "white" : "#FF9800") + "; -fx-font-weight: bold;");
            cell.getChildren().add(lessonsLabel);
        }

        cell.setOnMouseClicked(e -> {
            selectedDate = date;
            displaySchedule();
        });

        return cell;
    }

    private VBox createDayInfoBox() {
        VBox dayInfo = new VBox(10);
        dayInfo.setPadding(new Insets(15));
        dayInfo.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        Label dateLabel = new Label("Занятия на " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"))));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        dateLabel.setStyle("-fx-text-fill: #333;");

        dayInfo.getChildren().add(dateLabel);

        List<LessonDTO> dayLessons = allLessons.stream()
                .filter(l -> l.getScheduledTime() != null && l.getScheduledTime().toLocalDate().equals(selectedDate))
                .sorted(Comparator.comparing(LessonDTO::getScheduledTime))
                .collect(Collectors.toList());

        if (dayLessons.isEmpty()) {
            Label noLessonsLabel = new Label("Нет занятий на этот день");
            noLessonsLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            dayInfo.getChildren().add(noLessonsLabel);
        } else {
            for (LessonDTO lesson : dayLessons) {
                VBox lessonCard = createLessonCard(lesson);
                dayInfo.getChildren().add(lessonCard);
            }
        }

        return dayInfo;
    }

    private VBox createLessonCard(LessonDTO lesson) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #fafafa; -fx-background-radius: 5;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(lesson.getScheduledTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        timeLabel.setStyle("-fx-text-fill: #2196F3;");

        Label durationLabel = new Label("(" + lesson.getDurationMinutes() + " мин)");
        durationLabel.setStyle("-fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(translateStatus(lesson.getStatus()));
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 3; " + getStatusStyle(lesson.getStatus()));

        headerBox.getChildren().addAll(timeLabel, durationLabel, spacer, statusLabel);

        Label tutorLabel = new Label("Репетитор: " + (lesson.getTutor() != null ? lesson.getTutor().getFullName() : "N/A"));
        tutorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label subjectLabel = new Label("Предмет: " + (lesson.getSubject() != null ? lesson.getSubject().getName() : "N/A"));
        subjectLabel.setStyle("-fx-text-fill: #666;");

        Label priceLabel = new Label("Стоимость: " + (lesson.getPrice() != null ? lesson.getPrice() + " ₽" : "N/A"));
        priceLabel.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(headerBox, tutorLabel, subjectLabel, priceLabel);

        return card;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";

        switch (status.toUpperCase()) {
            case "PENDING":
                return "-fx-background-color: #FFF9C4; -fx-text-fill: #F57C00;";
            case "CONFIRMED":
                return "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
            case "COMPLETED":
                return "-fx-background-color: #E8F5E9; -fx-text-fill: #388E3C;";
            case "CANCELLED":
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";
        }
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
    
    private void showProfile() {
        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        Label loadingLabel = new Label("Загрузка профиля...");
        loadingLabel.setFont(new Font(16));
        ProgressIndicator progress = new ProgressIndicator();
        loadingBox.getChildren().addAll(progress, loadingLabel);
        view.setCenter(loadingBox);
        
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
        
        Label titleLabel = new Label("Мой профиль");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");
        
        VBox profileCard = new VBox(15);
        profileCard.setPadding(new Insets(25));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        profileCard.setMaxWidth(600);
        
        String firstName = getJsonString(profileData, "firstName");
        String lastName = getJsonString(profileData, "lastName");
        String fullName = firstName + " " + lastName;
        
        Label nameLabel = new Label(fullName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        
        Label roleLabel = new Label("Роль: Студент");
        roleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Separator separator1 = new Separator();
        
        Label basicInfoHeader = new Label("Основная информация");
        basicInfoHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        basicInfoHeader.setStyle("-fx-text-fill: #333;");
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);
        infoGrid.setPadding(new Insets(10, 0, 0, 0));
        
        int row = 0;
        addInfoRow(infoGrid, row++, "Имя пользователя:", getJsonString(profileData, "username"));
        addInfoRow(infoGrid, row++, "Email:", getJsonString(profileData, "email"));
        
        String phone = getJsonString(profileData, "phoneNumber");
        if (phone != null && !phone.isEmpty() && !phone.equals("N/A")) {
            addInfoRow(infoGrid, row++, "Телефон:", phone);
        }
        
        addInfoRow(infoGrid, row++, "Имя:", firstName);
        addInfoRow(infoGrid, row++, "Фамилия:", lastName);
        
        profileCard.getChildren().addAll(nameLabel, roleLabel, separator1, basicInfoHeader, infoGrid);
        
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

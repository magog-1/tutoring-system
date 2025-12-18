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
        statusFilter.getItems().addAll("Все", "Запланировано", "Завершено", "Отменено");
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




        Button completeButton = new Button("Завершить");
        completeButton.setOnAction(e -> completeLesson());

        buttonBox.getChildren().addAll(refreshButton, viewDetailsButton, completeButton);

        content.getChildren().addAll(titleLabel, filterBox, lessonsTable, buttonBox);
        view.setCenter(content);

        // Загрузка данных
        loadMyLessons("Все");
    }

    private String translateStatus(String status) {
        switch (status.toUpperCase()) {
            case "PENDING": return "Ожидает";
            case "CONFIRMED": return "Подтверждено";
            case "SCHEDULED": return "Запланировано";
            case "COMPLETED": return "Завершено";
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
                    String statusEn = statusFilter.equals("Запланировано") ? "SCHEDULED" :
                            statusFilter.equals("Завершено") ? "COMPLETED" : "CANCELLED";
                    lessons = lessons.stream()
                            .filter(l -> statusEn.equalsIgnoreCase(l.getStatus()))
                            .toList();
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

    private void completeLesson() {
        LessonDTO selected = lessonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Выберите занятие");
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(selected.getStatus())) {
            showWarning("Занятие уже завершено");
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
                    // Передаем Map напрямую - ApiClient сам сериализует в JSON
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

    private void showSchedule() {
        // Загружаем занятия, если еще не загружены
        if (allLessons.isEmpty()) {
            new Thread(() -> {
                try {
                    String response = Session.getInstance().getApiClient().get("/tutor/lessons", String.class);
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

        // Заголовок
        Label titleLabel = new Label("Расписание занятий");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #4CAF50;");

        // Навигация по месяцам
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
        todayBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        todayBtn.setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            displaySchedule();
        });

        navigationBox.getChildren().addAll(prevMonthBtn, monthLabel, nextMonthBtn, todayBtn);

        // Календарная сетка
        GridPane calendar = createCalendarGrid();

        // Информация о выбранном дне
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

        // Заголовки дней недели
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            dayLabel.setStyle("-fx-text-fill: #666; -fx-alignment: center;");
            dayLabel.setPrefWidth(80);
            dayLabel.setAlignment(Pos.CENTER);
            calendar.add(dayLabel, i, 0);
        }

        // Получаем первый день месяца и количество дней
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

        // Заполняем календарь
        int row = 1;
        int col = dayOfWeek - 1; // Начинаем с понедельника

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

        // Стиль по умолчанию
        String baseStyle = "-fx-border-color: #ddd; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;";

        // Подсчет занятий на этот день
        long lessonsCount = allLessons.stream()
                .filter(l -> l.getScheduledTime() != null && l.getScheduledTime().toLocalDate().equals(date))
                .count();

        // Стиль в зависимости от даты
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean isPast = date.isBefore(LocalDate.now());

        if (isSelected) {
            cell.setStyle(baseStyle + " -fx-background-color: #4CAF50; -fx-cursor: hand;");
        } else if (isToday) {
            cell.setStyle(baseStyle + " -fx-background-color: #E8F5E9; -fx-cursor: hand;");
        } else if (lessonsCount > 0) {
            cell.setStyle(baseStyle + " -fx-background-color: #FFF9C4; -fx-cursor: hand;");
        } else if (isPast) {
            cell.setStyle(baseStyle + " -fx-background-color: #f5f5f5; -fx-cursor: hand;");
        } else {
            cell.setStyle(baseStyle + " -fx-background-color: white; -fx-cursor: hand;");
        }

        // Номер дня
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setFont(Font.font("System", FontWeight.BOLD, 14));
        dayNumber.setStyle("-fx-text-fill: " + (isSelected ? "white" : isPast ? "#999" : "#333") + ";");

        cell.getChildren().add(dayNumber);

        // Индикатор занятий
        if (lessonsCount > 0) {
            Label lessonsLabel = new Label(lessonsCount + " зан.");
            lessonsLabel.setFont(new Font(9));
            lessonsLabel.setStyle("-fx-text-fill: " + (isSelected ? "white" : "#FF9800") + "; -fx-font-weight: bold;");
            cell.getChildren().add(lessonsLabel);
        }

        // Обработка клика
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

        // Заголовок с выбранной датой
        Label dateLabel = new Label("Занятия на " + selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"))));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        dateLabel.setStyle("-fx-text-fill: #333;");

        dayInfo.getChildren().add(dateLabel);

        // Список занятий на выбранный день
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

        // Время и статус
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(lesson.getScheduledTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        timeLabel.setStyle("-fx-text-fill: #4CAF50;");

        Label durationLabel = new Label("(" + lesson.getDurationMinutes() + " мин)");
        durationLabel.setStyle("-fx-text-fill: #666;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(translateStatus(lesson.getStatus()));
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 6; -fx-background-radius: 3; " + getStatusStyle(lesson.getStatus()));

        headerBox.getChildren().addAll(timeLabel, durationLabel, spacer, statusLabel);

        // Студент и предмет
        Label studentLabel = new Label("Студент: " + (lesson.getStudent() != null ? lesson.getStudent().getFullName() : "N/A"));
        studentLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Label subjectLabel = new Label("Предмет: " + (lesson.getSubject() != null ? lesson.getSubject().getName() : "N/A"));
        subjectLabel.setStyle("-fx-text-fill: #666;");

        Label priceLabel = new Label("Стоимость: " + (lesson.getPrice() != null ? lesson.getPrice() + " ₽" : "N/A"));
        priceLabel.setStyle("-fx-text-fill: #666;");

        card.getChildren().addAll(headerBox, studentLabel, subjectLabel, priceLabel);

        return card;
    }

    private String getStatusStyle(String status) {
        if (status == null) return "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";

        switch (status.toUpperCase()) {
            case "PENDING":
                return "-fx-background-color: #FFF9C4; -fx-text-fill: #F57C00;";
            case "CONFIRMED":
            case "SCHEDULED":
                return "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;";
            case "COMPLETED":
                return "-fx-background-color: #E8F5E9; -fx-text-fill: #388E3C;";
            case "CANCELLED":
                return "-fx-background-color: #FFEBEE; -fx-text-fill: #D32F2F;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #666;";
        }
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
                String response = Session.getInstance().getApiClient().get("/tutor/profile", String.class);
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
        titleLabel.setStyle("-fx-text-fill: #4CAF50;");

        // Карточка профиля
        VBox profileCard = new VBox(15);
        profileCard.setPadding(new Insets(25));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        profileCard.setMaxWidth(700);

        String firstName = getJsonString(profileData, "firstName");
        String lastName = getJsonString(profileData, "lastName");
        String fullName = firstName + " " + lastName;

        // Полное имя
        Label nameLabel = new Label(fullName);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Роль и статус
        HBox roleBox = new HBox(15);
        roleBox.setAlignment(Pos.CENTER_LEFT);

        Label roleLabel = new Label("Роль: Репетитор");
        roleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold;");

        boolean isVerified = profileData.has("isVerified") && profileData.get("isVerified").getAsBoolean();
        if (isVerified) {
            Label verifiedLabel = new Label("✓ Подтвержден");
            verifiedLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-color: #E3F2FD; -fx-padding: 3 8; -fx-background-radius: 3;");
            roleBox.getChildren().addAll(roleLabel, verifiedLabel);
        } else {
            roleBox.getChildren().add(roleLabel);
        }

        // Рейтинг
        if (profileData.has("rating") && !profileData.get("rating").isJsonNull()) {
            double rating = profileData.get("rating").getAsDouble();
            int totalReviews = profileData.has("totalReviews") ? profileData.get("totalReviews").getAsInt() : 0;
            Label ratingLabel = new Label(String.format("★ %.1f (%d %s)", rating, totalReviews,
                    totalReviews == 1 ? "отзыв" : "отзывов"));
            ratingLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 14px; -fx-font-weight: bold;");
            roleBox.getChildren().add(ratingLabel);
        }

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

        profileCard.getChildren().addAll(nameLabel, roleBox, separator1, basicInfoHeader, infoGrid);

        // Профессиональная информация
        Separator separator2 = new Separator();
        profileCard.getChildren().add(separator2);

        Label professionalInfoHeader = new Label("Профессиональная информация");
        professionalInfoHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        professionalInfoHeader.setStyle("-fx-text-fill: #333;");
        profileCard.getChildren().add(professionalInfoHeader);

        GridPane professionalGrid = new GridPane();
        professionalGrid.setHgap(15);
        professionalGrid.setVgap(12);
        professionalGrid.setPadding(new Insets(10, 0, 0, 0));

        int pRow = 0;

        // Образование
        String education = getJsonString(profileData, "education");
        if (education != null && !education.equals("N/A")) {
            addInfoRow(professionalGrid, pRow++, "Образование:", education);
        }

        // Опыт работы
        if (profileData.has("experienceYears") && !profileData.get("experienceYears").isJsonNull()) {
            int experience = profileData.get("experienceYears").getAsInt();
            addInfoRow(professionalGrid, pRow++, "Опыт работы:", experience + " " + getYearsWord(experience));
        }

        // Ставка
        if (profileData.has("hourlyRate") && !profileData.get("hourlyRate").isJsonNull()) {
            String rate = profileData.get("hourlyRate").getAsString();
            addInfoRow(professionalGrid, pRow++, "Ставка:", rate + " ₽/час");
        }

        // Предметы
        if (profileData.has("subjects") && profileData.get("subjects").isJsonArray()) {
            JsonArray subjects = profileData.getAsJsonArray("subjects");
            if (subjects.size() > 0) {
                StringBuilder subjectsList = new StringBuilder();
                for (int i = 0; i < subjects.size(); i++) {
                    JsonObject subject = subjects.get(i).getAsJsonObject();
                    if (subject.has("name")) {
                        if (i > 0) subjectsList.append(", ");
                        subjectsList.append(subject.get("name").getAsString());
                    }
                }
                if (subjectsList.length() > 0) {
                    addInfoRow(professionalGrid, pRow++, "Предметы:", subjectsList.toString());
                }
            }
        }

        profileCard.getChildren().add(professionalGrid);

        // О себе
        String bio = getJsonString(profileData, "bio");
        if (bio != null && !bio.equals("N/A") && !bio.isEmpty()) {
            Separator separator3 = new Separator();
            profileCard.getChildren().add(separator3);

            Label bioHeader = new Label("О себе");
            bioHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
            bioHeader.setStyle("-fx-text-fill: #333;");

            Label bioText = new Label(bio);
            bioText.setWrapText(true);
            bioText.setMaxWidth(650);
            bioText.setStyle("-fx-text-fill: #555; -fx-padding: 10 0 0 0;");

            profileCard.getChildren().addAll(bioHeader, bioText);
        }

        // Кнопки действий
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button editButton = new Button("Редактировать профиль");
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
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

    private String getYearsWord(int years) {
        if (years % 10 == 1 && years % 100 != 11) {
            return "год";
        } else if (years % 10 >= 2 && years % 10 <= 4 && (years % 100 < 10 || years % 100 >= 20)) {
            return "года";
        } else {
            return "лет";
        }
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setStyle("-fx-text-fill: #333;");
        valueNode.setWrapText(true);
        valueNode.setMaxWidth(450);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.showAndWait();
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
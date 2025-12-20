package com.tutoring.client.view.manager;

import com.google.gson.Gson;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.SubjectDTO;
import com.tutoring.client.model.TutorDTO;
import com.tutoring.client.model.UserDTO;
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

import java.util.*;

public class ManagerDashboard {
    private BorderPane view;
    private Stage primaryStage;
    private TableView<TutorDTO> pendingTutorsTable;
    private TableView<SubjectDTO> subjectsTable;
    private TableView<UserDTO> usersTable;
    
    public ManagerDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new BorderPane();
        
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #FF9800;");
        
        Label titleLabel = new Label("Панель менеджера");
        titleLabel.setFont(new Font(20));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> logout());
        
        topBox.getChildren().addAll(titleLabel, logoutButton);
        view.setTop(topBox);
        
        VBox leftMenu = createMenu();
        view.setLeft(leftMenu);
        
        showPendingTutors();
    }
    
    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(15));
        menu.setStyle("-fx-background-color: #f5f5f5;");
        menu.setPrefWidth(200);
        
        Button verifyBtn = new Button("Верификация");
        verifyBtn.setPrefWidth(180);
        verifyBtn.setOnAction(e -> showPendingTutors());
        
        Button subjectsBtn = new Button("Предметы");
        subjectsBtn.setPrefWidth(180);
        subjectsBtn.setOnAction(e -> showSubjects());
        
        Button usersBtn = new Button("Пользователи");
        usersBtn.setPrefWidth(180);
        usersBtn.setOnAction(e -> showUsers());
        
        menu.getChildren().addAll(verifyBtn, subjectsBtn, usersBtn);
        return menu;
    }
    
    private void showPendingTutors() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Репетиторы, ожидающие верификации");
        titleLabel.setFont(new Font(18));
        
        pendingTutorsTable = new TableView<>();
        
        TableColumn<TutorDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<TutorDTO, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                (data.getValue().getFirstName() != null ? data.getValue().getFirstName() : "") + " " + 
                (data.getValue().getLastName() != null ? data.getValue().getLastName() : "")
            )
        );
        
        TableColumn<TutorDTO, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<TutorDTO, String> educationCol = new TableColumn<>("Образование");
        educationCol.setCellValueFactory(new PropertyValueFactory<>("education"));
        
        pendingTutorsTable.getColumns().addAll(idCol, nameCol, emailCol, educationCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadPendingTutors());
        
        Button verifyButton = new Button("Верифицировать");
        verifyButton.setOnAction(e -> verifyTutor());
        
        HBox buttonBox = new HBox(10, refreshButton, verifyButton);
        
        content.getChildren().addAll(titleLabel, pendingTutorsTable, buttonBox);
        view.setCenter(content);
        
        loadPendingTutors();
    }
    
    private void loadPendingTutors() {
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] Запрашиваем /manager/tutors/pending...");
                String response = Session.getInstance().getApiClient()
                    .get("/manager/tutors/pending", String.class);
                System.out.println("[DEBUG] Получен ответ: " + response);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> pendingTutorsTable.setItems(FXCollections.observableArrayList()));
                    return;
                }
                
                Gson gson = GsonProvider.getGson();
                
                TutorDTO[] tutorsArray = gson.fromJson(response, TutorDTO[].class);
                List<TutorDTO> tutors = tutorsArray != null ? Arrays.asList(tutorsArray) : new ArrayList<>();
                System.out.println("[DEBUG] Распарсено " + tutors.size() + " репетиторов");
                
                Platform.runLater(() -> {
                    ObservableList<TutorDTO> data = FXCollections.observableArrayList(tutors);
                    pendingTutorsTable.setItems(data);
                });
            } catch (Exception ex) {
                System.err.println("[ERROR] Ошибка при загрузке:");
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void verifyTutor() {
        TutorDTO selected = pendingTutorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Выберите репетитора");
            alert.showAndWait();
            return;
        }
        
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient()
                    .put("/manager/tutors/" + selected.getId() + "/verify", null);
                
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Репетитор верифицирован!");
                    alert.showAndWait();
                    loadPendingTutors();
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
    
    private void showSubjects() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Управление предметами");
        titleLabel.setFont(new Font(18));
        
        subjectsTable = new TableView<>();
        subjectsTable.setPrefHeight(400);
        
        TableColumn<SubjectDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<SubjectDTO, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<SubjectDTO, String> descCol = new TableColumn<>("Описание");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);
        
        TableColumn<SubjectDTO, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);
        
        subjectsTable.getColumns().addAll(idCol, nameCol, descCol, categoryCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadSubjects());
        
        Button createButton = new Button("Создать");
        createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        createButton.setOnAction(e -> createSubject());
        
        Button editButton = new Button("Редактировать");
        editButton.setOnAction(e -> editSubject());
        
        Button deleteButton = new Button("Удалить");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSubject());
        
        HBox buttonBox = new HBox(10, refreshButton, createButton, editButton, deleteButton);
        
        content.getChildren().addAll(titleLabel, subjectsTable, buttonBox);
        view.setCenter(content);
        
        loadSubjects();
    }
    
    private void loadSubjects() {
        new Thread(() -> {
            try {
                String response = Session.getInstance().getApiClient()
                    .get("/subjects", String.class);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> subjectsTable.setItems(FXCollections.observableArrayList()));
                    return;
                }
                
                Gson gson = GsonProvider.getGson();
                SubjectDTO[] subjectsArray = gson.fromJson(response, SubjectDTO[].class);
                List<SubjectDTO> subjects = subjectsArray != null ? Arrays.asList(subjectsArray) : new ArrayList<>();
                
                Platform.runLater(() -> {
                    ObservableList<SubjectDTO> data = FXCollections.observableArrayList(subjects);
                    subjectsTable.setItems(data);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка загрузки предметов: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }
    
    private void createSubject() {
        Dialog<SubjectDTO> dialog = new Dialog<>();
        dialog.setTitle("Создать предмет");
        dialog.setHeaderText("Введите данные нового предмета");
        
        ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Название");
        TextArea descField = new TextArea();
        descField.setPromptText("Описание");
        descField.setPrefRowCount(3);
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("MATHEMATICS", "LANGUAGES", "SCIENCES", "ARTS", "OTHER");
        categoryBox.setValue("OTHER");
        
        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Категория:"), 0, 2);
        grid.add(categoryBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                SubjectDTO subject = new SubjectDTO();
                subject.setName(nameField.getText());
                subject.setDescription(descField.getText());
                subject.setCategory(categoryBox.getValue());
                return subject;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(subject -> {
            new Thread(() -> {
                try {
                    Gson gson = GsonProvider.getGson();
                    String json = gson.toJson(subject);
                    Session.getInstance().getApiClient()
                        .post("/manager/subjects", json);
                    
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Предмет создан!");
                        alert.showAndWait();
                        loadSubjects();
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
    
    private void editSubject() {
        SubjectDTO selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Выберите предмет для редактирования");
            alert.showAndWait();
            return;
        }
        
        Dialog<SubjectDTO> dialog = new Dialog<>();
        dialog.setTitle("Редактировать предмет");
        dialog.setHeaderText("Изменить данные предмета");
        
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(selected.getName());
        TextArea descField = new TextArea(selected.getDescription());
        descField.setPrefRowCount(3);
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("MATHEMATICS", "LANGUAGES", "SCIENCES", "ARTS", "OTHER");
        categoryBox.setValue(selected.getCategory() != null ? selected.getCategory() : "OTHER");
        
        grid.add(new Label("Название:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Категория:"), 0, 2);
        grid.add(categoryBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                SubjectDTO subject = new SubjectDTO();
                subject.setId(selected.getId());
                subject.setName(nameField.getText());
                subject.setDescription(descField.getText());
                subject.setCategory(categoryBox.getValue());
                return subject;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(subject -> {
            new Thread(() -> {
                try {
                    Gson gson = GsonProvider.getGson();
                    String json = gson.toJson(subject);
                    Session.getInstance().getApiClient()
                        .put("/manager/subjects/" + subject.getId(), json);
                    
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Предмет обновлён!");
                        alert.showAndWait();
                        loadSubjects();
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
    
    private void deleteSubject() {
        SubjectDTO selected = subjectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Выберите предмет для удаления");
            alert.showAndWait();
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
            "Вы уверены, что хотите удалить предмет \"" + selected.getName() + "\"?",
            ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        Session.getInstance().getApiClient()
                            .delete("/manager/subjects/" + selected.getId());
                        
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Предмет удалён!");
                            alert.showAndWait();
                            loadSubjects();
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
        });
    }
    
    private void showUsers() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Управление пользователями");
        titleLabel.setFont(new Font(18));
        
        // Фильтр по ролям
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label filterLabel = new Label("Фильтр по роли:");
        ComboBox<String> roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Все", "STUDENT", "TUTOR", "MANAGER");
        roleFilter.setValue("Все");
        Button applyFilterButton = new Button("Применить");
        applyFilterButton.setOnAction(e -> {
            String role = roleFilter.getValue().equals("Все") ? null : roleFilter.getValue();
            loadUsers(role);
        });
        filterBox.getChildren().addAll(filterLabel, roleFilter, applyFilterButton);
        
        usersTable = new TableView<>();
        usersTable.setPrefHeight(400);
        
        TableColumn<UserDTO, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<UserDTO, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<UserDTO, String> roleCol = new TableColumn<>("Роль");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);
        
        TableColumn<UserDTO, String> firstNameCol = new TableColumn<>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(120);
        
        TableColumn<UserDTO, String> lastNameCol = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(120);
        
        TableColumn<UserDTO, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(120);
        
        usersTable.getColumns().addAll(idCol, emailCol, roleCol, firstNameCol, lastNameCol, phoneCol);
        
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> loadUsers(null));
        
        HBox buttonBox = new HBox(10, refreshButton);
        
        content.getChildren().addAll(titleLabel, filterBox, usersTable, buttonBox);
        view.setCenter(content);
        
        loadUsers(null);
    }
    
    private void loadUsers(String role) {
        new Thread(() -> {
            try {
                String endpoint = "/manager/users";
                if (role != null && !role.isEmpty()) {
                    endpoint += "?role=" + role;
                }
                
                String response = Session.getInstance().getApiClient()
                    .get(endpoint, String.class);
                
                if (response == null || response.trim().isEmpty()) {
                    Platform.runLater(() -> usersTable.setItems(FXCollections.observableArrayList()));
                    return;
                }
                
                Gson gson = GsonProvider.getGson();
                UserDTO[] usersArray = gson.fromJson(response, UserDTO[].class);
                List<UserDTO> users = usersArray != null ? Arrays.asList(usersArray) : new ArrayList<>();
                
                Platform.runLater(() -> {
                    ObservableList<UserDTO> data = FXCollections.observableArrayList(users);
                    usersTable.setItems(data);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка загрузки пользователей: " + ex.getMessage());
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

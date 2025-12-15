package com.tutoring.client.view.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.TutorDTO;
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
import java.util.List;

public class ManagerDashboard {
    private BorderPane view;
    private Stage primaryStage;
    private TableView<TutorDTO> pendingTutorsTable;
    
    public ManagerDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }
    
    private void createView() {
        view = new BorderPane();
        
        // Top
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
        
        // Left menu
        VBox leftMenu = createMenu();
        view.setLeft(leftMenu);
        
        // Center
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
                data.getValue().getFirstName() + " " + data.getValue().getLastName()
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
                String response = Session.getInstance().getApiClient()
                    .get("/manager/tutors/pending", String.class);
                
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TutorDTO>>(){}.getType();
                List<TutorDTO> tutors = gson.fromJson(response, listType);
                
                Platform.runLater(() -> {
                    ObservableList<TutorDTO> data = FXCollections.observableArrayList(tutors);
                    pendingTutorsTable.setItems(data);
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
        Label label = new Label("Управление предметами - в разработке");
        label.setFont(new Font(18));
        VBox box = new VBox(label);
        box.setPadding(new Insets(20));
        view.setCenter(box);
    }
    
    private void showUsers() {
        Label label = new Label("Управление пользователями - в разработке");
        label.setFont(new Font(18));
        VBox box = new VBox(label);
        box.setPadding(new Insets(20));
        view.setCenter(box);
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

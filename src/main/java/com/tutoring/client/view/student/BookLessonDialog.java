package com.tutoring.client.view.student;

import com.google.gson.Gson;
import com.tutoring.client.api.GsonProvider;
import com.tutoring.client.api.Session;
import com.tutoring.client.model.SubjectDTO;
import com.tutoring.client.model.TutorDTO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BookLessonDialog {
    private final TutorDTO tutor;
    private List<SubjectDTO> allSubjects;
    
    public BookLessonDialog(TutorDTO tutor) {
        this.tutor = tutor;
        loadSubjects();
    }
    
    private void loadSubjects() {
        try {
            System.out.println("[DEBUG] Загрузка всех предметов...");
            String response = Session.getInstance().getApiClient().get("/subjects", String.class);
            
            if (response != null && !response.trim().isEmpty()) {
                Gson gson = GsonProvider.getGson();
                SubjectDTO[] subjectsArray = gson.fromJson(response, SubjectDTO[].class);
                allSubjects = subjectsArray != null ? Arrays.asList(subjectsArray) : new ArrayList<>();
                System.out.println("[DEBUG] Загружено " + allSubjects.size() + " предметов");
            } else {
                allSubjects = new ArrayList<>();
            }
        } catch (Exception ex) {
            System.err.println("[ERROR] Ошибка загрузки предметов: " + ex.getMessage());
            ex.printStackTrace();
            allSubjects = new ArrayList<>();
        }
    }
    
    public Optional<BookingData> show() {
        Dialog<BookingData> dialog = new Dialog<>();
        dialog.setTitle("Бронирование занятия");
        dialog.setHeaderText("Забронировать занятие с " + tutor.getFirstName() + " " + tutor.getLastName());
        
        ButtonType bookButtonType = new ButtonType("Забронировать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        int row = 0;
        
        // Информация о репетиторе
        Label tutorNameLabel = new Label("Репетитор:");
        Label tutorNameValue = new Label(tutor.getFirstName() + " " + tutor.getLastName());
        tutorNameValue.setStyle("-fx-font-weight: bold;");
        grid.add(tutorNameLabel, 0, row);
        grid.add(tutorNameValue, 1, row);
        row++;
        
        // Образование (если есть)
        if (tutor.getEducation() != null && !tutor.getEducation().isEmpty()) {
            Label educationLabel = new Label("Образование:");
            Label educationValue = new Label(tutor.getEducation());
            educationValue.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            educationValue.setWrapText(true);
            educationValue.setMaxWidth(300);
            grid.add(educationLabel, 0, row);
            grid.add(educationValue, 1, row);
            row++;
        }
        
        // Предмет
        ComboBox<SubjectDTO> subjectCombo = new ComboBox<>();
        if (allSubjects != null && !allSubjects.isEmpty()) {
            subjectCombo.getItems().addAll(allSubjects);
            subjectCombo.setValue(allSubjects.get(0));
            subjectCombo.setConverter(new javafx.util.StringConverter<SubjectDTO>() {
                @Override
                public String toString(SubjectDTO subject) {
                    return subject != null ? subject.getName() : "";
                }
                @Override
                public SubjectDTO fromString(String string) {
                    return null;
                }
            });
        } else {
            subjectCombo.setPromptText("Нет доступных предметов");
            subjectCombo.setDisable(true);
        }
        
        grid.add(new Label("Предмет:"), 0, row);
        grid.add(subjectCombo, 1, row);
        row++;
        
        // Дата
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(1));
        datePicker.setPromptText("Выберите дату");
        
        grid.add(new Label("Дата:"), 0, row);
        grid.add(datePicker, 1, row);
        row++;
        
        // Время
        Spinner<Integer> hourSpinner = new Spinner<>(8, 20, 10);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 45, 0, 15);
        
        javafx.scene.layout.HBox timeBox = new javafx.scene.layout.HBox(5);
        timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
        
        grid.add(new Label("Время:"), 0, row);
        grid.add(timeBox, 1, row);
        row++;
        
        // Продолжительность
        ComboBox<Integer> durationCombo = new ComboBox<>();
        durationCombo.getItems().addAll(45, 60, 90, 120);
        durationCombo.setValue(60);
        durationCombo.setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer duration) {
                return duration != null ? duration + " мин" : "";
            }
            @Override
            public Integer fromString(String string) {
                return null;
            }
        });
        
        grid.add(new Label("Продолжительность:"), 0, row);
        grid.add(durationCombo, 1, row);
        row++;
        
        // Примечания
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Дополнительные пожелания...");
        notesArea.setPrefRowCount(3);
        notesArea.setMaxWidth(300);
        
        grid.add(new Label("Примечания:"), 0, row);
        grid.add(notesArea, 1, row);
        row++;
        
        // Цена
        Label priceLabel = new Label();
        if (tutor.getHourlyRate() != null) {
            priceLabel.setText("Примерная стоимость: " + tutor.getHourlyRate() + " ₽/час");
            priceLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        }
        grid.add(priceLabel, 1, row);
        row++;
        
        dialog.getDialogPane().setContent(grid);
        
        // Валидация
        javafx.scene.Node bookButton = dialog.getDialogPane().lookupButton(bookButtonType);
        bookButton.setDisable(allSubjects == null || allSubjects.isEmpty());
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == bookButtonType) {
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
                LocalDateTime scheduledTime = LocalDateTime.of(date, time);
                
                return new BookingData(
                    subjectCombo.getValue(),
                    scheduledTime,
                    durationCombo.getValue(),
                    notesArea.getText()
                );
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    public static class BookingData {
        private final SubjectDTO subject;
        private final LocalDateTime scheduledTime;
        private final Integer duration;
        private final String notes;
        
        public BookingData(SubjectDTO subject, LocalDateTime scheduledTime, Integer duration, String notes) {
            this.subject = subject;
            this.scheduledTime = scheduledTime;
            this.duration = duration;
            this.notes = notes;
        }
        
        public SubjectDTO getSubject() { return subject; }
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public Integer getDuration() { return duration; }
        public String getNotes() { return notes; }
    }
}

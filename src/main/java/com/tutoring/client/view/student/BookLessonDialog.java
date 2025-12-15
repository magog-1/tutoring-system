package com.tutoring.client.view.student;

import com.tutoring.client.model.SubjectDTO;
import com.tutoring.client.model.TutorDTO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class BookLessonDialog {
    private final TutorDTO tutor;
    
    public BookLessonDialog(TutorDTO tutor) {
        this.tutor = tutor;
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
        
        // Предмет
        ComboBox<SubjectDTO> subjectCombo = new ComboBox<>();
        if (tutor.getSubjects() != null && !tutor.getSubjects().isEmpty()) {
            subjectCombo.getItems().addAll(tutor.getSubjects());
            subjectCombo.setValue(tutor.getSubjects().get(0));
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
        }
        
        // Дата
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now().plusDays(1));
        datePicker.setPromptText("Выберите дату");
        
        // Время
        Spinner<Integer> hourSpinner = new Spinner<>(8, 20, 10);
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 45, 0, 15);
        
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
        
        // Примечания
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Дополнительные пожелания...");
        notesArea.setPrefRowCount(3);
        
        // Цена
        Label priceLabel = new Label();
        if (tutor.getHourlyRate() != null) {
            priceLabel.setText("Примерная стоимость: " + tutor.getHourlyRate() + " ₽/час");
        }
        
        grid.add(new Label("Предмет:"), 0, 0);
        grid.add(subjectCombo, 1, 0);
        grid.add(new Label("Дата:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Время:"), 0, 2);
        
        javafx.scene.layout.HBox timeBox = new javafx.scene.layout.HBox(5);
        timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
        grid.add(timeBox, 1, 2);
        
        grid.add(new Label("Продолжительность:"), 0, 3);
        grid.add(durationCombo, 1, 3);
        grid.add(new Label("Примечания:"), 0, 4);
        grid.add(notesArea, 1, 4);
        grid.add(priceLabel, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Валидация
        javafx.scene.Node bookButton = dialog.getDialogPane().lookupButton(bookButtonType);
        bookButton.setDisable(tutor.getSubjects() == null || tutor.getSubjects().isEmpty());
        
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

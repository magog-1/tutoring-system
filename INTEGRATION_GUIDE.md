# Интеграция диалогов редактирования профиля

## Добавить импорты в TutorDashboard.java и StudentDashboard.java

```java
import com.tutoring.client.view.dialogs.EditProfileDialog;
import com.tutoring.client.view.dialogs.ChangePasswordDialog;
import com.tutoring.client.view.dialogs.ReviewsDialog; // только для TutorDashboard
```

## Для TutorDashboard.java

Заменить секцию с кнопками в методе `displayProfile()` (начиная со строки с `HBox buttonBox = new HBox(15);`) на:

```java
HBox buttonBox = new HBox(15);
buttonBox.setAlignment(Pos.CENTER);
buttonBox.setPadding(new Insets(20, 0, 0, 0));

Button editButton = new Button("Редактировать профиль");
editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
editButton.setOnAction(e -> {
    EditProfileDialog dialog = new EditProfileDialog(profileData, true);
    dialog.show().ifPresent(updatedData -> {
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient().put("/tutor/profile", updatedData);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText("Профиль обновлён");
                    alert.setContentText("Изменения успешно сохранены!");
                    alert.showAndWait();
                    showProfile();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка обновления");
                    alert.setContentText("Не удалось обновить профиль: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    });
});

Button changePasswordButton = new Button("Изменить пароль");
changePasswordButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
changePasswordButton.setOnAction(e -> {
    ChangePasswordDialog dialog = new ChangePasswordDialog();
    dialog.show().ifPresent(passwordData -> {
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient().put("/user/password", passwordData);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText("Пароль изменён");
                    alert.setContentText("Ваш пароль успешно обновлён!");
                    alert.showAndWait();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка смены пароля");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    });
});

Button reviewsButton = new Button("Мои отзывы");
reviewsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
reviewsButton.setOnAction(e -> {
    new Thread(() -> {
        try {
            String response = Session.getInstance().getApiClient().get("/tutor/reviews", String.class);
            Gson gson = GsonProvider.getGson();
            com.tutoring.client.model.ReviewDTO[] reviewsArray = gson.fromJson(response, com.tutoring.client.model.ReviewDTO[].class);
            java.util.List<com.tutoring.client.model.ReviewDTO> reviews = reviewsArray != null ? 
                java.util.Arrays.asList(reviewsArray) : new java.util.ArrayList<>();
            
            Platform.runLater(() -> {
                ReviewsDialog reviewsDialog = new ReviewsDialog(reviews);
                reviewsDialog.show();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Ошибка загрузки отзывов");
                alert.setContentText("Не удалось загрузить отзывы: " + ex.getMessage());
                alert.showAndWait();
            });
        }
    }).start();
});

buttonBox.getChildren().addAll(editButton, changePasswordButton, reviewsButton);
```

## Для StudentDashboard.java

Заменить секцию с кнопками в методе `displayProfile()` на:

```java
HBox buttonBox = new HBox(15);
buttonBox.setAlignment(Pos.CENTER);
buttonBox.setPadding(new Insets(20, 0, 0, 0));

Button editButton = new Button("Редактировать профиль");
editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
editButton.setOnAction(e -> {
    EditProfileDialog dialog = new EditProfileDialog(profileData, false);
    dialog.show().ifPresent(updatedData -> {
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient().put("/student/profile", updatedData);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText("Профиль обновлён");
                    alert.setContentText("Изменения успешно сохранены!");
                    alert.showAndWait();
                    showProfile();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка обновления");
                    alert.setContentText("Не удалось обновить профиль: " + ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    });
});

Button changePasswordButton = new Button("Изменить пароль");
changePasswordButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
changePasswordButton.setOnAction(e -> {
    ChangePasswordDialog dialog = new ChangePasswordDialog();
    dialog.show().ifPresent(passwordData -> {
        new Thread(() -> {
            try {
                Session.getInstance().getApiClient().put("/user/password", passwordData);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успех");
                    alert.setHeaderText("Пароль изменён");
                    alert.setContentText("Ваш пароль успешно обновлён!");
                    alert.showAndWait();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Ошибка смены пароля");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    });
});

buttonBox.getChildren().addAll(editButton, changePasswordButton);
```

## API Endpoints уже готовы:

✅ `PUT /api/student/profile` - обновление профиля студента
✅ `PUT /api/tutor/profile` - обновление профиля репетитора  
✅ `GET /api/tutor/reviews` - получение отзывов репетитора
✅ `PUT /api/user/password` - смена пароля (универсальный)

## Диалоги уже созданы:

✅ `EditProfileDialog.java` - редактирование профиля
✅ `ChangePasswordDialog.java` - смена пароля
✅ `ReviewsDialog.java` - просмотр отзывов

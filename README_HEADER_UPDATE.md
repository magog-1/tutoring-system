# Инструкция по обновлению шапок дашбордов

## Для TutorDashboard.java

Замените метод `createView()` следующим кодом:

```java
private void createView() {
    view = new BorderPane();
    
    // Улучшенная шапка с статистикой
    VBox headerBox = createHeader();
    view.setTop(headerBox);
    
    // Left - меню
    VBox leftMenu = createMenu();
    view.setLeft(leftMenu);
    
    // Center - по умолчанию показываем занятия
    showMyLessons();
}

private VBox createHeader() {
    VBox headerBox = new VBox(10);
    headerBox.setPadding(new Insets(20));
    headerBox.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #45a049);");
    
    // Верхняя строка: заголовок + имя + выход
    HBox topRow = new HBox(20);
    topRow.setAlignment(Pos.CENTER_LEFT);
    
    Label titleLabel = new Label("🎓 Личный кабинет репетитора");
    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
    titleLabel.setStyle("-fx-text-fill: white;");
    
    Region spacer1 = new Region();
    HBox.setHgrow(spacer1, Priority.ALWAYS);
    
    Label userLabel = new Label("👤 " + Session.getInstance().getCurrentUser().getFullName());
    userLabel.setFont(new Font(14));
    userLabel.setStyle("-fx-text-fill: white;");
    
    Button logoutButton = new Button("Выйти");
    logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
    logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
    logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
    logoutButton.setOnAction(e -> logout());
    
    topRow.getChildren().addAll(titleLabel, spacer1, userLabel, logoutButton);
    
    // Нижняя строка: статистика
    HBox statsRow = new HBox(30);
    statsRow.setAlignment(Pos.CENTER_LEFT);
    statsRow.setPadding(new Insets(10, 0, 0, 0));
    
    VBox todayLessons = createStatCard("Сегодня", String.valueOf(countTodayLessons()), "📅");
    VBox pendingLessons = createStatCard("На согласовании", String.valueOf(countByStatus("PENDING")), "⌛");
    VBox confirmedLessons = createStatCard("Подтверждено", String.valueOf(countByStatus("CONFIRMED")), "✅");
    VBox completedTotal = createStatCard("Всего завершено", String.valueOf(countByStatus("COMPLETED")), "🎯");
    
    statsRow.getChildren().addAll(todayLessons, pendingLessons, confirmedLessons, completedTotal);
    
    Separator separator = new Separator();
    separator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
    
    headerBox.getChildren().addAll(topRow, separator, statsRow);
    return headerBox;
}

private VBox createStatCard(String label, String value, String emoji) {
    VBox card = new VBox(5);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(10));
    card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;");
    card.setPrefWidth(150);
    
    Label emojiLabel = new Label(emoji);
    emojiLabel.setFont(new Font(20));
    
    Label valueLabel = new Label(value);
    valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
    valueLabel.setStyle("-fx-text-fill: white;");
    
    Label labelText = new Label(label);
    labelText.setFont(new Font(11));
    labelText.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
    labelText.setWrapText(true);
    labelText.setMaxWidth(140);
    labelText.setAlignment(Pos.CENTER);
    labelText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    
    card.getChildren().addAll(emojiLabel, valueLabel, labelText);
    return card;
}

private long countTodayLessons() {
    return allLessons.stream()
        .filter(l -> l.getScheduledTime() != null && 
                    l.getScheduledTime().toLocalDate().equals(LocalDate.now()))
        .count();
}

private long countByStatus(String status) {
    return allLessons.stream()
        .filter(l -> status.equalsIgnoreCase(l.getStatus()))
        .count();
}
```

## Для StudentDashboard.java

Замените метод `createView()` следующим кодом:

```java
private void createView() {
    view = new BorderPane();
    
    // Улучшенная шапка с статистикой
    VBox headerBox = createHeader();
    view.setTop(headerBox);
    
    // Left - меню
    VBox leftMenu = createMenu();
    view.setLeft(leftMenu);
    
    // Center
    showTutorSearch();
}

private VBox createHeader() {
    VBox headerBox = new VBox(10);
    headerBox.setPadding(new Insets(20));
    headerBox.setStyle("-fx-background-color: linear-gradient(to right, #2196F3, #1976D2);");
    
    // Верхняя строка
    HBox topRow = new HBox(20);
    topRow.setAlignment(Pos.CENTER_LEFT);
    
    Label titleLabel = new Label("📚 Личный кабинет студента");
    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
    titleLabel.setStyle("-fx-text-fill: white;");
    
    Region spacer1 = new Region();
    HBox.setHgrow(spacer1, Priority.ALWAYS);
    
    Label userLabel = new Label("👤 " + Session.getInstance().getCurrentUser().getFullName());
    userLabel.setFont(new Font(14));
    userLabel.setStyle("-fx-text-fill: white;");
    
    Button logoutButton = new Button("Выйти");
    logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
    logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
    logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
    logoutButton.setOnAction(e -> logout());
    
    topRow.getChildren().addAll(titleLabel, spacer1, userLabel, logoutButton);
    
    // Нижняя строка: статистика
    HBox statsRow = new HBox(30);
    statsRow.setAlignment(Pos.CENTER_LEFT);
    statsRow.setPadding(new Insets(10, 0, 0, 0));
    
    VBox todayLessons = createStatCard("Сегодня", String.valueOf(countTodayLessons()), "📅");
    VBox pendingLessons = createStatCard("Ожидают", String.valueOf(countByStatus("PENDING")), "⌛");
    VBox upcomingLessons = createStatCard("Предстоящие", String.valueOf(countByStatus("CONFIRMED")), "📖");
    VBox completedTotal = createStatCard("Завершено", String.valueOf(countByStatus("COMPLETED")), "✨");
    
    statsRow.getChildren().addAll(todayLessons, pendingLessons, upcomingLessons, completedTotal);
    
    Separator separator = new Separator();
    separator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
    
    headerBox.getChildren().addAll(topRow, separator, statsRow);
    return headerBox;
}

private VBox createStatCard(String label, String value, String emoji) {
    VBox card = new VBox(5);
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(10));
    card.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;");
    card.setPrefWidth(150);
    
    Label emojiLabel = new Label(emoji);
    emojiLabel.setFont(new Font(20));
    
    Label valueLabel = new Label(value);
    valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
    valueLabel.setStyle("-fx-text-fill: white;");
    
    Label labelText = new Label(label);
    labelText.setFont(new Font(11));
    labelText.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
    labelText.setWrapText(true);
    labelText.setMaxWidth(140);
    labelText.setAlignment(Pos.CENTER);
    labelText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
    
    card.getChildren().addAll(emojiLabel, valueLabel, labelText);
    return card;
}

private long countTodayLessons() {
    return allLessons.stream()
        .filter(l -> l.getScheduledTime() != null && 
                    l.getScheduledTime().toLocalDate().equals(LocalDate.now()))
        .count();
}

private long countByStatus(String status) {
    return allLessons.stream()
        .filter(l -> status.equalsIgnoreCase(l.getStatus()))
        .count();
}
```

## Что добавлено:

1. **Градиентный фон** - красивый градиент для каждой роли (зеленый для репетитора, синий для студента)
2. **Эмодзи иконки** - визуальные индикаторы для быстрой идентификации
3. **Карточки статистики** - 4 карточки показывающие:
   - Количество занятий сегодня
   - Занятия ожидающие подтверждения
   - Подтвержденные/Предстоящие занятия  
   - Всего завершенных занятий
4. **Улучшенная кнопка выхода** - с анимацией при наведении
5. **Выравненная сетка** - все элементы правильно выровнены с использованием Region spacer

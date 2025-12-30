# 🎓 Tutoring System - Система управления репетиторством

> Полнофункциональная платформа для организации и управления репетиторскими услугами с современным JavaFX интерфейсом и REST API backend.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📋 Содержание

- [Описание](#-описание)
- [Основные возможности](#-основные-возможности)
- [Технологический стек](#-технологический-стек)
- [Архитектура](#-архитектура)
- [Установка и запуск](#-установка-и-запуск)
- [Конфигурация](#-конфигурация)
- [Структура проекта](#-структура-проекта)
- [API Endpoints](#-api-endpoints)

## 🎯 Описание

**Tutoring System** - это комплексное решение для автоматизации работы репетиторских центров и индивидуальных преподавателей. Система предоставляет удобные инструменты для студентов, репетиторов и администраторов.

### Ключевые преимущества:

- 🎨 **Современный UI/UX** - интуитивный интерфейс на JavaFX с Material Design
- 🔐 **Безопасность** - Spring Security с JWT аутентификацией
- 📊 **Аналитика** - визуализация расписания и статистики
- 🌐 **RESTful API** - полнофункциональный бэкенд
- 💾 **Надежность** - PostgreSQL база данных
- 🚀 **Производительность** - оптимизированные запросы и кэширование

## ✨ Основные возможности

### Для студентов 👨‍🎓

- ✅ Поиск и выбор репетиторов по предметам
- ✅ Просмотр профилей репетиторов с рейтингами и отзывами
- ✅ Бронирование занятий с выбором времени и длительности
- ✅ Календарь занятий с визуализацией
- ✅ Управление личным профилем
- ✅ Смена пароля с валидацией
- ✅ История занятий и оплат
- ✅ Создание отзывов о репетиторах

### Для репетиторов 👨‍🏫

- ✅ Управление расписанием занятий
- ✅ Подтверждение/отклонение заявок на занятия
- ✅ Завершение занятий с добавлением заметок и домашних заданий
- ✅ Просмотр списка студентов
- ✅ Календарь с визуализацией занятий
- ✅ Управление профилем (образование, опыт, ставка)
- ✅ Просмотр отзывов от студентов
- ✅ Статистика по занятиям
- ✅ Смена пароля

### Для администраторов 👔

- ✅ Управление пользователями
- ✅ Модерация профилей репетиторов
- ✅ Управление предметами
- ✅ Просмотр всех занятий
- ✅ Аналитика и отчеты
- ✅ Настройка системы

### Система управления

- 🔄 **Статусы занятий**:
  - `PENDING` - На согласовании преподавателем
  - `CONFIRMED` - Подтверждено преподавателем
  - `COMPLETED` - Завершено
  - `CANCELLED` - Отклонено

- 📝 **Отзывы и рейтинги**:
  - Система оценок от 1 до 5 звёзд
  - Текстовые комментарии
  - Автоматический пересчёт рейтинга репетитора

- 📅 **Расписание**:
  - Календарный вид с выделением дат
  - Детальная информация о занятиях
  - Фильтры по статусам и датам

## 🛠 Технологический стек

### Backend

| Технология | Версия | Назначение |
|-----------|--------|------------|
| Java | 17+ | Основной язык разработки |
| Spring Boot | 3.2.0 | Фреймворк приложения |
| Spring Security | 6.x | Аутентификация и авторизация |
| Spring Data JPA | 3.2.0 | ORM и работа с БД |
| PostgreSQL | 15+ | Реляционная база данных |
| Hibernate | 6.x | ORM провайдер |
| Maven | 3.9+ | Система сборки |
| JWT | - | Токены для API |
| Lombok | 1.18+ | Уменьшение boilerplate кода |

### Frontend (Desktop Client)

| Технология | Версия | Назначение |
|-----------|--------|------------|
| JavaFX | 21 | UI фреймворк |
| Gson | 2.10 | JSON сериализация |
| Java HTTP Client | 11+ | HTTP клиент для API |

### База данных

```sql
-- Основные таблицы:
- users          -- Базовая таблица пользователей
- students       -- Студенты (extends users)
- tutors         -- Репетиторы (extends users)
- subjects       -- Предметы
- lessons        -- Занятия
- reviews        -- Отзывы
- tutor_subjects -- Связь репетиторов и предметов
```

## 🏗 Архитектура

### Архитектурные паттерны

- **MVC** - Model-View-Controller для разделения логики
- **Repository Pattern** - Абстракция доступа к данным
- **Service Layer** - Бизнес-логика
- **DTO Pattern** - Передача данных между слоями
- **REST API** - Взаимодействие клиент-сервер

### Структура слоёв

```
┌─────────────────────────────────────────┐
│         JavaFX Desktop Client           │
│  (View Layer + Local Controllers)       │
└─────────────┬───────────────────────────┘
              │ HTTP/JSON (REST API)
              ↓
┌─────────────────────────────────────────┐
│         Spring Boot Backend             │
├─────────────────────────────────────────┤
│  Controllers (REST Endpoints)           │
├─────────────────────────────────────────┤
│  Service Layer (Business Logic)         │
├─────────────────────────────────────────┤
│  Repository Layer (Data Access)         │
├─────────────────────────────────────────┤
│  Spring Data JPA / Hibernate            │
└─────────────┬───────────────────────────┘
              │ JDBC
              ↓
┌─────────────────────────────────────────┐
│         PostgreSQL Database             │
└─────────────────────────────────────────┘
```

### Программное обеспечение

- **JDK**: 17 или выше ([AdoptOpenJDK](https://adoptopenjdk.net/) или [Oracle JDK](https://www.oracle.com/java/technologies/downloads/))
- **Maven**: 3.9+ ([скачать](https://maven.apache.org/download.cgi))
- **PostgreSQL**: 15+ ([скачать](https://www.postgresql.org/download/))
- **IDE** (опционально): IntelliJ IDEA, Eclipse, VS Code

## 🚀 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/magog-1/tutoring-system.git
cd tutoring-system
```

### 2. Настройка базы данных

#### Создание базы данных

```bash
# Подключитесь к PostgreSQL
psql -U postgres

# Создайте базу данных
CREATE DATABASE tutoring_db;

# Создайте пользователя (опционально)
CREATE USER tutoring_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE tutoring_db TO tutoring_user;
```

#### Настройка application.properties

Откройте `src/main/resources/application.properties` и настройте подключение:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/tutoring_db
spring.datasource.username=tutoring_user
spring.datasource.password=your_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Server
server.port=8080
```

### 3. Сборка проекта

```bash
# Сборка с тестами
mvn clean install

# Сборка без тестов (быстрее)
mvn clean install -DskipTests
```

### 4. Запуск backend

```bash
# Вариант 1: через Maven
mvn spring-boot:run

# Вариант 2: через JAR
java -jar target/tutoring-system-1.0.0.jar

# Вариант 3: через IDE
# Запустите TutoringSystemApplication.java
```

Backend будет доступен на `http://localhost:8080`

### 5. Запуск JavaFX клиента

```bash
# Через Maven
mvn javafx:run

# Или запустите Main.java из IDE
```

### 6. Первоначальная настройка

#### Создание администратора

```sql
-- Подключитесь к БД и выполните:
INSERT INTO users (username, password, email, first_name, last_name, role, is_active)
VALUES (
    'admin',
    '$2a$10$...', -- BCrypt хеш пароля
    'admin@tutoring.com',
    'Admin',
    'User',
    'ADMIN',
    true
);
```

Или через REST API:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@tutoring.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

#### Добавление тестовых данных

```bash
# Запустите скрипт инициализации
psql -U tutoring_user -d tutoring_db -f sql/init_test_data.sql
```

## ⚙️ Конфигурация

### application.properties

```properties
# Основные настройки
spring.application.name=Tutoring System

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/tutoring_db
spring.datasource.username=tutoring_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Server
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.com.tutoring=DEBUG
logging.level.org.springframework.security=DEBUG

# JWT Configuration
jwt.secret=your-secret-key-change-in-production
jwt.expiration=86400000

# File Upload (если используется)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Переменные окружения (Production)

```bash
export DB_URL=jdbc:postgresql://production-db:5432/tutoring_db
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=production-secret-key
export SERVER_PORT=8080
```

## 📁 Структура проекта

```
tutoring-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tutoring/
│   │   │       ├── TutoringSystemApplication.java
│   │   │       ├── client/                    # JavaFX клиент
│   │   │       │   ├── Main.java
│   │   │       │   ├── api/                   # API клиент
│   │   │       │   │   ├── ApiClient.java
│   │   │       │   │   └── Session.java
│   │   │       │   ├── model/                 # DTO модели
│   │   │       │   │   ├── UserDTO.java
│   │   │       │   │   ├── LessonDTO.java
│   │   │       │   │   └── ReviewDTO.java
│   │   │       │   └── view/                  # UI компоненты
│   │   │       │       ├── LoginView.java
│   │   │       │       ├── dialogs/
│   │   │       │       │   ├── EditProfileDialog.java
│   │   │       │       │   ├── ChangePasswordDialog.java
│   │   │       │       │   └── ReviewsDialog.java
│   │   │       │       ├── student/
│   │   │       │       │   └── StudentDashboard.java
│   │   │       │       ├── tutor/
│   │   │       │       │   └── TutorDashboard.java
│   │   │       │       └── manager/
│   │   │       │           └── ManagerDashboard.java
│   │   │       ├── controller/                # REST контроллеры
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── StudentController.java
│   │   │       │   ├── TutorController.java
│   │   │       │   ├── UserController.java
│   │   │       │   └── PublicTutorController.java
│   │   │       ├── service/                   # Бизнес-логика
│   │   │       │   ├── LessonService.java
│   │   │       │   ├── ReviewService.java
│   │   │       │   ├── TutorService.java
│   │   │       │   └── UserService.java
│   │   │       ├── repository/                # Репозитории
│   │   │       │   ├── LessonRepository.java
│   │   │       │   ├── ReviewRepository.java
│   │   │       │   ├── StudentRepository.java
│   │   │       │   ├── TutorRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       ├── model/                     # Entities
│   │   │       │   ├── User.java
│   │   │       │   ├── Student.java
│   │   │       │   ├── Tutor.java
│   │   │       │   ├── Lesson.java
│   │   │       │   ├── Review.java
│   │   │       │   └── Subject.java
│   │   │       ├── config/                    # Конфигурация
│   │   │       │   └── SecurityConfig.java
│   │   │       └── exception/                 # Обработка ошибок
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── test/                                  # Тесты
│       └── java/
├── pom.xml                                    # Maven конфигурация
├── README.md
├── INTEGRATION_GUIDE.md
└── README_INTEGRATION.md
```

## 🔌 API Endpoints

### Authentication

```http
POST   /api/auth/register     # Регистрация
POST   /api/auth/login        # Вход
POST   /api/auth/logout       # Выход
```

### Student Endpoints

```http
GET    /api/student/profile           # Получить профиль
PUT    /api/student/profile           # Обновить профиль
GET    /api/student/tutors            # Список репетиторов
POST   /api/student/lessons/book      # Забронировать занятие
GET    /api/student/lessons           # Мои занятия
POST   /api/student/reviews           # Создать отзыв
DELETE /api/student/reviews/{id}      # Удалить отзыв
```

### Tutor Endpoints

```http
GET    /api/tutor/profile             # Получить профиль
PUT    /api/tutor/profile             # Обновить профиль
GET    /api/tutor/reviews             # Мои отзывы
GET    /api/tutor/lessons             # Мои занятия
PUT    /api/tutor/lessons/{id}/confirm   # Подтвердить занятие
PUT    /api/tutor/lessons/{id}/cancel    # Отклонить занятие
PUT    /api/tutor/lessons/{id}/complete  # Завершить занятие
```

### User Endpoints

```http
PUT    /api/user/password             # Сменить пароль
```

### Public Endpoints

```http
GET    /api/tutors                    # Все репетиторы
GET    /api/tutors/{id}               # Репетитор по ID
GET    /api/tutors/{id}/reviews       # Отзывы репетитора
GET    /api/tutors/search             # Поиск репетиторов
```

## 👨‍💻 Разработка

### Настройка среды разработки

#### IntelliJ IDEA

1. Импортируйте проект как Maven проект
2. Установите Lombok plugin
3. Enable Annotation Processing: Settings → Build → Compiler → Annotation Processors
4. Настройте Code Style согласно `.editorconfig`

#### VS Code

Установите расширения:
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support

### Запуск в режиме разработки

```bash
# Backend с hot reload
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"

# Frontend
mvn javafx:run
```

### Тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn test -Dtest=LessonServiceTest

# Генерация отчёта о покрытии
mvn jacoco:report
```

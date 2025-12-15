package com.tutoring.service;

import com.tutoring.model.Student;
import com.tutoring.model.Tutor;
import com.tutoring.model.User;
import com.tutoring.repository.StudentRepository;
import com.tutoring.repository.TutorRepository;
import com.tutoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // Проверка email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Электронная почта уже зарегистрирована");
        }

        // Проверка username (если указан)
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new IllegalArgumentException("Username уже занят");
            }
        } else {
            // Если username не указан, используем email до @ как username
            user.setUsername(user.getEmail().split("@")[0]);
        }

        // Шифруем пароль
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        user.setIsActive(true);

        // В зависимости от роли создаём специализированные сущности
        if (user.getRole() == User.UserRole.STUDENT) {
            Student student = new Student();
            student.setEmail(user.getEmail());
            student.setUsername(user.getUsername());
            student.setPassword(user.getPassword());
            student.setFirstName(user.getFirstName());
            student.setLastName(user.getLastName());
            student.setPhone(user.getPhone());
            student.setRole(user.getRole());
            student.setRegistrationDate(user.getRegistrationDate());
            student.setIsActive(user.getIsActive());
            // Дополнительные поля студента по умолчанию
            return studentRepository.save(student);
        } else if (user.getRole() == User.UserRole.TUTOR) {
            Tutor tutor = new Tutor();
            tutor.setEmail(user.getEmail());
            tutor.setUsername(user.getUsername());
            tutor.setPassword(user.getPassword());
            tutor.setFirstName(user.getFirstName());
            tutor.setLastName(user.getLastName());
            tutor.setPhone(user.getPhone());
            tutor.setRole(user.getRole());
            tutor.setRegistrationDate(user.getRegistrationDate());
            tutor.setIsActive(user.getIsActive());
            // Дополнительные поля репетитора по умолчанию
            tutor.setEducation("Не указано"); // Можно обновить позже
            tutor.setExperienceYears(0);
            tutor.setHourlyRate(BigDecimal.valueOf(1000)); // Ставка по умолчанию
            return tutorRepository.save(tutor);
        } else {
            // Для MANAGER и ADMIN сохраняем обычного User
            return userRepository.save(user);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPassword()) && user.getIsActive();
    }
}

package com.courseapp.service;

import com.courseapp.domain.Instructor;
import com.courseapp.domain.Student;
import com.courseapp.domain.User;
import com.courseapp.dto.LoginRequest;
import com.courseapp.dto.RegisterRequest;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.repository.StudentRepository;
import com.courseapp.repository.UserRepository;
import com.courseapp.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                      InstructorRepository instructorRepository,
                      StudentRepository studentRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.instructorRepository = instructorRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        String role = req.getRole();
        if (!role.equals("student") && !role.equals("instructor")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role must be student or instructor");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setRole(role);

        Long userId = userRepository.save(user);

        Long profileId;
        if (role.equals("instructor")) {
            profileId = instructorRepository.save(userId);
        } else {
            profileId = studentRepository.save(userId);
        }

        User saved = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));

        String token = jwtUtil.generateToken(userId, saved.getEmail(), saved.getRole());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", saved.getId());
        userMap.put("email", saved.getEmail());
        userMap.put("full_name", saved.getFullName());
        userMap.put("role", saved.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userMap);
        return result;
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!passwordEncoder.matches(req.getPassword(), storedHash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        Long profileId = null;
        if (user.getRole().equals("instructor")) {
            profileId = instructorRepository.findIdByUserId(user.getId()).orElse(null);
        } else if (user.getRole().equals("student")) {
            profileId = studentRepository.findIdByUserId(user.getId()).orElse(null);
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("full_name", user.getFullName());
        userMap.put("role", user.getRole());
        if (profileId != null) userMap.put("profile_id", profileId);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userMap);
        return result;
    }

    public Map<String, Object> me(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("full_name", user.getFullName());
        result.put("role", user.getRole());
        result.put("created_at", user.getCreatedAt());

        if (user.getRole().equals("instructor")) {
            Instructor inv = instructorRepository.findWithProfile(userId).orElse(null);
            if (inv != null) {
                Map<String, Object> invMap = new HashMap<>();
                invMap.put("id", inv.getId());
                invMap.put("bio", inv.getBio());
                invMap.put("expertise", inv.getExpertise());
                result.put("instructor", invMap);
            }
        } else if (user.getRole().equals("student")) {
            Student stu = studentRepository.findWithProfile(userId).orElse(null);
            if (stu != null) {
                Map<String, Object> stuMap = new HashMap<>();
                stuMap.put("id", stu.getId());
                stuMap.put("date_of_birth", stu.getDateOfBirth());
                stuMap.put("phone", stu.getPhone());
                stuMap.put("address", stu.getAddress());
                result.put("student", stuMap);
            }
        }

        return result;
    }
}
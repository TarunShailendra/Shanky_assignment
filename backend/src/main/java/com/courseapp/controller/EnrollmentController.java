package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.StudentRepository;
import com.courseapp.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentRepository studentRepository;

    public EnrollmentController(EnrollmentService enrollmentService,
                                StudentRepository studentRepository) {
        this.enrollmentService = enrollmentService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/student")
    public ResponseEntity<List<?>> byStudent() {
        var user = CourseController.getUser();
        if (!"student".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        return ResponseEntity.ok(enrollmentService.getByStudent(profileId));
    }

    @PostMapping
    public ResponseEntity<?> enroll(@RequestBody Map<String, Long> body) {
        var user = CourseController.getUser();
        if (!"student".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only students can enroll");
        }
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        Long courseId = body.get("course_id");
        if (courseId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "course_id is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(enrollmentService.enroll(profileId, courseId));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> cancel(@PathVariable Long courseId) {
        var user = CourseController.getUser();
        if (!"student".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        enrollmentService.cancel(profileId, courseId);
        return ResponseEntity.ok(Map.of("message", "Enrollment cancelled"));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<?>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getByCourse(courseId));
    }
}
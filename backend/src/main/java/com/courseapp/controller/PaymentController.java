package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.repository.StudentRepository;
import com.courseapp.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public PaymentController(PaymentService paymentService,
                            StudentRepository studentRepository,
                            InstructorRepository instructorRepository) {
        this.paymentService = paymentService;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/student")
    public ResponseEntity<List<?>> byStudent() {
        var user = CourseController.getUser();
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        return ResponseEntity.ok(paymentService.getByStudent(profileId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<?>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(paymentService.getByCourse(courseId));
    }

    @GetMapping("/instructor")
    public ResponseEntity<List<?>> byInstructor() {
        var user = CourseController.getUser();
        if (!"instructor".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        return ResponseEntity.ok(paymentService.getByInstructor(profileId));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary() {
        var user = CourseController.getUser();
        return ResponseEntity.ok(paymentService.summary(user.role(), user.userId()));
    }

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        if (!"student".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only students can make payments");
        }
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        Long courseId = ((Number) body.get("course_id")).longValue();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.pay(profileId, courseId, amount,
                (String) body.get("payment_method")));
    }
}
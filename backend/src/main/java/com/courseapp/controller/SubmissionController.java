package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.repository.StudentRepository;
import com.courseapp.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public SubmissionController(SubmissionService submissionService,
                               StudentRepository studentRepository,
                               InstructorRepository instructorRepository) {
        this.submissionService = submissionService;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/student")
    public ResponseEntity<List<?>> byStudent() {
        var user = CourseController.getUser();
        return ResponseEntity.ok(
            submissionService.getByStudentRole(user.role(), user.userId()));
    }

    @GetMapping("/assignment/{id}")
    public ResponseEntity<List<?>> byAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getByAssignment(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(submissionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        if (!"student".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only students can submit");
        }
        Long profileId = studentRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(submissionService.submit(profileId,
                ((Number) body.get("assignment_id")).longValue(),
                (String) body.get("submission_text"),
                (String) body.get("file_url")));
    }

    @PutMapping("/{id}/grade")
    public ResponseEntity<?> grade(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        if (!"instructor".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only instructors can grade");
        }
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        Integer score = ((Number) body.get("score")).intValue();
        return ResponseEntity.ok(submissionService.grade(id, profileId,
            score, (String) body.get("feedback")));
    }
}
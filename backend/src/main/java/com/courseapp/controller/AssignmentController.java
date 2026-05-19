package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.service.AssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final InstructorRepository instructorRepository;

    public AssignmentController(AssignmentService assignmentService,
                               InstructorRepository instructorRepository) {
        this.assignmentService = assignmentService;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<?>> byModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(assignmentService.getByModule(moduleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        if (!"instructor".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(assignmentService.create(profileId,
                ((Number) body.get("module_id")).longValue(),
                (String) body.get("title"),
                (String) body.get("description"),
                (String) body.get("due_date"),
                body.get("max_score") != null
                    ? ((Number) body.get("max_score")).intValue() : null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        return ResponseEntity.ok(assignmentService.update(id, profileId,
            (String) body.get("title"),
            (String) body.get("description"),
            (String) body.get("due_date"),
            body.get("max_score") != null
                ? ((Number) body.get("max_score")).intValue() : null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        var user = CourseController.getUser();
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        assignmentService.delete(id, profileId);
        return ResponseEntity.ok(Map.of("message", "Assignment deleted"));
    }
}
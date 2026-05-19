package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.service.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;
    private final InstructorRepository instructorRepository;

    public ModuleController(ModuleService moduleService,
                          InstructorRepository instructorRepository) {
        this.moduleService = moduleService;
        this.instructorRepository = instructorRepository;
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<?>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(moduleService.getByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getById(id));
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
            .body(moduleService.create(profileId,
                ((Number) body.get("course_id")).longValue(),
                (String) body.get("title"),
                (String) body.get("description")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var user = CourseController.getUser();
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        Integer orderIndex = body.get("order_index") != null
            ? ((Number) body.get("order_index")).intValue() : null;
        return ResponseEntity.ok(moduleService.update(id, profileId,
            (String) body.get("title"),
            (String) body.get("description"),
            orderIndex));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        var user = CourseController.getUser();
        Long profileId = instructorRepository.findIdByUserId(user.userId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Instructor not found"));
        moduleService.delete(id, profileId);
        return ResponseEntity.ok(Map.of("message", "Module deleted"));
    }
}
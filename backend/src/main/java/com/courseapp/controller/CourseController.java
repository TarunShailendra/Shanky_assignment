package com.courseapp.controller;

import com.courseapp.exception.ApiException;
import com.courseapp.repository.InstructorRepository;
import com.courseapp.repository.StudentRepository;
import com.courseapp.security.UserPrincipal;
import com.courseapp.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<?>> list() {
        return ResponseEntity.ok(courseService.listPublished());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @GetMapping("/instructor/mine")
    public ResponseEntity<List<?>> mine() {
        UserPrincipal user = getUser();
        if (!"instructor".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return ResponseEntity.ok(courseService.listByInstructor(user.userId()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        UserPrincipal user = getUser();
        if (!"instructor".equals(user.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(courseService.create(user.userId(),
                body.get("title"), body.get("description"),
                body.get("price"), body.get("category")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        UserPrincipal user = getUser();
        return ResponseEntity.ok(courseService.update(id, user.userId(),
            body.get("title"), body.get("description"),
            body.get("price"), body.get("category"),
            body.get("is_published"), body.get("thumbnail")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        UserPrincipal user = getUser();
        courseService.delete(id, user.userId());
        return ResponseEntity.ok(Map.of("message", "Course deleted"));
    }

    public static UserPrincipal getUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) auth.getPrincipal();
    }
}
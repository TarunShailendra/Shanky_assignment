package com.courseapp.service;

import com.courseapp.domain.Assignment;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.AssignmentRepository;
import com.courseapp.repository.ModuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ModuleRepository moduleRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                            ModuleRepository moduleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.moduleRepository = moduleRepository;
    }

    public List<Assignment> getByModule(Long moduleId) {
        return assignmentRepository.findByModule(moduleId);
    }

    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    public Assignment create(Long instructorId, Long moduleId, String title,
                             String description, String dueDate, Integer maxScore) {
        if (moduleId == null || title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "module_id and title are required");
        }

        var owner = moduleRepository.findInstructorId(moduleId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Assignment a = new Assignment();
        a.setModuleId(moduleId);
        a.setTitle(title);
        a.setDescription(description != null ? description : "");
        a.setDueDate(parseDateTime(dueDate));
        a.setMaxScore(maxScore != null ? maxScore : 100);

        Long id = assignmentRepository.save(a);
        return assignmentRepository.findById(id).orElse(a);
    }

    public Assignment update(Long id, Long instructorId, String title,
                              String description, String dueDate, Integer maxScore) {
        var owner = assignmentRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Assignment updates = new Assignment();
        updates.setTitle(title);
        updates.setDescription(description);
        updates.setDueDate(parseDateTime(dueDate));
        updates.setMaxScore(maxScore);

        assignmentRepository.update(id, updates);
        return assignmentRepository.findById(id).orElseThrow();
    }

    public void delete(Long id, Long instructorId) {
        var owner = assignmentRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        assignmentRepository.delete(id);
    }

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s.replace(" ", "T"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
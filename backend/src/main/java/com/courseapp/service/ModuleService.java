package com.courseapp.service;

import com.courseapp.domain.Assignment;
import com.courseapp.domain.Module;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.AssignmentRepository;
import com.courseapp.repository.CourseRepository;
import com.courseapp.repository.ModuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;

    public ModuleService(ModuleRepository moduleRepository,
                        CourseRepository courseRepository,
                        AssignmentRepository assignmentRepository) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<Module> getByCourse(Long courseId) {
        List<Module> modules = moduleRepository.findByCourse(courseId);
        for (Module m : modules) {
            if (m.getAssignmentCount() == null) {
                m.setAssignmentCount(assignmentRepository.findByModule(m.getId()).size());
            }
            // Build assignments inline for the nested JSON structure
            List<Assignment> assignments = assignmentRepository.findByModule(m.getId());
            m.setAssignments(assignments);
        }
        return modules;
    }

    public Module getById(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Module not found"));

        List<Assignment> assignments = assignmentRepository.findByModule(id);
        for (Assignment a : assignments) {
            a.setSubmissionCount(0);
        }
        module.setAssignments(assignments);
        return module;
    }

    public Module create(Long instructorId, Long courseId, String title, String description) {
        if (courseId == null || title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "course_id and title are required");
        }

        var owner = courseRepository.findInstructorId(courseId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Module module = new Module();
        module.setCourseId(courseId);
        module.setTitle(title);
        module.setDescription(description != null ? description : "");
        module.setOrderIndex(moduleRepository.getNextOrder(courseId));

        Long id = moduleRepository.save(module);
        return moduleRepository.findById(id).orElse(module);
    }

    public Module update(Long id, Long instructorId, String title, String description, Integer orderIndex) {
        var instructorIdOpt = moduleRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!instructorIdOpt.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Module updates = new Module();
        updates.setTitle(title);
        updates.setDescription(description);
        updates.setOrderIndex(orderIndex);

        moduleRepository.update(id, updates);
        return moduleRepository.findById(id).orElseThrow();
    }

    public void delete(Long id, Long instructorId) {
        var instructorIdOpt = moduleRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Module not found"));
        if (!instructorIdOpt.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        moduleRepository.delete(id);
    }
}
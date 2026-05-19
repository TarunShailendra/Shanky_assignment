package com.courseapp.service;

import com.courseapp.domain.Course;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final AssignmentRepository assignmentRepository;

    public CourseService(CourseRepository courseRepository,
                        ModuleRepository moduleRepository,
                        AssignmentRepository assignmentRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<Course> listPublished() {
        return courseRepository.findPublished();
    }

    public Map<String, Object> getById(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));

        List<Module> modules = moduleRepository.findByCourse(id);
        // Fill in assignmentCount for each module
        for (Module m : modules) {
            if (m.getAssignmentCount() == null) {
                List<?> assignments = assignmentRepository.findByModule(m.getId());
                m.setAssignmentCount(assignments.size());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("instructor_id", course.getInstructorId());
        result.put("title", course.getTitle());
        result.put("description", course.getDescription());
        result.put("price", course.getPrice());
        result.put("thumbnail", course.getThumbnail());
        result.put("is_published", course.getIsPublished());
        result.put("category", course.getCategory());
        result.put("created_at", course.getCreatedAt());
        result.put("updated_at", course.getUpdatedAt());
        result.put("instructor_name", course.getInstructorName());
        result.put("bio", null);           // populated via JOIN; leave as-is
        result.put("expertise", null);
        result.put("modules", modules);
        return result;
    }

    public List<Course> listByInstructor(Long instructorId) {
        return courseRepository.findByInstructor(instructorId);
    }

    public Course create(Long instructorId, String title, String description,
                         String price, String category) {
        if (title == null || title.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        Course course = new Course();
        course.setInstructorId(instructorId);
        course.setTitle(title);
        course.setDescription(description != null ? description : "");
        course.setPrice(price != null ? new java.math.BigDecimal(price) : java.math.BigDecimal.ZERO);
        course.setCategory(category != null ? category : "");
        course.setIsPublished(false);
        course.setThumbnail("");

        Long id = courseRepository.save(course);
        return courseRepository.findById(id).orElse(course);
    }

    public Course update(Long id, Long instructorId,
                        String title, String description,
                        String price, String category,
                        String isPublished, String thumbnail) {
        var owner = courseRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        Course updates = new Course();
        updates.setTitle(title);
        updates.setDescription(description);
        updates.setPrice(price != null ? new java.math.BigDecimal(price) : null);
        updates.setCategory(category);
        updates.setIsPublished(isPublished != null ? Boolean.parseBoolean(isPublished) : null);
        updates.setThumbnail(thumbnail);

        courseRepository.update(id, updates);
        return courseRepository.findById(id).orElseThrow();
    }

    public void delete(Long id, Long instructorId) {
        var owner = courseRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        courseRepository.delete(id);
    }
}
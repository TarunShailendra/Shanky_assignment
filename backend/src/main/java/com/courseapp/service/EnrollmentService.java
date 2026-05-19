package com.courseapp.service;

import com.courseapp.domain.Enrollment;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.CourseRepository;
import com.courseapp.repository.EnrollmentRepository;
import com.courseapp.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            CourseRepository courseRepository,
                            StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    public List<Enrollment> getByStudent(Long profileId) {
        return enrollmentRepository.findByStudent(profileId);
    }

    public Enrollment enroll(Long profileId, Long courseId) {
        var result = enrollmentRepository.enroll(profileId, courseId);
        if (result == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Already enrolled in this course");
        }
        return enrollmentRepository.findByStudent(profileId).stream()
            .filter(e -> e.getCourseId().equals(courseId))
            .findFirst()
            .orElseThrow();
    }

    public void cancel(Long profileId, Long courseId) {
        int rows = enrollmentRepository.cancel(profileId, courseId);
        if (rows == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Enrollment not found");
        }
    }

    public List<Enrollment> getByCourse(Long courseId) {
        return enrollmentRepository.findByCourse(courseId);
    }
}
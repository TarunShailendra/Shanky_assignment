package com.courseapp.service;

import com.courseapp.domain.Submission;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.AssignmentRepository;
import com.courseapp.repository.SubmissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                            AssignmentRepository assignmentRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<Submission> getByStudent(Long profileId) {
        return submissionRepository.findByStudent(profileId);
    }

    public List<Submission> getByStudentRole(String role, Long profileId) {
        if (role.equals("student")) {
            return submissionRepository.findByStudent(profileId);
        } else {
            return submissionRepository.findByInstructor(profileId);
        }
    }

    public List<Submission> getByAssignment(Long assignmentId) {
        assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found"));
        return submissionRepository.findByAssignment(assignmentId);
    }

    public Submission submit(Long studentId, Long assignmentId,
                              String submissionText, String fileUrl) {
        if (assignmentId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "assignment_id is required");
        }

        Submission s = new Submission();
        s.setAssignmentId(assignmentId);
        s.setStudentId(studentId);
        s.setSubmissionText(submissionText != null ? submissionText : "");
        s.setFileUrl(fileUrl != null ? fileUrl : "");

        Long id = submissionRepository.upsert(s);
        return submissionRepository.findById(id).orElse(s);
    }

    public Submission grade(Long id, Long instructorId, Integer score, String feedback) {
        if (score == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "score is required");
        }

        var owner = submissionRepository.findInstructorId(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (!owner.equals(instructorId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        submissionRepository.grade(id, score, feedback != null ? feedback : "");
        return submissionRepository.findById(id).orElseThrow();
    }

    public Submission getById(Long id) {
        return submissionRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found"));
    }
}
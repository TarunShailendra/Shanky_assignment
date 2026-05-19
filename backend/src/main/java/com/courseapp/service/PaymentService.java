package com.courseapp.service;

import com.courseapp.domain.Payment;
import com.courseapp.exception.ApiException;
import com.courseapp.repository.EnrollmentRepository;
import com.courseapp.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public PaymentService(PaymentRepository paymentRepository,
                        EnrollmentRepository enrollmentRepository) {
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Payment> getByStudent(Long studentId) {
        return paymentRepository.findByStudent(studentId);
    }

    public List<Payment> getByCourse(Long courseId) {
        return paymentRepository.findByCourse(courseId);
    }

    public List<Payment> getByInstructor(Long instructorId) {
        return paymentRepository.findByInstructor(instructorId);
    }

    @Transactional
    public Payment pay(Long studentId, Long courseId, BigDecimal amount, String paymentMethod) {
        if (courseId == null || amount == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "course_id and amount are required");
        }

        if (paymentRepository.existsCompleted(studentId, courseId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Already paid for this course");
        }

        String transactionId = "txn_" + System.currentTimeMillis() + "_" +
                               UUID.randomUUID().toString().replace("-", "").substring(0, 9);

        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setCourseId(courseId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "card");
        payment.setStatus("completed");
        payment.setTransactionId(transactionId);

        Long id = paymentRepository.save(payment);

        // Auto-enroll on successful payment
        enrollmentRepository.enroll(studentId, courseId);

        paymentRepository.findByStudent(studentId).stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .ifPresent(p -> { /* loaded */ });

        Payment saved = new Payment();
        saved.setId(id);
        saved.setStudentId(studentId);
        saved.setCourseId(courseId);
        saved.setAmount(amount);
        saved.setPaymentMethod(payment.getPaymentMethod());
        saved.setStatus("completed");
        saved.setTransactionId(transactionId);
        return saved;
    }

    public Map<String, Object> summary(String role, Long profileId) {
        if ("instructor".equals(role)) {
            return paymentRepository.summaryInstructor(profileId);
        } else {
            return paymentRepository.summaryStudent(profileId);
        }
    }
}
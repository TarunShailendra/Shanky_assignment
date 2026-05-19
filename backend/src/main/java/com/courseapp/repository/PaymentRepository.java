package com.courseapp.repository;

import com.courseapp.domain.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Payment> ROW_MAPPER = (rs, rowNum) -> mapPayment(rs);

    private static Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setStudentId(rs.getLong("student_id"));
        p.setCourseId(rs.getLong("course_id"));
        p.setAmount(getDecimal(rs, "amount"));
        p.setCurrency(rs.getString("currency"));
        p.setStatus(rs.getString("status"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setPaidAt(getTimestamp(rs, "paid_at"));

        p.setCourseTitle(rs.getString("course_title"));
        p.setCourseThumbnail(rs.getString("course_thumbnail"));
        p.setStudentName(rs.getString("student_name"));
        p.setStudentInternalId(getLong(rs, "student_internal_id"));

        return p;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static BigDecimal getDecimal(ResultSet rs, String col) throws SQLException {
        return rs.getBigDecimal(col);
    }

    private static Long getLong(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }

    public List<Payment> findByStudent(Long studentId) {
        String sql = """
            SELECT p.*, c.title AS course_title, c.thumbnail AS course_thumbnail,
                   NULL AS student_name, NULL AS student_internal_id
            FROM payments p
            JOIN courses c ON c.id = p.course_id
            WHERE p.student_id = ?
            ORDER BY p.paid_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, studentId);
    }

    public List<Payment> findByCourse(Long courseId) {
        String sql = """
            SELECT p.*, u.full_name AS student_name, st.id AS student_internal_id,
                   NULL AS course_title, NULL AS course_thumbnail
            FROM payments p
            JOIN students st ON st.id = p.student_id
            JOIN users u ON u.id = st.user_id
            WHERE p.course_id = ? AND p.status = 'completed'
            ORDER BY p.paid_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, courseId);
    }

    public List<Payment> findByInstructor(Long instructorId) {
        String sql = """
            SELECT p.*, c.title AS course_title, u.full_name AS student_name,
                   NULL AS course_thumbnail, NULL AS student_internal_id
            FROM payments p
            JOIN courses c ON c.id = p.course_id
            JOIN students st ON st.id = p.student_id
            JOIN users u ON u.id = st.user_id
            WHERE c.instructor_id = ? AND p.status = 'completed'
            ORDER BY p.paid_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, instructorId);
    }

    public boolean existsCompleted(Long studentId, Long courseId) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM payments WHERE student_id = ? AND course_id = ? AND status = 'completed'",
            Integer.class, studentId, courseId);
        return count != null && count > 0;
    }

    public Long save(Payment payment) {
        String sql = """
            INSERT INTO payments (student_id, course_id, amount, payment_method, status, transaction_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        jdbc.update(sql, payment.getStudentId(), payment.getCourseId(),
                    payment.getAmount(), payment.getPaymentMethod(),
                    payment.getStatus(), payment.getTransactionId());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Map<String, Object> summaryStudent(Long studentId) {
        Map<String, Object> result = jdbc.queryForObject("""
            SELECT COALESCE(SUM(amount), 0) AS total_spent,
                   COUNT(id) AS total_payments,
                   COUNT(DISTINCT course_id) AS courses_purchased
            FROM payments
            WHERE student_id = ? AND status = 'completed'
            """,
            (rs, rowNum) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("total_spent", rs.getBigDecimal("total_spent"));
                m.put("total_payments", rs.getInt("total_payments"));
                m.put("courses_purchased", rs.getInt("courses_purchased"));
                return m;
            }, studentId);
        return result != null ? result : Map.of("total_spent", BigDecimal.ZERO,
                "total_payments", 0, "courses_purchased", 0);
    }

    public Map<String, Object> summaryInstructor(Long instructorId) {
        Map<String, Object> result = jdbc.queryForObject("""
            SELECT COALESCE(SUM(p.amount), 0) AS total_revenue,
                   COUNT(p.id) AS total_transactions,
                   COUNT(DISTINCT p.course_id) AS courses_sold,
                   COUNT(DISTINCT p.student_id) AS total_students
            FROM payments p
            JOIN courses c ON c.id = p.course_id
            WHERE c.instructor_id = ? AND p.status = 'completed'
            """,
            (rs, rowNum) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("total_revenue", rs.getBigDecimal("total_revenue"));
                m.put("total_transactions", rs.getInt("total_transactions"));
                m.put("courses_sold", rs.getInt("courses_sold"));
                m.put("total_students", rs.getInt("total_students"));
                return m;
            }, instructorId);
        return result != null ? result : Map.of("total_revenue", BigDecimal.ZERO,
                "total_transactions", 0, "courses_sold", 0, "total_students", 0);
    }
}
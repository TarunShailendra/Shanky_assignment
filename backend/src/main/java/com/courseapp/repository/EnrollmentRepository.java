package com.courseapp.repository;

import com.courseapp.domain.Enrollment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EnrollmentRepository {

    private final JdbcTemplate jdbc;

    public EnrollmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Enrollment> ROW_MAPPER = (rs, rowNum) -> mapEnrollment(rs);

    private static Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getLong("id"));
        e.setStudentId(rs.getLong("student_id"));
        e.setCourseId(rs.getLong("course_id"));
        e.setEnrolledAt(getTimestamp(rs, "enrolled_at"));
        e.setStatus(rs.getString("status"));

        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setThumbnail(rs.getString("thumbnail"));
        e.setPrice(getDecimal(rs, "price"));
        e.setCategory(rs.getString("category"));
        e.setInstructorName(rs.getString("instructor_name"));
        e.setModuleCount(getInt(rs, "module_count"));
        e.setAssignmentCount(getInt(rs, "assignment_count"));
        e.setFullName(rs.getString("full_name"));

        return e;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static Integer getInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    private static BigDecimal getDecimal(ResultSet rs, String col) throws SQLException {
        return rs.getBigDecimal(col);
    }

    public List<Enrollment> findByStudent(Long studentId) {
        String sql = """
            SELECT e.*, c.title, c.description, c.thumbnail, c.price, c.category,
                   u.full_name AS instructor_name,
                   (SELECT COUNT(*) FROM modules WHERE course_id = c.id) AS module_count,
                   (SELECT COUNT(*) FROM assignments a JOIN modules m ON m.id = a.module_id
                    WHERE m.course_id = c.id) AS assignment_count,
                   NULL AS full_name
            FROM enrollments e
            JOIN courses c ON c.id = e.course_id
            JOIN instructors i ON i.id = c.instructor_id
            JOIN users u ON u.id = i.user_id
            WHERE e.student_id = ?
            ORDER BY e.enrolled_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, studentId);
    }

    public List<Enrollment> findByCourse(Long courseId) {
        String sql = """
            SELECT e.*, u.full_name, st.id AS student_id,
                   NULL AS title, NULL AS description, NULL AS thumbnail,
                   NULL AS price, NULL AS category, NULL AS instructor_name,
                   NULL AS module_count, NULL AS assignment_count
            FROM enrollments e
            JOIN students st ON st.id = e.student_id
            JOIN users u ON u.id = st.user_id
            WHERE e.course_id = ? AND e.status = 'active'
            ORDER BY e.enrolled_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, courseId);
    }

    public Long enroll(Long studentId, Long courseId) {
        String checkSql = "SELECT id FROM enrollments WHERE student_id = ? AND course_id = ?";
        var existing = jdbc.query(checkSql, (rs, rowNum) -> rs.getLong("id"), studentId, courseId)
                           .stream().findFirst();
        if (existing.isPresent()) return null;

        String sql = "INSERT INTO enrollments (student_id, course_id, status) VALUES (?, ?, 'active')";
        jdbc.update(sql, studentId, courseId);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public int cancel(Long studentId, Long courseId) {
        String sql = """
            UPDATE enrollments SET status = 'cancelled'
            WHERE student_id = ? AND course_id = ? AND status = 'active'
            """;
        return jdbc.update(sql, studentId, courseId);
    }
}
package com.courseapp.repository;

import com.courseapp.domain.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepository {

    private final JdbcTemplate jdbc;

    public CourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Course> ROW_MAPPER = (rs, rowNum) -> mapCourse(rs);

    private static Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getLong("id"));
        c.setInstructorId(rs.getLong("instructor_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setPrice(getDecimal(rs, "price"));
        c.setThumbnail(rs.getString("thumbnail"));
        c.setIsPublished(getBool(rs, "is_published"));
        c.setCategory(rs.getString("category"));
        c.setCreatedAt(getTimestamp(rs, "created_at"));
        c.setUpdatedAt(getTimestamp(rs, "updated_at"));

        // Joined/computed fields
        c.setInstructorName(rs.getString("instructor_name"));
        c.setStudentCount(getInt(rs, "student_count"));
        c.setModuleCount(getInt(rs, "module_count"));
        c.setTotalRevenue(getDecimal(rs, "total_revenue"));

        return c;
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
        var val = rs.getBigDecimal(col);
        return val;
    }

    private static Boolean getBool(ResultSet rs, String col) throws SQLException {
        Object val = rs.getObject(col);
        if (val == null) return null;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() == 1;
        return Boolean.valueOf(val.toString());
    }

    public List<Course> findPublished() {
        String sql = """
            SELECT c.*, u.full_name AS instructor_name,
                   (SELECT COUNT(*) FROM enrollments WHERE course_id = c.id) AS student_count,
                   (SELECT COUNT(*) FROM modules WHERE course_id = c.id) AS module_count,
                   NULL AS total_revenue
            FROM courses c
            JOIN instructors i ON i.id = c.instructor_id
            JOIN users u ON u.id = i.user_id
            WHERE c.is_published = 1
            ORDER BY c.created_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER);
    }

    public List<Course> findByInstructor(Long instructorId) {
        String sql = """
            SELECT c.*,
                   (SELECT COUNT(*) FROM enrollments WHERE course_id = c.id) AS student_count,
                   (SELECT COUNT(*) FROM modules WHERE course_id = c.id) AS module_count,
                   (SELECT SUM(p.amount) FROM payments p WHERE p.course_id = c.id AND p.status = 'completed') AS total_revenue,
                   NULL AS instructor_name
            FROM courses c
            WHERE c.instructor_id = ?
            ORDER BY c.created_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, instructorId);
    }

    public Optional<Course> findById(Long id) {
        String sql = """
            SELECT c.*, u.full_name AS instructor_name, i.bio, i.expertise,
                   0 AS student_count, 0 AS module_count, NULL AS total_revenue
            FROM courses c
            JOIN instructors i ON i.id = c.instructor_id
            JOIN users u ON u.id = i.user_id
            WHERE c.id = ?
            """;
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Long> findInstructorId(Long courseId) {
        String sql = "SELECT instructor_id FROM courses WHERE id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("instructor_id"), courseId)
                   .stream().findFirst();
    }

    public Long save(Course course) {
        String sql = """
            INSERT INTO courses (instructor_id, title, description, price, category, is_published, thumbnail)
            VALUES (?, ?, ?, ?, ?, 0, '')
            """;
        jdbc.update(sql, course.getInstructorId(), course.getTitle(),
                    course.getDescription(), course.getPrice(),
                    course.getCategory());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public int update(Long id, Course updates) {
        String sql = """
            UPDATE courses SET
              title = COALESCE(?, title),
              description = COALESCE(?, description),
              price = COALESCE(?, price),
              category = COALESCE(?, category),
              is_published = COALESCE(?, is_published),
              thumbnail = COALESCE(?, thumbnail),
              updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        return jdbc.update(sql,
            updates.getTitle(), updates.getDescription(), updates.getPrice(),
            updates.getCategory(),
            updates.getIsPublished() != null ? (updates.getIsPublished() ? 1 : 0) : null,
            updates.getThumbnail(), id);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM courses WHERE id = ?", id);
    }
}
package com.courseapp.repository;

import com.courseapp.domain.Assignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AssignmentRepository {

    private final JdbcTemplate jdbc;

    public AssignmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Assignment> ROW_MAPPER = (rs, rowNum) -> mapAssignment(rs);

    private static Assignment mapAssignment(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getLong("id"));
        a.setModuleId(rs.getLong("module_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setDueDate(getTimestamp(rs, "due_date"));
        a.setMaxScore(getInt(rs, "max_score"));
        a.setCreatedAt(getTimestamp(rs, "created_at"));
        a.setModuleTitle(rs.getString("module_title"));
        a.setCourseId(getLong(rs, "course_id"));
        a.setCourseTitle(rs.getString("course_title"));
        a.setSubmissionCount(getInt(rs, "submission_count"));
        return a;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static Integer getInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    private static Long getLong(ResultSet rs, String col) throws SQLException {
        long val = rs.getLong(col);
        return rs.wasNull() ? null : val;
    }

    public List<Assignment> findByModule(Long moduleId) {
        String sql = "SELECT * FROM assignments WHERE module_id = ? ORDER BY created_at";
        return jdbc.query(sql, ROW_MAPPER, moduleId);
    }

    public Optional<Assignment> findById(Long id) {
        String sql = """
            SELECT a.*, m.title AS module_title, m.course_id, c.title AS course_title,
                   0 AS submission_count
            FROM assignments a
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            WHERE a.id = ?
            """;
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Long> findInstructorId(Long assignmentId) {
        String sql = """
            SELECT c.instructor_id FROM assignments a
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            WHERE a.id = ?
            """;
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("instructor_id"), assignmentId)
                   .stream().findFirst();
    }

    public Long save(Assignment a) {
        String sql = """
            INSERT INTO assignments (module_id, title, description, due_date, max_score)
            VALUES (?, ?, ?, ?, ?)
            """;
        jdbc.update(sql, a.getModuleId(), a.getTitle(),
                    a.getDescription(),
                    a.getDueDate() != null ? Timestamp.valueOf(a.getDueDate()) : null,
                    a.getMaxScore() != null ? a.getMaxScore() : 100);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public int update(Long id, Assignment updates) {
        String sql = """
            UPDATE assignments SET
              title = COALESCE(?, title),
              description = COALESCE(?, description),
              due_date = COALESCE(?, due_date),
              max_score = COALESCE(?, max_score)
            WHERE id = ?
            """;
        return jdbc.update(sql,
            updates.getTitle(), updates.getDescription(),
            updates.getDueDate() != null ? Timestamp.valueOf(updates.getDueDate()) : null,
            updates.getMaxScore(), id);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM assignments WHERE id = ?", id);
    }
}
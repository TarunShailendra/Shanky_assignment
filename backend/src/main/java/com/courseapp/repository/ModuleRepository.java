package com.courseapp.repository;

import com.courseapp.domain.Module;
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
public class ModuleRepository {

    private final JdbcTemplate jdbc;

    public ModuleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Module> ROW_MAPPER = (rs, rowNum) -> mapModule(rs);

    private static Module mapModule(ResultSet rs) throws SQLException {
        Module m = new Module();
        m.setId(rs.getLong("id"));
        m.setCourseId(rs.getLong("course_id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setOrderIndex(getInt(rs, "order_index"));
        m.setCreatedAt(getTimestamp(rs, "created_at"));
        m.setAssignmentCount(getInt(rs, "assignment_count"));
        return m;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static Integer getInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    public List<Module> findByCourse(Long courseId) {
        String sql = """
            SELECT m.*,
                   (SELECT COUNT(*) FROM assignments WHERE module_id = m.id) AS assignment_count
            FROM modules m
            WHERE m.course_id = ?
            ORDER BY m.order_index
            """;
        return jdbc.query(sql, ROW_MAPPER, courseId);
    }

    public Optional<Module> findById(Long id) {
        String sql = "SELECT m.*, 0 AS assignment_count FROM modules m WHERE m.id = ?";
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Long> findInstructorId(Long moduleId) {
        String sql = """
            SELECT c.instructor_id FROM modules m
            JOIN courses c ON c.id = m.course_id
            WHERE m.id = ?
            """;
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("instructor_id"), moduleId)
                   .stream().findFirst();
    }

    public int getNextOrder(Long courseId) {
        Integer max = jdbc.queryForObject(
            "SELECT COALESCE(MAX(order_index), -1) + 1 FROM modules WHERE course_id = ?",
            Integer.class, courseId);
        return max != null ? max : 0;
    }

    public Long save(Module module) {
        String sql = "INSERT INTO modules (course_id, title, description, order_index) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, module.getCourseId(), module.getTitle(),
                    module.getDescription(), module.getOrderIndex());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public int update(Long id, Module updates) {
        String sql = """
            UPDATE modules SET
              title = COALESCE(?, title),
              description = COALESCE(?, description),
              order_index = COALESCE(?, order_index)
            WHERE id = ?
            """;
        return jdbc.update(sql, updates.getTitle(), updates.getDescription(),
                           updates.getOrderIndex(), id);
    }

    public int delete(Long id) {
        return jdbc.update("DELETE FROM modules WHERE id = ?", id);
    }
}
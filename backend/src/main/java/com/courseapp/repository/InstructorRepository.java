package com.courseapp.repository;

import com.courseapp.domain.Instructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class InstructorRepository {

    private final JdbcTemplate jdbc;

    public InstructorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Instructor> ROW_MAPPER = (rs, rowNum) -> {
        Instructor i = new Instructor();
        i.setId(rs.getLong("id"));
        i.setUserId(rs.getLong("user_id"));
        i.setBio(rs.getString("bio"));
        i.setExpertise(rs.getString("expertise"));
        i.setCreatedAt(getTimestamp(rs, "created_at"));
        return i;
    };

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public Optional<Instructor> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, bio, expertise, created_at FROM instructors WHERE user_id = ?";
        return jdbc.query(sql, ROW_MAPPER, userId).stream().findFirst();
    }

    public Long save(Long userId) {
        String sql = "INSERT INTO instructors (user_id) VALUES (?)";
        jdbc.update(sql, userId);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Optional<Long> findIdByUserId(Long userId) {
        String sql = "SELECT id FROM instructors WHERE user_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("id"), userId).stream().findFirst();
    }

    public Optional<Instructor> findById(Long id) {
        String sql = "SELECT id, user_id, bio, expertise, created_at FROM instructors WHERE id = ?";
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Instructor> findWithProfile(Long userId) {
        String sql = "SELECT id, user_id, bio, expertise, created_at FROM instructors WHERE user_id = ?";
        return jdbc.query(sql, ROW_MAPPER, userId).stream().findFirst();
    }
}
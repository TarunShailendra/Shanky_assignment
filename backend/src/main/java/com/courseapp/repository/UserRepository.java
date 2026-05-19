package com.courseapp.repository;

import com.courseapp.domain.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(getLocalDateTime(rs, "created_at"));
        return u;
    };

    private static LocalDateTime getLocalDateTime(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, email, password_hash, full_name, role, created_at FROM users WHERE email = ?";
        return jdbc.query(sql, ROW_MAPPER, email).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, email, password_hash, full_name, role, created_at FROM users WHERE id = ?";
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Long save(User user) {
        String sql = "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, user.getEmail(), user.getPasswordHash(), user.getFullName(), user.getRole());
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public int updatePasswordHash(String email, String hash) {
        return jdbc.update("UPDATE users SET password_hash = ? WHERE email = ?", hash, email);
    }
}
package com.courseapp.repository;

import com.courseapp.domain.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Student> ROW_MAPPER = (rs, rowNum) -> {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setUserId(rs.getLong("user_id"));
        Date dob = rs.getDate("date_of_birth");
        s.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        s.setCreatedAt(getTimestamp(rs, "created_at"));
        return s;
    };

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public Long save(Long userId) {
        String sql = "INSERT INTO students (user_id) VALUES (?)";
        jdbc.update(sql, userId);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Optional<Student> findById(Long id) {
        String sql = "SELECT id, user_id, date_of_birth, phone, address, created_at FROM students WHERE id = ?";
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Student> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, date_of_birth, phone, address, created_at FROM students WHERE user_id = ?";
        return jdbc.query(sql, ROW_MAPPER, userId).stream().findFirst();
    }

    public Optional<Long> findIdByUserId(Long userId) {
        String sql = "SELECT id FROM students WHERE user_id = ?";
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("id"), userId).stream().findFirst();
    }

    public Optional<Student> findWithProfile(Long userId) {
        String sql = "SELECT id, user_id, date_of_birth, phone, address, created_at FROM students WHERE user_id = ?";
        return jdbc.query(sql, ROW_MAPPER, userId).stream().findFirst();
    }
}
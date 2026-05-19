package com.courseapp.repository;

import com.courseapp.domain.Submission;
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
public class SubmissionRepository {

    private final JdbcTemplate jdbc;

    public SubmissionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Submission> ROW_MAPPER = (rs, rowNum) -> mapSubmission(rs);

    private static Submission mapSubmission(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setId(rs.getLong("id"));
        s.setAssignmentId(rs.getLong("assignment_id"));
        s.setStudentId(rs.getLong("student_id"));
        s.setSubmissionText(rs.getString("submission_text"));
        s.setFileUrl(rs.getString("file_url"));
        s.setScore(getInt(rs, "score"));
        s.setFeedback(rs.getString("feedback"));
        s.setSubmittedAt(getTimestamp(rs, "submitted_at"));
        s.setGradedAt(getTimestamp(rs, "graded_at"));
        s.setStudentName(rs.getString("student_name"));
        s.setAssignmentTitle(rs.getString("assignment_title"));
        s.setAssignmentDesc(rs.getString("assignment_desc"));
        s.setDueDate(getTimestamp(rs, "due_date"));
        s.setMaxScore(getInt(rs, "max_score"));
        s.setModuleTitle(rs.getString("module_title"));
        s.setCourseTitle(rs.getString("course_title"));
        return s;
    }

    private static LocalDateTime getTimestamp(ResultSet rs, String col) throws SQLException {
        var ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private static Integer getInt(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : val;
    }

    public List<Submission> findByStudent(Long studentId) {
        String sql = """
            SELECT s.*, a.title AS assignment_title, a.description AS assignment_desc,
                   a.due_date, a.max_score, m.title AS module_title, c.title AS course_title,
                   NULL AS student_name
            FROM submissions s
            JOIN assignments a ON a.id = s.assignment_id
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            WHERE s.student_id = ?
            ORDER BY s.submitted_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, studentId);
    }

    public List<Submission> findByInstructor(Long instructorId) {
        String sql = """
            SELECT s.*, a.title AS assignment_title, a.description AS assignment_desc,
                   a.due_date, a.max_score, m.title AS module_title, c.title AS course_title,
                   u.full_name AS student_name
            FROM submissions s
            JOIN assignments a ON a.id = s.assignment_id
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            JOIN students st ON st.id = s.student_id
            JOIN users u ON u.id = st.user_id
            WHERE c.instructor_id = ?
            ORDER BY s.submitted_at DESC
            """;
        return jdbc.query(sql, ROW_MAPPER, instructorId);
    }

    public List<Submission> findByAssignment(Long assignmentId) {
        String sql = """
            SELECT s.*, u.full_name AS student_name,
                   NULL AS assignment_title, NULL AS assignment_desc, NULL AS due_date,
                   NULL AS max_score, NULL AS module_title, NULL AS course_title
            FROM submissions s
            JOIN students st ON st.id = s.student_id
            JOIN users u ON u.id = st.user_id
            WHERE s.assignment_id = ?
            ORDER BY s.submitted_at
            """;
        return jdbc.query(sql, ROW_MAPPER, assignmentId);
    }

    public Optional<Submission> findById(Long id) {
        String sql = """
            SELECT s.*, u.full_name AS student_name, a.title AS assignment_title,
                   a.max_score, m.title AS module_title, c.title AS course_title,
                   NULL AS assignment_desc, NULL AS due_date
            FROM submissions s
            JOIN students st ON st.id = s.student_id
            JOIN users u ON u.id = st.user_id
            JOIN assignments a ON a.id = s.assignment_id
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            WHERE s.id = ?
            """;
        return jdbc.query(sql, ROW_MAPPER, id).stream().findFirst();
    }

    public Optional<Long> findInstructorId(Long submissionId) {
        String sql = """
            SELECT c.instructor_id FROM submissions s
            JOIN assignments a ON a.id = s.assignment_id
            JOIN modules m ON m.id = a.module_id
            JOIN courses c ON c.id = m.course_id
            WHERE s.id = ?
            """;
        return jdbc.query(sql, (rs, rowNum) -> rs.getLong("instructor_id"), submissionId)
                   .stream().findFirst();
    }

    public Long upsert(Submission s) {
        String checkSql = "SELECT id FROM submissions WHERE assignment_id = ? AND student_id = ?";
        Optional<Long> existing = jdbc.query(checkSql,
            (rs, rowNum) -> rs.getLong("id"), s.getAssignmentId(), s.getStudentId())
            .stream().findFirst();

        if (existing.isPresent()) {
            String updateSql = """
                UPDATE submissions SET
                  submission_text = ?, file_url = ?, submitted_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
            jdbc.update(updateSql,
                s.getSubmissionText() != null ? s.getSubmissionText() : "",
                s.getFileUrl() != null ? s.getFileUrl() : "",
                existing.get());
            return existing.get();
        } else {
            String insertSql = """
                INSERT INTO submissions (assignment_id, student_id, submission_text, file_url)
                VALUES (?, ?, ?, ?)
                """;
            jdbc.update(insertSql,
                s.getAssignmentId(), s.getStudentId(),
                s.getSubmissionText() != null ? s.getSubmissionText() : "",
                s.getFileUrl() != null ? s.getFileUrl() : "");
            return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        }
    }

    public int grade(Long id, Integer score, String feedback) {
        String sql = """
            UPDATE submissions SET score = ?, feedback = ?, graded_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        return jdbc.update(sql, score, feedback != null ? feedback : "", id);
    }
}
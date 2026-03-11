package ru.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.domain.Comment;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findAllByPostId(long postId) {
        return jdbcTemplate.query(
                "SELECT id, text, post_id FROM comments WHERE post_id = ? ORDER BY id",
                (rs, rowNum) -> new Comment(rs.getLong("id"), rs.getString("text"), rs.getLong("post_id")),
                postId
        );
    }

    @Override
    public Optional<Comment> findByIdAndPostId(long commentId, long postId) {
        Comment result = jdbcTemplate.query(
                "SELECT id, text, post_id FROM comments WHERE id = ? AND post_id = ?",
                rs -> rs.next() ? new Comment(rs.getLong("id"), rs.getString("text"), rs.getLong("post_id")) : null,
                commentId, postId);
        return Optional.ofNullable(result);
    }

    @Override
    public void create(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO comments (text, post_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);
        comment.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }
}

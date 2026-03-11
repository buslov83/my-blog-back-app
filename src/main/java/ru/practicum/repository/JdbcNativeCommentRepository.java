package ru.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.domain.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findAllByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT id, text, post_id FROM comments WHERE post_id = ? ORDER BY id",
                (rs, rowNum) -> new Comment(rs.getLong("id"), rs.getString("text"), rs.getLong("post_id")),
                postId
        );
    }

    @Override
    public Optional<Comment> findByIdAndPostId(Long commentId, Long postId) {
        Comment result = jdbcTemplate.query(
                "SELECT id, text, post_id FROM comments WHERE id = ? AND post_id = ?",
                rs -> rs.next() ? new Comment(rs.getLong("id"), rs.getString("text"), rs.getLong("post_id")) : null,
                commentId, postId);
        return Optional.ofNullable(result);
    }
}

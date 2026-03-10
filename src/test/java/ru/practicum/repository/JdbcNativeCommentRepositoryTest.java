package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.configuration.DataSourceConfiguration;
import ru.practicum.domain.Comment;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, JdbcNativeCommentRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@ActiveProfiles("test")
class JdbcNativeCommentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM posts"); // cascades to comments
        // Reset identity sequence so DB generated IDs don't overlap with the explicit IDs below
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 50");

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                1L, "Post with two comments", "text", 0
        );
        jdbcTemplate.update("INSERT INTO comments (id, text, post_id) VALUES (?,?,?)", 1L, "First comment", 1L);
        jdbcTemplate.update("INSERT INTO comments (id, text, post_id) VALUES (?,?,?)", 2L, "Second comment", 1L);

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count) VALUES (?,?,?,?)",
                2L, "Post with no comments", "text", 0
        );
    }

    @Test
    void findAllByPostId_postWithComments_returnsAllComments() {
        List<Comment> comments = commentRepository.findAllByPostId(1L);
        assertEquals(2, comments.size());
        assertThat(comments, everyItem(hasProperty("postId", equalTo(1L))));
        assertEquals("First comment", comments.getFirst().getText());
        assertEquals("Second comment", comments.getLast().getText());
    }

    @Test
    void findAllByPostId_postWithNoComments_returnsEmptyList() {
        List<Comment> comments = commentRepository.findAllByPostId(2L);
        assertTrue(comments.isEmpty());
    }

    @Test
    void findAllByPostId_nonExistentPost_returnsEmptyList() {
        List<Comment> comments = commentRepository.findAllByPostId(999L);
        assertTrue(comments.isEmpty());
    }
}

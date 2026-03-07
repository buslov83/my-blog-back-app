package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.configuration.DataSourceConfiguration;
import ru.practicum.domain.Post;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, JdbcNativePostRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
class JdbcNativePostRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM posts"); // cascades to comments

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count, tags) VALUES (?,?,?,?,?)",
                1L, "Spring MVC Guide", "Spring text", 3, " java spring "
        );

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count, tags) VALUES (?,?,?,?,?)",
                2L, "Hibernate ORM Tutorial", "Hibernate text", 1, " java orm hibernate "
        );
        jdbcTemplate.update("INSERT INTO comments (text, post_id) VALUES (?,?)", "Nice post", 2L);
        jdbcTemplate.update("INSERT INTO comments (text, post_id) VALUES (?,?)", "Thanks for sharing", 2L);

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count, tags) VALUES (?,?,?,?,?)",
                3L, "Design Patterns in Java", "Patterns text", 7, " java design oop patterns "
        );

        jdbcTemplate.update(
                "INSERT INTO posts (id, title, text, likes_count, tags) VALUES (?,?,?,?,?)",
                4L, "How to springify your app", "Spring Boot text", 0, null
        );
    }

    @Test
    void findAll_noFilters_returnsAllPosts() {
        List<Post> posts = postRepository.findAll("", List.of(), 0, 10);
        assertEquals(4, posts.size());
        assertEquals(4L, posts.getFirst().getId()); // ORDER BY id DESC
        assertEquals(1L, posts.getLast().getId());
    }

    @Test
    void count_noFilters_returnsTotal() {
        assertEquals(4, postRepository.count("", List.of()));
    }

    @Test
    void findAll_noRecords_returnsEmptyList() {
        List<Post> posts = postRepository.findAll("nonexistent", List.of(), 0, 10);
        assertTrue(posts.isEmpty());
    }

    @Test
    void count_noRecords_returnsZero() {
        assertEquals(0, postRepository.count("nonexistent", List.of()));
    }

    @Test
    void findAll_byTitleSearch_returnsMatchingPosts() {
        List<Post> posts = postRepository.findAll("spring", List.of(), 0, 10);
        assertEquals(2, posts.size());
        assertEquals("How to springify your app", posts.getFirst().getTitle());
        assertEquals("Spring MVC Guide", posts.getLast().getTitle());
    }

    @Test
    void count_byTitleSearch_returnsMatchingCount() {
        assertEquals(2, postRepository.count("spring", List.of()));
    }

    @Test
    void findAll_byTwoWordTitleSearch_returnsMatchingPosts() {
        List<Post> posts = postRepository.findAll("spring mvc", List.of(), 0, 10);
        assertEquals(1, posts.size());
        assertEquals("Spring MVC Guide", posts.getFirst().getTitle());
    }

    @Test
    void count_byTwoWordTitleSearch_returnsMatchingCount() {
        assertEquals(1, postRepository.count("spring mvc", List.of()));
    }

    @Test
    void findAll_bySingleTag_returnsMatchingPosts() {
        List<Post> posts = postRepository.findAll("", List.of("java"), 0, 10);
        assertEquals(3, posts.size());
        assertThat(posts, everyItem(hasProperty("tags", hasItem("java"))));
    }

    @Test
    void count_bySingleTag_returnsMatchingCount() {
        assertEquals(3, postRepository.count("", List.of("java")));
    }

    @Test
    void findAll_byMultipleTags_andLogic() {
        List<Post> posts = postRepository.findAll("", List.of("hibernate", "java"), 0, 10);
        assertEquals(1, posts.size());
        assertEquals("Hibernate ORM Tutorial", posts.getFirst().getTitle());
    }

    @Test
    void count_byMultipleTags_andLogic() {
        assertEquals(1, postRepository.count("", List.of("hibernate", "java")));
    }

    @Test
    void findAll_mappedFieldsAreCorrect() {
        List<Post> posts = postRepository.findAll("", List.of("orm"), 0, 10);
        assertEquals(1, posts.size());
        Post post = posts.getFirst();
        assertEquals(2L, post.getId());
        assertEquals("Hibernate ORM Tutorial", post.getTitle());
        assertEquals("Hibernate text", post.getText());
        assertEquals(1, post.getLikesCount());
        assertThat(post.getTags(), contains("java", "orm", "hibernate"));
        assertNull(post.getPostImage());
        assertEquals(2, post.getCommentsCount());
    }

    @Test
    void findAll_offsetAndLimit_returnsCorrectSlice() {
        // ORDER BY id DESC => ids 4, 3, 2, 1; offset=1, limit=2 should return posts 3 and 2
        List<Post> posts = postRepository.findAll("", List.of(), 1, 2);
        assertEquals(2, posts.size());
        assertEquals(3L, posts.getFirst().getId());
        assertEquals(2L, posts.getLast().getId());
    }

    @Test
    void findAll_nullTags_mappedToEmptyList() {
        List<Post> posts = postRepository.findAll("springify", List.of(), 0, 10);
        assertEquals(1, posts.size());
        assertTrue(posts.getFirst().getTags().isEmpty());
    }
}

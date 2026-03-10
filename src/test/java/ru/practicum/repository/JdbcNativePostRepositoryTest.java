package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.configuration.DataSourceConfiguration;
import ru.practicum.domain.Post;

import java.util.List;
import java.util.Optional;

import ru.practicum.domain.PostImage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, JdbcNativePostRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@ActiveProfiles("test")
class JdbcNativePostRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM posts"); // cascades to comments
        // Reset identity sequence so DB generated IDs don't overlap with the explicit IDs below
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 50");

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
    void findAll_byMultiWordTitleSearch_returnsMatchingPosts() {
        List<Post> posts = postRepository.findAll("spring mvc", List.of(), 0, 10);
        assertEquals(1, posts.size());
        assertEquals("Spring MVC Guide", posts.getFirst().getTitle());
    }

    @Test
    void count_byMultiWordTitleSearch_returnsMatchingCount() {
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
    void findAll_mixedSearch_returnsMatchingPosts() {
        List<Post> posts = postRepository.findAll("spring", List.of("java"), 0, 10);
        assertEquals(1, posts.size());
        assertEquals("Spring MVC Guide", posts.getFirst().getTitle());
    }

    @Test
    void count_mixedSearch_returnsMatchingCount() {
        assertEquals(1, postRepository.count("spring", List.of("java")));
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
    void findAll_returnedNullTags_mappedToEmptyList() {
        List<Post> posts = postRepository.findAll("springify", List.of(), 0, 10);
        assertEquals(1, posts.size());
        assertTrue(posts.getFirst().getTags().isEmpty());
    }

    @Test
    void findById_existingId_returnsNonEmptyOptional() {
        assertTrue(postRepository.findById(1L).isPresent());
    }

    @Test
    void findById_nonExistentId_returnsEmpty() {
        assertTrue(postRepository.findById(999L).isEmpty());
    }

    @Test
    void findById_allFieldsMappedCorrectly() {
        Post post = postRepository.findById(2L).orElseThrow();

        assertEquals(2L, post.getId());
        assertEquals("Hibernate ORM Tutorial", post.getTitle());
        assertEquals("Hibernate text", post.getText());
        assertEquals(1, post.getLikesCount());
        assertThat(post.getTags(), contains("java", "orm", "hibernate"));
        assertEquals(2, post.getCommentsCount());
        assertNull(post.getPostImage());
    }

    @Test
    void findById_postWithNullTags_returnsEmptyTagList() {
        Post post = postRepository.findById(4L).orElseThrow();

        assertTrue(post.getTags().isEmpty());
    }

    @Test
    void findById_commentsCount() {
        assertEquals(0, postRepository.findById(1L).orElseThrow().getCommentsCount());
        assertEquals(2, postRepository.findById(2L).orElseThrow().getCommentsCount());
    }

    @Test
    void existsById_existingId() {
        assertTrue(postRepository.existsById(1L));
    }

    @Test
    void existsById_nonExistentId() {
        assertFalse(postRepository.existsById(999L));
    }

    @Test
    void create_withTags_persistsPostAndSetsId() {
        Post post = new Post(null, "New Post", "New text", 0, List.of("foo", "bar"), 0);

        postRepository.create(post);

        assertNotNull(post.getId());
        Optional<Post> saved = postRepository.findById(post.getId());
        assertTrue(saved.isPresent());
        Post found = saved.get();
        assertEquals("New Post", found.getTitle());
        assertEquals("New text", found.getText());
        assertEquals(0, found.getLikesCount());
        assertThat(found.getTags(), contains("foo", "bar"));
        assertEquals(0, found.getCommentsCount());
    }

    @Test
    void create_withEmptyTags_persistsPostWithNoTags() {
        Post post = new Post(null, "No Tags Post", "Some text", 0, List.of(), 0);

        postRepository.create(post);

        assertNotNull(post.getId());
        Optional<Post> saved = postRepository.findById(post.getId());
        assertTrue(saved.isPresent());
        assertTrue(saved.get().getTags().isEmpty());
    }

    @Test
    void create_withNullTags_persistsPostWithNoTags() {
        Post post = new Post(null, "Null Tags Post", "Some text", 0, null, 0);

        postRepository.create(post);

        assertNotNull(post.getId());
        Optional<Post> saved = postRepository.findById(post.getId());
        assertTrue(saved.isPresent());
        assertTrue(saved.get().getTags().isEmpty());
    }

    @Test
    void create_tagsStoredWithSpacePaddingAllowTagFiltering() {
        Post post = new Post(null, "Tag Filter Test", "text", 0, List.of("bar"), 0);

        postRepository.create(post);

        List<Post> found = postRepository.findAll("", List.of("bar"), 0, 10);
        assertEquals(1, found.size());
        assertEquals("Tag Filter Test", found.getFirst().getTitle());
    }

    @Test
    void update_existingPost_updatesFields() {
        Post updated = new Post(2L, "Updated Title", "Updated text", 0, List.of("bar", "foo"), 0);

        postRepository.update(updated);

        Post found = postRepository.findById(2L).orElseThrow();
        assertEquals("Updated Title", found.getTitle());
        assertEquals("Updated text", found.getText());
        assertThat(found.getTags(), contains("bar", "foo"));
        assertEquals(1, found.getLikesCount()); // unchanged
        assertEquals(2, found.getCommentsCount()); // unchanged
    }

    @Test
    void update_tagsStoredWithSpacePaddingAllowTagFiltering() {
        Post post = new Post(1L, "Spring MVC Guide", "Spring text", 0, List.of("bar"), 0);

        postRepository.update(post);

        List<Post> found = postRepository.findAll("", List.of("bar"), 0, 10);
        assertEquals(1, found.size());
        assertEquals("Spring MVC Guide", found.getFirst().getTitle());
    }

    @Test
    void update_emptyTags_storesNullAndReturnsEmptyList() {
        Post post = new Post(2L, "Hibernate ORM Tutorial", "Hibernate text", 0, List.of(), 0);

        postRepository.update(post);

        Post found = postRepository.findById(2L).orElseThrow();
        assertTrue(found.getTags().isEmpty());
    }

    @Test
    void delete_existingPost_returnsTrueAndPostIsDeleted() {
        assertTrue(postRepository.delete(1L));
        assertTrue(postRepository.findById(1L).isEmpty());
    }

    @Test
    void delete_existingPostWithComments_cascadesDelete() {
        // post 2 has 2 comments
        assertTrue(postRepository.delete(2L));
        assertTrue(postRepository.findById(2L).isEmpty());
        Integer commentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE post_id = ?", Integer.class, 2L);
        assertEquals(0, commentCount);
    }

    @Test
    void delete_nonExistingPost_returnsFalse() {
        assertFalse(postRepository.delete(999L));
    }

    @Test
    void findImageById_nonExistentPost_returnsEmptyOptional() {
        assertTrue(postRepository.findImageById(999L).isEmpty());
    }

    @Test
    void findImageById_existingPostWithNoImage_returnsOptionalWithNullData() {
        PostImage postImage = postRepository.findImageById(1L).orElseThrow();
        assertNull(postImage.data());
        assertNull(postImage.contentType());
    }

    @Test
    void updateImage_existingPost_updatesImageDataAndContentType() {
        byte[] imageData = new byte[]{10, 20, 30, 40};
        String contentType = "image/jpeg";

        postRepository.updateImage(1L, imageData, contentType);

        PostImage postImage = postRepository.findImageById(1L).orElseThrow();
        assertArrayEquals(imageData, postImage.data());
        assertEquals(contentType, postImage.contentType());
    }
}

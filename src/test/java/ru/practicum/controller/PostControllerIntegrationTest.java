package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.WebConfiguration;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {
        WebConfiguration.class,
        PostControllerIntegrationTest.TestConfig.class
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
@ActiveProfiles("test")
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();

        jdbcTemplate.execute("DELETE FROM posts"); // cascades to comments
        // Reset identity sequence so DB generated IDs don't overlap with the explicit IDs below
        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 50");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 50");

        jdbcTemplate.execute("""
                INSERT INTO posts (id, title, text, image, image_content_type, likes_count, tags)
                VALUES (1, 'Введение в Spring Framework',
                        'Spring Framework — это мощный Java-фреймворк для создания enterprise-приложений. Он предоставляет инверсию управления (IoC) и внедрение зависимостей (DI), что упрощает разработку и тестирование кода.',
                        NULL, NULL, 3, NULL)
                """);
        jdbcTemplate.execute("""
                INSERT INTO posts (id, title, text, image, image_content_type, likes_count, tags)
                VALUES (2, 'Работа с Hibernate ORM',
                        'Hibernate — популярный ORM-фреймворк для Java, который позволяет работать с реляционными базами данных через объектно-ориентированную модель. Основные концепции: Session, Transaction и HQL.',
                        NULL, NULL, 1, ' java orm hibernate ')
                """);
        jdbcTemplate.execute("""
                INSERT INTO posts (id, title, text, image, image_content_type, likes_count, tags)
                VALUES (3, 'Паттерны проектирования в Java',
                        'Паттерны проектирования — это типовые решения часто встречающихся проблем при проектировании программ. В Java широко применяются паттерны: Singleton, Factory, Builder, Strategy и Observer.',
                        NULL, NULL, 7, ' java design oop patterns ')
                """);

        jdbcTemplate.execute("INSERT INTO comments (id, text, post_id) VALUES (1, 'Отличная статья, очень доступно объяснено!', 1)");
        jdbcTemplate.execute("INSERT INTO comments (id, text, post_id) VALUES (2, 'Спасибо, давно искал хорошее введение в Spring.', 1)");
        jdbcTemplate.execute("INSERT INTO comments (id, text, post_id) VALUES (3, 'Полезный материал про Hibernate, жду продолжения.', 2)");
    }

    @Test
    void getPosts_emptySearch_returnsAllPostsSortedByIdDescWithTruncatedText() throws Exception {
        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts", hasSize(3)))
                .andExpect(jsonPath("$.posts[0].title").value("Паттерны проектирования в Java"))
                .andExpect(jsonPath("$.posts[0].text").value(endsWith("...")))
                .andExpect(jsonPath("$.posts[0].commentsCount").value(0))
                .andExpect(jsonPath("$.posts[0].likesCount").value(7))
                .andExpect(jsonPath("$.posts[0].tags", contains("java", "design", "oop", "patterns")))
                .andExpect(jsonPath("$.posts[1].title").value("Работа с Hibernate ORM"))
                .andExpect(jsonPath("$.posts[1].text").value(endsWith("...")))
                .andExpect(jsonPath("$.posts[2].title").value("Введение в Spring Framework"))
                .andExpect(jsonPath("$.posts[2].commentsCount").value(2))
                .andExpect(jsonPath("$.posts[2].tags", hasSize(0)))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    void getPosts_titleSearch() throws Exception {
        mockMvc.perform(get("/api/posts?search=Hibernate&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Работа с Hibernate ORM"));
    }

    @Test
    void getPosts_tagSearch() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "#java")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(2)))
                .andExpect(jsonPath("$.posts[0].title").value("Паттерны проектирования в Java"))
                .andExpect(jsonPath("$.posts[1].title").value("Работа с Hibernate ORM"));
    }

    @Test
    void getPosts_mixedSearch() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("search", "hibernate #java orm")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Работа с Hibernate ORM"));
    }

    @Test
    void getPosts_pagination() throws Exception {
        mockMvc.perform(get("/api/posts?search=&pageNumber=2&pageSize=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(1)))
                .andExpect(jsonPath("$.posts[0].title").value("Введение в Spring Framework"))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(2));
    }

    @Test
    void getPosts_noMatches_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/posts?search=nonexistent&pageNumber=1&pageSize=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(0)))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    void getPost_found_returnsFullText() throws Exception {
        mockMvc.perform(get("/api/posts/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Работа с Hibernate ORM"))
                .andExpect(jsonPath("$.text").value(not(endsWith("..."))))
                .andExpect(jsonPath("$.likesCount").value(1))
                .andExpect(jsonPath("$.commentsCount").value(1))
                .andExpect(jsonPath("$.tags", contains("java", "orm", "hibernate")));
    }

    @Test
    void getPost_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost_returnsCreated_andPersists() throws Exception {
        String json = """
                {"title":"Новый пост","text":"Текст нового поста","tags":[]}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Новый пост"))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0))
                .andExpect(jsonPath("$.tags", hasSize(0)));

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts", hasSize(4)))
                .andExpect(jsonPath("$.posts[0].title").value("Новый пост"))
                .andExpect(jsonPath("$.posts[0].text").value("Текст нового поста"))
                .andExpect(jsonPath("$.posts[0].likesCount").value(0))
                .andExpect(jsonPath("$.posts[0].commentsCount").value(0))
                .andExpect(jsonPath("$.posts[0].tags", hasSize(0)));
    }

    @Test
    void createPost_withTags_tagsReturned() throws Exception {
        String json = """
                {"title":"Tagged Post","text":"Some text","tags":["spring","boot"]}
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags", contains("spring", "boot")));

        mockMvc.perform(get("/api/posts?search=&pageNumber=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].title").value("Tagged Post"))
                .andExpect(jsonPath("$.posts[0].tags", contains("spring", "boot")));
    }

    @Test
    void updatePost_existingPost_returnsUpdatedPost() throws Exception {
        String json = """
                {"id":1,"title":"Updated Title","text":"Updated text content","tags":["foo","bar"]}
                """;

        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.text").value("Updated text content"))
                .andExpect(jsonPath("$.tags", contains("foo", "bar")))
                .andExpect(jsonPath("$.likesCount").value(3))
                .andExpect(jsonPath("$.commentsCount").value(2));
    }

    @Test
    void updatePost_nonExistingPost_returns404() throws Exception {
        String json = """
                {"id":999,"title":"Updated Title","text":"Updated text","tags":[]}
                """;

        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_existingPost_returns204AndPostIsGone() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 1L))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_nonExistingPost_returns404() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void incrementLikes_existingPost_returnsNewCount() throws Exception {
        // post 1 has likes_count = 3 initially
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("4"));
    }

    @Test
    void incrementLikes_nonExistingPost_returns404() throws Exception {
        mockMvc.perform(post("/api/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAndGetImage_success() throws Exception {
        byte[] pngStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", pngStub);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L).file(file))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(pngStub));
    }

    @Test
    void updateImage_emptyFile_returns400() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L).file(empty))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Empty file"));
    }

    @Test
    void updateImage_postNotFound_returns404() throws Exception {
        byte[] pngStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "photo.png", "image/png", pngStub);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/posts/{id}/image", 999L).file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_postWithoutImage_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_postNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_found_returnsListSortedByIdAsc() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].postId").value(1))
                .andExpect(jsonPath("$[0].text").value("Отличная статья, очень доступно объяснено!"))
                .andExpect(jsonPath("$[1].postId").value(1))
                .andExpect(jsonPath("$[1].text").value("Спасибо, давно искал хорошее введение в Spring."));
    }

    @Test
    void getComments_noComments_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getComments_postNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComment_found_returnsComment() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments/{cid}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.text").value("Отличная статья, очень доступно объяснено!"));
    }

    @Test
    void getComment_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments/{cid}", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComment_commentBelongsToDifferentPost_returns404() throws Exception {
        // comment 3 belongs to post 2, not post 1
        mockMvc.perform(get("/api/posts/{id}/comments/{cid}", 1L, 3L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_success_returns201WithComment() throws Exception {
        String json = """
                {"text":"Hello","postId":1}
                """;

        mockMvc.perform(post("/api/posts/{id}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.text").value("Hello"))
                .andExpect(jsonPath("$.postId").value(1));

        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[2].text").value("Hello"))
                .andExpect(jsonPath("$[2].postId").value(1));
    }

    @Test
    void createComment_postNotFound_returns404() throws Exception {
        String json = """
                {"text":"Hello","postId":999}
                """;

        mockMvc.perform(post("/api/posts/{id}/comments", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_postIdMismatch_returns400() throws Exception {
        String json = """
                {"text":"Hello","postId":2}
                """;

        mockMvc.perform(post("/api/posts/{id}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Configuration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.stream()
                    .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                    .map(c -> (MappingJackson2HttpMessageConverter) c)
                    .forEach(c -> c.setPrettyPrint(true));
        }
    }
}

package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Post;
import ru.practicum.dto.CreatePostDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostMapperTest {

    private final PostMapper mapper = new PostMapper();

    @Test
    void fromCreateDto_setsFieldsFromDto() {
        CreatePostDto dto = new CreatePostDto("My Title", "My text", List.of("java", "spring"));

        Post post = mapper.fromCreateDto(dto);

        assertNull(post.getId());
        assertEquals("My Title", post.getTitle());
        assertEquals("My text", post.getText());
        assertEquals(List.of("java", "spring"), post.getTags());
        assertEquals(0, post.getLikesCount());
        assertEquals(0, post.getCommentsCount());
    }
}
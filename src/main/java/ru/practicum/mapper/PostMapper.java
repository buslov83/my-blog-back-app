package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.model.Post;
import ru.practicum.dto.CreatePostDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.UpdatePostDto;

@Component
public class PostMapper {

    private static final int MAX_PREVIEW_LENGTH = 128;

    public PostDto toListDto(Post post) {
        String text = post.getText();
        if (text.length() > MAX_PREVIEW_LENGTH) {
            text = text.substring(0, MAX_PREVIEW_LENGTH) + "...";
        }
        return new PostDto(post.getId(), post.getTitle(), text, post.getTags(),
                post.getLikesCount(), post.getCommentsCount());
    }

    public PostDto toFullDto(Post post) {
        return new PostDto(post.getId(), post.getTitle(), post.getText(), post.getTags(),
                post.getLikesCount(), post.getCommentsCount());
    }

    public Post fromCreateDto(CreatePostDto dto) {
        return new Post(null, dto.title(), dto.text(), 0, dto.tags(), 0);
    }

    public Post fromUpdateDto(UpdatePostDto dto) {
        return new Post(dto.id(), dto.title(), dto.text(), null, dto.tags(), null);
    }
}

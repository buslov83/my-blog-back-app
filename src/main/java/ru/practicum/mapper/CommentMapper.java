package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.model.Comment;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CreateCommentDto;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getPostId());
    }

    public Comment fromCreateDto(CreateCommentDto dto) {
        return new Comment(null, dto.text(), dto.postId());
    }
}

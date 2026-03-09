package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.domain.Comment;
import ru.practicum.dto.CommentDto;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getPostId());
    }
}

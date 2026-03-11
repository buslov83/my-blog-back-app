package ru.practicum.service;

import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CreateCommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Optional<List<CommentDto>> getCommentsByPostId(long postId);

    Optional<CommentDto> getCommentByPostIdAndCommentId(long postId, long commentId);

    Optional<CommentDto> createComment(CreateCommentDto dto);
}

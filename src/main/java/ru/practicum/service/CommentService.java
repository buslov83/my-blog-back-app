package ru.practicum.service;

import ru.practicum.dto.CommentDto;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Optional<List<CommentDto>> getCommentsByPostId(Long postId);

    Optional<CommentDto> getCommentByPostIdAndCommentId(Long postId, Long commentId);
}

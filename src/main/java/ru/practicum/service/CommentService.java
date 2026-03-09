package ru.practicum.service;

import ru.practicum.dto.CommentDto;

import java.util.List;

public interface CommentService {

    List<CommentDto> getCommentsByPostId(Long postId);
}

package ru.practicum.repository;

import ru.practicum.domain.Comment;

import java.util.List;

public interface CommentRepository {

    List<Comment> findAllByPostId(Long postId);
}

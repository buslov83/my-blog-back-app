package ru.practicum.repository;

import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findAllByPostId(long postId);

    Optional<Comment> findByIdAndPostId(long commentId, long postId);

    void create(Comment comment);
}

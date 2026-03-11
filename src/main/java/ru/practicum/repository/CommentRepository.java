package ru.practicum.repository;

import ru.practicum.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findAllByPostId(Long postId);

    Optional<Comment> findByIdAndPostId(Long commentId, Long postId);
}

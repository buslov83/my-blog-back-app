package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.model.Comment;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CreateCommentDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostRepository postRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              CommentMapper commentMapper,
                              PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.postRepository = postRepository;
    }

    @Override
    public Optional<List<CommentDto>> getCommentsByPostId(long postId) {
        if (!postRepository.existsById(postId)) {
            return Optional.empty();
        }
        return Optional.of(commentRepository.findAllByPostId(postId).stream()
                .map(commentMapper::toDto)
                .toList());
    }

    @Override
    public Optional<CommentDto> getCommentByPostIdAndCommentId(long postId, long commentId) {
        return commentRepository.findByIdAndPostId(commentId, postId).map(commentMapper::toDto);
    }

    @Override
    public Optional<CommentDto> createComment(CreateCommentDto dto) {
        if (!postRepository.existsById(dto.postId())) {
            return Optional.empty();
        }
        Comment comment = commentMapper.fromCreateDto(dto);
        commentRepository.create(comment);
        return Optional.of(commentMapper.toDto(comment));
    }

    @Override
    public Optional<CommentDto> updateComment(CommentDto dto) {
        // validate both comment existence and post ownership
        if (commentRepository.findByIdAndPostId(dto.id(), dto.postId()).isEmpty()) {
            return Optional.empty();
        }
        commentRepository.update(commentMapper.fromDto(dto));
        return Optional.of(dto);
    }
}

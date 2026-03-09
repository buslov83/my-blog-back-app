package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.dto.CommentDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(commentMapper::toDto)
                .toList();
    }
}

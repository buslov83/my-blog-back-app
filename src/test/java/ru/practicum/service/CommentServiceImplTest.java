package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.domain.Comment;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CreateCommentDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.PostRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment comment1;
    private Comment comment2;
    private CommentDto commentDto1;
    private CommentDto commentDto2;

    @BeforeEach
    void setUp() {
        comment1 = new Comment(1L, "First comment", 10L);
        comment2 = new Comment(2L, "Second comment", 10L);
        commentDto1 = new CommentDto(1L, "First comment", 10L);
        commentDto2 = new CommentDto(2L, "Second comment", 10L);
    }

    @Test
    void getCommentsByPostId_postExists_returnsMappedComments() {
        when(postRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findAllByPostId(10L)).thenReturn(List.of(comment1, comment2));
        when(commentMapper.toDto(comment1)).thenReturn(commentDto1);
        when(commentMapper.toDto(comment2)).thenReturn(commentDto2);

        Optional<List<CommentDto>> result = commentService.getCommentsByPostId(10L);

        assertTrue(result.isPresent());
        assertEquals(List.of(commentDto1, commentDto2), result.get());
    }

    @Test
    void getCommentsByPostId_postDoesNotExist_returnsEmpty() {
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<List<CommentDto>> result = commentService.getCommentsByPostId(999L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }

    @Test
    void getCommentsByPostId_postExistsWithNoComments_returnsEmptyList() {
        when(postRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findAllByPostId(10L)).thenReturn(Collections.emptyList());

        Optional<List<CommentDto>> result = commentService.getCommentsByPostId(10L);

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    void createComment_postExists_returnsDto() {
        CreateCommentDto dto = new CreateCommentDto("New comment", 10L);
        when(postRepository.existsById(10L)).thenReturn(true);
        Comment newComment = new Comment(null, "New comment", 10L);
        when(commentMapper.fromCreateDto(dto)).thenReturn(newComment);
        CommentDto expectedDto = new CommentDto(99L, "New comment", 10L);
        when(commentMapper.toDto(newComment)).thenReturn(expectedDto);

        Optional<CommentDto> result = commentService.createComment(dto);

        assertTrue(result.isPresent());
        assertEquals(expectedDto, result.get());
        verify(commentRepository).create(newComment);
    }

    @Test
    void createComment_postDoesNotExist_returnsEmpty() {
        CreateCommentDto dto = new CreateCommentDto("New comment", 999L);
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<CommentDto> result = commentService.createComment(dto);

        assertTrue(result.isEmpty());
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }
}

package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.model.Comment;
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
    private PostRepository postRepository;

    private final CommentMapper commentMapper = new CommentMapper();

    private CommentServiceImpl commentService;

    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, commentMapper, postRepository);

        comment1 = new Comment(1L, "First comment", 10L);
        comment2 = new Comment(2L, "Second comment", 10L);
    }

    @Test
    void getCommentsByPostId_postExists_returnsMappedComments() {
        when(postRepository.existsById(10L)).thenReturn(true);
        when(commentRepository.findAllByPostId(10L)).thenReturn(List.of(comment1, comment2));

        Optional<List<CommentDto>> result = commentService.getCommentsByPostId(10L);

        assertTrue(result.isPresent());
        assertEquals(List.of(
                        new CommentDto(1L, "First comment", 10L),
                        new CommentDto(2L, "Second comment", 10L)),
                result.get());
    }

    @Test
    void getCommentsByPostId_postDoesNotExist_returnsEmpty() {
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<List<CommentDto>> result = commentService.getCommentsByPostId(999L);

        assertTrue(result.isEmpty());
        verifyNoInteractions(commentRepository);
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
    void getCommentByPostIdAndCommentId_commentExists_returnsDto() {
        when(commentRepository.findByIdAndPostId(1L, 10L)).thenReturn(Optional.of(comment1));

        Optional<CommentDto> result = commentService.getCommentByPostIdAndCommentId(10L, 1L);

        assertTrue(result.isPresent());
        assertEquals(new CommentDto(1L, "First comment", 10L), result.get());
    }

    @Test
    void getCommentByPostIdAndCommentId_commentNotFound_returnsEmpty() {
        when(commentRepository.findByIdAndPostId(999L, 10L)).thenReturn(Optional.empty());

        Optional<CommentDto> result = commentService.getCommentByPostIdAndCommentId(10L, 999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createComment_postExists_returnsDto() {
        CreateCommentDto dto = new CreateCommentDto("New comment", 10L);
        when(postRepository.existsById(10L)).thenReturn(true);
        doAnswer(invocation -> {
            invocation.getArgument(0, Comment.class).setId(99L);
            return null;
        }).when(commentRepository).create(any(Comment.class));

        Optional<CommentDto> result = commentService.createComment(dto);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).create(captor.capture());
        assertEquals("New comment", captor.getValue().getText());
        assertEquals(10L, captor.getValue().getPostId());

        assertTrue(result.isPresent());
        assertEquals(new CommentDto(99L, "New comment", 10L), result.get());
    }

    @Test
    void createComment_postDoesNotExist_returnsEmpty() {
        CreateCommentDto dto = new CreateCommentDto("New comment", 999L);
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<CommentDto> result = commentService.createComment(dto);

        assertTrue(result.isEmpty());
        verifyNoInteractions(commentRepository);
    }

    @Test
    void updateComment_commentExists_returnsUpdatedDto() {
        CommentDto dto = new CommentDto(1L, "Updated text", 10L);
        when(commentRepository.findByIdAndPostId(1L, 10L)).thenReturn(Optional.of(comment1));

        Optional<CommentDto> result = commentService.updateComment(dto);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).update(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("Updated text", captor.getValue().getText());
        assertEquals(10L, captor.getValue().getPostId());

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    void updateComment_commentNotFound_returnsEmpty() {
        CommentDto dto = new CommentDto(999L, "Updated text", 10L);
        when(commentRepository.findByIdAndPostId(999L, 10L)).thenReturn(Optional.empty());

        Optional<CommentDto> result = commentService.updateComment(dto);

        assertTrue(result.isEmpty());
        verify(commentRepository, never()).update(any());
    }

    @Test
    void deleteComment_commentExists_returnsTrue() {
        when(commentRepository.deleteByIdAndPostId(1L, 10L)).thenReturn(true);

        boolean result = commentService.deleteComment(10L, 1L);

        assertTrue(result);
    }

    @Test
    void deleteComment_commentNotFound_returnsFalse() {
        when(commentRepository.deleteByIdAndPostId(999L, 10L)).thenReturn(false);

        boolean result = commentService.deleteComment(10L, 999L);

        assertFalse(result);
    }
}

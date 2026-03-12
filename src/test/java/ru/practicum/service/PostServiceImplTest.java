package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.model.Post;
import ru.practicum.model.PostImage;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.dto.UpdatePostDto;
import ru.practicum.mapper.PostMapper;
import ru.practicum.repository.PostRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private Post post1;
    private Post post2;
    private PostDto postDto1;
    private PostDto postDto2;

    @BeforeEach
    void setUp() {
        post1 = new Post(1L, "Spring Boot Guide", "Content of post 1", 5, List.of("spring", "java"), 3);
        post2 = new Post(2L, "Java Tips", "Content of post 2", 2, List.of("java"), 1);
        postDto1 = new PostDto(1L, "Spring Boot Guide", "Content of post 1", List.of("spring", "java"), 5, 3);
        postDto2 = new PostDto(2L, "Java Tips", "Content of post 2", List.of("java"), 2, 1);
    }

    @Test
    void getPosts_nullSearch_passesEmptyFiltersToRepository() {
        when(postRepository.findAll("", Collections.emptyList(), 0, 10))
                .thenReturn(List.of(post1, post2));
        when(postRepository.count("", Collections.emptyList())).thenReturn(2);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);
        when(postMapper.toListDto(post2)).thenReturn(postDto2);

        PostsPageDto result = postService.getPosts(null, 1, 10);

        assertEquals(List.of(postDto1, postDto2), result.posts());
    }

    @Test
    void getPosts_emptySearch_passesEmptyFiltersToRepository() {
        when(postRepository.findAll("", Collections.emptyList(), 0, 10))
                .thenReturn(List.of(post1));
        when(postRepository.count("", Collections.emptyList())).thenReturn(1);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts("", 1, 10);

        assertEquals(List.of(postDto1), result.posts());
    }

    @Test
    void getPosts_titleOnlySearch_passesTitleWordsAndEmptyTags() {
        when(postRepository.findAll("Spring Boot", Collections.emptyList(), 0, 10))
                .thenReturn(List.of(post1));
        when(postRepository.count("Spring Boot", Collections.emptyList())).thenReturn(1);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts(" Spring   Boot ", 1, 10);

        assertEquals(List.of(postDto1), result.posts());
    }

    @Test
    void getPosts_singleWordSearch_passesTitleAndEmptyTags() {
        when(postRepository.findAll("Java", Collections.emptyList(), 0, 10))
                .thenReturn(List.of(post2));
        when(postRepository.count("Java", Collections.emptyList())).thenReturn(1);
        when(postMapper.toListDto(post2)).thenReturn(postDto2);

        PostsPageDto result = postService.getPosts("Java", 1, 10);

        assertEquals(List.of(postDto2), result.posts());
    }

    @Test
    void getPosts_tagOnlySearch_passesEmptyTitleAndParsedTags() {
        when(postRepository.findAll("", List.of("spring", "java"), 0, 10))
                .thenReturn(List.of(post1));
        when(postRepository.count("", List.of("spring", "java"))).thenReturn(1);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts(" #spring #  #java ", 1, 10);

        assertEquals(List.of(postDto1), result.posts());
    }

    @Test
    void getPosts_singleTagSearch_passesCorrectTag() {
        when(postRepository.findAll("", List.of("java"), 0, 10))
                .thenReturn(List.of(post1, post2));
        when(postRepository.count("", List.of("java"))).thenReturn(2);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);
        when(postMapper.toListDto(post2)).thenReturn(postDto2);

        PostsPageDto result = postService.getPosts("#java", 1, 10);

        assertEquals(List.of(postDto1, postDto2), result.posts());
    }

    @Test
    void getPosts_mixedSearch_splitsTitleWordsAndTagsCorrectly() {
        when(postRepository.findAll("Spring Boot", List.of("java", "spring"), 0, 10))
                .thenReturn(List.of(post1));
        when(postRepository.count("Spring Boot", List.of("java", "spring"))).thenReturn(1);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts(" Spring   #java  Boot #spring ", 1, 10);

        assertEquals(List.of(postDto1), result.posts());
    }

    @Test
    void getPosts_noResultsFromRepository_returnsEmptyPostsList() {
        when(postRepository.findAll("nonexistent", Collections.emptyList(), 0, 10))
                .thenReturn(Collections.emptyList());
        when(postRepository.count("nonexistent", Collections.emptyList())).thenReturn(0);

        PostsPageDto result = postService.getPosts("nonexistent", 1, 10);

        assertTrue(result.posts().isEmpty());
        assertFalse(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(1, result.lastPage());
    }

    @Test
    void getPosts_firstPage_hasNoPrevAndHasNext() {
        when(postRepository.findAll("", Collections.emptyList(), 0, 2))
                .thenReturn(List.of(post1, post2));
        when(postRepository.count("", Collections.emptyList())).thenReturn(5);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);
        when(postMapper.toListDto(post2)).thenReturn(postDto2);

        PostsPageDto result = postService.getPosts(null, 1, 2);

        assertEquals(List.of(postDto1, postDto2), result.posts());
        assertFalse(result.hasPrev());
        assertTrue(result.hasNext());
        assertEquals(3, result.lastPage());
    }

    @Test
    void getPosts_lastPage_hasPrevAndNoNext() {
        when(postRepository.findAll("", Collections.emptyList(), 4, 2))
                .thenReturn(List.of(post1));
        when(postRepository.count("", Collections.emptyList())).thenReturn(5);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts(null, 3, 2);

        assertEquals(List.of(postDto1), result.posts());
        assertTrue(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(3, result.lastPage());
    }

    @Test
    void getPosts_middlePage_hasPrevAndHasNext() {
        when(postRepository.findAll("", Collections.emptyList(), 2, 2))
                .thenReturn(List.of(post1, post2));
        when(postRepository.count("", Collections.emptyList())).thenReturn(6);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);
        when(postMapper.toListDto(post2)).thenReturn(postDto2);

        PostsPageDto result = postService.getPosts(null, 2, 2);

        assertEquals(List.of(postDto1, postDto2), result.posts());
        assertTrue(result.hasPrev());
        assertTrue(result.hasNext());
        assertEquals(3, result.lastPage());
    }

    @Test
    void getPosts_singlePage_hasNoPrevAndNoNext() {
        when(postRepository.findAll("", Collections.emptyList(), 0, 10))
                .thenReturn(List.of(post1));
        when(postRepository.count("", Collections.emptyList())).thenReturn(1);
        when(postMapper.toListDto(post1)).thenReturn(postDto1);

        PostsPageDto result = postService.getPosts(null, 1, 10);

        assertEquals(List.of(postDto1), result.posts());
        assertFalse(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(1, result.lastPage());
    }

    @Test
    void getPosts_offsetCalculation_isCorrect() {
        when(postRepository.findAll("", Collections.emptyList(), 14, 7))
                .thenReturn(Collections.emptyList());
        when(postRepository.count("", Collections.emptyList())).thenReturn(0);

        PostsPageDto result = postService.getPosts(null, 3, 7);

        assertTrue(result.posts().isEmpty());
    }

    @Test
    void getPosts_lastPageCalculation_roundsUp() {
        // 11 posts with page size 5 => 3 pages
        when(postRepository.findAll("", Collections.emptyList(), 15, 5))
                .thenReturn(Collections.emptyList());
        when(postRepository.count("", Collections.emptyList())).thenReturn(11);

        PostsPageDto result = postService.getPosts(null, 4, 5);

        assertEquals(3, result.lastPage());
    }

    @Test
    void getPosts_lastPageCalculation_exactMultipleOfPageSize() {
        // 10 posts with page size 5 => 2 pages
        when(postRepository.findAll("", Collections.emptyList(), 15, 5))
                .thenReturn(List.of(post1, post2));
        when(postRepository.count("", Collections.emptyList())).thenReturn(10);

        PostsPageDto result = postService.getPosts(null, 4, 5);

        assertEquals(2, result.lastPage());
    }

    @Test
    void updatePost_existingPost_returnsDto() {
        UpdatePostDto dto = new UpdatePostDto(1L, "Updated Title", "Updated text", List.of("foo"));
        Post mappedPost = new Post(1L, "Updated Title", "Updated text", 0, List.of("foo"), 0);
        when(postRepository.existsById(1L)).thenReturn(true);
        when(postMapper.fromUpdateDto(dto)).thenReturn(mappedPost);
        Post updated = new Post(1L, "Updated Title", "Updated text", 2, List.of("foo"), 3);
        when(postRepository.findById(1L)).thenReturn(Optional.of(updated));
        PostDto updatedPostDto = new PostDto(1L, "Updated Title", "Updated text", List.of("foo"), 2, 3);
        when(postMapper.toFullDto(updated)).thenReturn(updatedPostDto);

        Optional<PostDto> result = postService.updatePost(dto);

        assertTrue(result.isPresent());
        assertEquals(updatedPostDto, result.get());
        verify(postRepository).update(mappedPost);
    }

    @Test
    void updatePost_nonExistingPost_returnsEmpty() {
        UpdatePostDto dto = new UpdatePostDto(999L, "Title", "Text", List.of());
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<PostDto> result = postService.updatePost(dto);

        assertTrue(result.isEmpty());
        verify(postRepository, never()).update(any());
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    void incrementLikes_existingPost_returnsNewCount() {
        when(postRepository.existsById(1L)).thenReturn(true);
        Post updatedPost = new Post(1L, "title", "text", 6, List.of(), 0);
        when(postRepository.findById(1L)).thenReturn(Optional.of(updatedPost));

        Optional<Integer> result = postService.incrementLikes(1L);

        assertTrue(result.isPresent());
        assertEquals(6, result.get());
        verify(postRepository).incrementLikes(1L);
    }

    @Test
    void incrementLikes_nonExistingPost_returnsEmpty() {
        when(postRepository.existsById(999L)).thenReturn(false);

        Optional<Integer> result = postService.incrementLikes(999L);

        assertTrue(result.isEmpty());
        verify(postRepository, never()).incrementLikes(anyLong());
    }

    @Test
    void getPostImage_imageExists() {
        byte[] data = {1, 2, 3};
        when(postRepository.findImageById(1L)).thenReturn(Optional.of(new PostImage(data, "image/jpeg")));

        Optional<PostImage> result = postService.getPostImage(1L);

        assertTrue(result.isPresent());
        assertArrayEquals(data, result.get().data());
        assertEquals("image/jpeg", result.get().contentType());
    }

    @Test
    void getPostImage_postNotFound_returnsEmpty() {
        when(postRepository.findImageById(99L)).thenReturn(Optional.empty());

        Optional<PostImage> result = postService.getPostImage(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostImage_imageDataIsNull_returnsEmpty() {
        when(postRepository.findImageById(1L)).thenReturn(Optional.of(new PostImage(null, null)));

        Optional<PostImage> result = postService.getPostImage(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostImage_imageDataIsEmpty_returnsEmpty() {
        when(postRepository.findImageById(1L)).thenReturn(Optional.of(new PostImage(new byte[0], "image/png")));

        Optional<PostImage> result = postService.getPostImage(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updatePostImage_existingPost_callsRepositoryAndReturnsTrue() {
        byte[] data = {1, 2, 3};
        when(postRepository.existsById(1L)).thenReturn(true);

        boolean result = postService.updatePostImage(1L, data, "image/jpeg");

        assertTrue(result);
        verify(postRepository).updateImage(1L, data, "image/jpeg");
    }

    @Test
    void updatePostImage_nonExistingPost_returnsFalseAndSkipsUpdate() {
        when(postRepository.existsById(999L)).thenReturn(false);

        boolean result = postService.updatePostImage(999L, new byte[]{1, 2, 3}, "image/png");

        assertFalse(result);
        verify(postRepository, never()).updateImage(anyLong(), any(), anyString());
    }
}

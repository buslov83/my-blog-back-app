package ru.practicum.service;

import ru.practicum.domain.PostImage;
import ru.practicum.dto.CreatePostDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.dto.UpdatePostDto;

import java.util.Optional;

public interface PostService {

    PostsPageDto getPosts(String search, int pageNumber, int pageSize);

    Optional<PostDto> getPost(long id);

    PostDto createPost(CreatePostDto dto);

    Optional<PostDto> updatePost(UpdatePostDto dto);

    Optional<Integer> incrementLikes(long id);

    boolean deletePost(long id);

    Optional<PostImage> getPostImage(long id);

    boolean updatePostImage(long id, byte[] data, String contentType);
}

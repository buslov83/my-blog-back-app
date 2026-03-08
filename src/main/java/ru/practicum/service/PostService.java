package ru.practicum.service;

import ru.practicum.domain.PostImage;
import ru.practicum.dto.CreatePostDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostsPageDto;

import java.util.Optional;

public interface PostService {

    PostsPageDto getPosts(String search, int pageNumber, int pageSize);

    Optional<PostDto> getPost(long id);

    Optional<PostImage> getPostImage(long id);

    PostDto createPost(CreatePostDto dto);
}

package ru.practicum.service;

import ru.practicum.domain.PostImage;
import ru.practicum.dto.PostsPageDto;

import java.util.Optional;

public interface PostService {

    PostsPageDto getPosts(String search, int pageNumber, int pageSize);

    Optional<PostImage> getPostImage(long id);
}

package ru.practicum.service;

import ru.practicum.dto.PostsPageDto;

public interface PostService {

    PostsPageDto getPosts(String search, int pageNumber, int pageSize);
}

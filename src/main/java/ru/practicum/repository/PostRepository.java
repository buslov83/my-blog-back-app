package ru.practicum.repository;

import ru.practicum.domain.Post;

import java.util.List;

public interface PostRepository {

    List<Post> findAll(String titleSearch, List<String> tags, int offset, int limit);

    int count(String titleSearch, List<String> tags);
}

package ru.practicum.repository;

import ru.practicum.domain.Post;
import ru.practicum.domain.PostImage;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    List<Post> findAll(String titleSearch, List<String> tags, int offset, int limit);

    int count(String titleSearch, List<String> tags);

    Optional<Post> findById(long id);

    Optional<PostImage> findImageById(long id);

    void create(Post post);

    boolean updateImage(long id, byte[] data, String contentType);
}

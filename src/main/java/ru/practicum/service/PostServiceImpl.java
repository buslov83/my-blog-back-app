package ru.practicum.service;

import org.springframework.stereotype.Service;
import ru.practicum.domain.Post;
import ru.practicum.domain.PostImage;
import ru.practicum.dto.CreatePostDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.mapper.PostMapper;
import ru.practicum.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PostServiceImpl(PostRepository postRepository, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    @Override
    public PostsPageDto getPosts(String search, int pageNumber, int pageSize) {
        List<String> tags = new ArrayList<>();
        List<String> titleWords = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            for (String word : search.trim().split("\\s+")) {
                if (word.startsWith("#")) {
                    String tag = word.substring(1);
                    if (!tag.isEmpty()) {
                        tags.add(tag);
                    }
                } else {
                    titleWords.add(word);
                }
            }
        }

        String titleSearch = String.join(" ", titleWords);
        int offset = (pageNumber - 1) * pageSize;

        List<Post> posts = postRepository.findAll(titleSearch, tags, offset, pageSize);
        int total = postRepository.count(titleSearch, tags);

        int lastPage = Math.max(1, (int) Math.ceil((double) total / pageSize));
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        List<PostDto> postDtos = posts.stream()
                .map(postMapper::toListDto)
                .toList();

        return new PostsPageDto(postDtos, hasPrev, hasNext, lastPage);
    }

    @Override
    public Optional<PostDto> getPost(long id) {
        return postRepository.findById(id).map(postMapper::toFullDto);
    }

    @Override
    public PostDto createPost(CreatePostDto dto) {
        Post post = postMapper.fromCreateDto(dto);
        postRepository.create(post);
        return postMapper.toFullDto(post);
    }

    @Override
    public Optional<PostImage> getPostImage(long id) {
        return postRepository.findImageById(id).filter(img -> img.data() != null && img.data().length > 0);
    }

    @Override
    public boolean updatePostImage(long id, byte[] data, String contentType) {
        return postRepository.updateImage(id, data, contentType);
    }
}

package ru.practicum.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CreatePostDto;
import ru.practicum.dto.PostDto;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto createPost(@RequestBody CreatePostDto dto) {
        return postService.createPost(dto);
    }

    @GetMapping
    public PostsPageDto getPosts(@RequestParam("search") String search,
                                 @RequestParam("pageNumber") int pageNumber,
                                 @RequestParam("pageSize") int pageSize) {
        return postService.getPosts(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable("id") long id) {
        return postService.getPost(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getPostImage(@PathVariable("id") long id) {
        return postService.getPostImage(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.contentType()))
                        .body(img.data()))
                .orElse(ResponseEntity.notFound().build());
    }
}

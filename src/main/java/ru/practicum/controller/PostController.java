package ru.practicum.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.PostsPageDto;
import ru.practicum.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public PostsPageDto getPosts(@RequestParam("search") String search,
                                 @RequestParam("pageNumber") int pageNumber,
                                 @RequestParam("pageSize") int pageSize) {
        return postService.getPosts(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getPostImage(@PathVariable long id) {
        return postService.getPostImage(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .body(img.getData()))
                .orElse(ResponseEntity.notFound().build());
    }
}

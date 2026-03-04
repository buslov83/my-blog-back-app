package ru.practicum.controller;

import org.springframework.web.bind.annotation.GetMapping;
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
}

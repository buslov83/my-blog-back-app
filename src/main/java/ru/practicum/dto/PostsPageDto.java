package ru.practicum.dto;

import java.util.List;

public record PostsPageDto(
        List<PostDto> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage) {
}

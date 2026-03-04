package ru.practicum.dto;

import java.util.List;

public record PostDto(
        Long id,
        String title,
        String text,
        List<String> tags,
        int likesCount,
        int commentsCount) {
}

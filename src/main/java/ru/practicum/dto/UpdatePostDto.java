package ru.practicum.dto;

import java.util.List;

public record UpdatePostDto(Long id, String title, String text, List<String> tags) {}

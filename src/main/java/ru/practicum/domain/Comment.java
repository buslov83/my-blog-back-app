package ru.practicum.domain;

public class Comment {

    private final Long id;
    private final String text;
    private final Long postId;

    public Comment(Long id, String text, Long postId) {
        this.id = id;
        this.text = text;
        this.postId = postId;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Long getPostId() {
        return postId;
    }
}

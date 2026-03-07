package ru.practicum.domain;

import java.util.List;

public class Post {

    private final Long id;
    private final String title;
    private final String text;
    private final PostImage postImage;
    private final int likesCount;
    private final List<String> tags;
    private final int commentsCount;

    public Post(Long id,
                String title,
                String text,
                PostImage postImage,
                int likesCount,
                List<String> tags,
                int commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.postImage = postImage;
        this.likesCount = likesCount;
        this.tags = tags;
        this.commentsCount = commentsCount;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public PostImage getPostImage() {
        return postImage;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
}

package ru.practicum.domain;

import java.util.List;

public class Post {

    private Long id;
    private String title;
    private String text;
    private PostImage postImage;
    private int likesCount;
    private List<String> tags;
    private int commentsCount;

    public Post(Long id,
                String title,
                String text,
                int likesCount,
                List<String> tags,
                int commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.likesCount = likesCount;
        this.tags = tags;
        this.commentsCount = commentsCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PostImage getPostImage() {
        return postImage;
    }

    public void setPostImage(PostImage postImage) {
        this.postImage = postImage;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }
}

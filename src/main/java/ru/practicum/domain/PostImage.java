package ru.practicum.domain;

public class PostImage {

    private final byte[] data;
    private final String contentType;

    public PostImage(byte[] data, String contentType) {
        this.data = data;
        this.contentType = contentType;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }
}

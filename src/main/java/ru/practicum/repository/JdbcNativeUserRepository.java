package ru.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.practicum.domain.Post;
import ru.practicum.domain.PostImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativeUserRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rowNum) -> new Post(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("text"),
            null,
            null,
            rs.getInt("likes_count"),
            parseTags(rs.getString("tags")),
            rs.getInt("comments_count")
    );

    @Override
    public List<Post> findAll(String titleSearch, List<String> tags, int offset, int limit) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.title, p.text, p.likes_count, p.tags," +
                "       COUNT(c.id) AS comments_count" +
                " FROM posts p" +
                " LEFT JOIN comments c ON c.post_id = p.id"
        );
        buildWhere(sql, titleSearch, tags, params);
        sql.append(" GROUP BY p.id, p.title, p.text, p.likes_count, p.tags");
        sql.append(" ORDER BY p.id DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), POST_ROW_MAPPER, params.toArray());
    }

    @Override
    public int count(String titleSearch, List<String> tags) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts p");
        buildWhere(sql, titleSearch, tags, params);
        Integer result = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return result != null ? result : 0;
    }

    private void buildWhere(StringBuilder sql, String titleSearch, List<String> tags, List<Object> params) {
        sql.append(" WHERE 1=1");
        if (titleSearch != null && !titleSearch.isBlank()) {
            sql.append(" AND LOWER(p.title) LIKE LOWER(?)");
            params.add("%" + titleSearch + "%");
        }
        for (String tag : tags) {
            sql.append(" AND p.tags LIKE ?");
            params.add("% " + tag + " %");
        }
    }

    @Override
    public Optional<PostImage> findImageById(long id) {
        String sql = "SELECT image, image_content_type FROM posts WHERE id = ?";
        PostImage result = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new PostImage(rs.getBytes("image"), rs.getString("image_content_type")),
                id);
        return Optional.ofNullable(result);
    }

    private static List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(tagsStr.trim().split("\\s+"));
    }
}

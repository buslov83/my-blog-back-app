package ru.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.domain.Post;
import ru.practicum.domain.PostImage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rowNum) -> new Post(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("text"),
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
    public Optional<Post> findById(long id) {
        Post result = jdbcTemplate.query(
                "SELECT p.id, p.title, p.text, p.likes_count, p.tags," +
                "       COUNT(c.id) AS comments_count" +
                " FROM posts p" +
                " LEFT JOIN comments c ON c.post_id = p.id" +
                " WHERE p.id = ?" +
                " GROUP BY p.id, p.title, p.text, p.likes_count, p.tags",
                resultSet -> resultSet.next() ? POST_ROW_MAPPER.mapRow(resultSet, 0) : null,
                id);
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<PostImage> findImageById(long id) {
        PostImage result = jdbcTemplate.query("SELECT image, image_content_type FROM posts WHERE id = ?",
                resultSet -> resultSet.next()
                        ? new PostImage(resultSet.getBytes("image"), resultSet.getString("image_content_type"))
                        : null,
                id);
        return Optional.ofNullable(result);
    }

    @Override
    public void create(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String tagsStr = post.getTags() == null || post.getTags().isEmpty()
                ? null
                : " " + String.join(" ", post.getTags()) + " ";
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO posts (title, text, tags, likes_count) VALUES (?, ?, ?, 0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setString(3, tagsStr);
            return ps;
        }, keyHolder);
        post.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }

    private static List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagsStr.trim().split("\\s+")).toList();
    }
}

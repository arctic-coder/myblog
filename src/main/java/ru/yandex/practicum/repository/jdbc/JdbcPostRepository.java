package ru.yandex.practicum.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Тут все запросы в таблицу posts
 */
@Repository
@RequiredArgsConstructor
public class JdbcPostRepository implements PostRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<Post> findFeed(String tag, int offset, int limit) {
        boolean filter = tag != null && !tag.isBlank();

        String sql = """
        SELECT id, title, text, tags_csv, image_data, likes_count, created_at
        FROM posts
        %s
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?
        """.formatted(filter ? "WHERE (',' || LOWER(tags_csv) || ',') LIKE ('%,' || LOWER(?) || ',%')" : "");

        Object[] args = filter
                ? new Object[]{tag, limit, offset}
                : new Object[]{limit, offset};

        return jdbc.query(sql, (rs, rn) -> mapPost(rs, false), args);
    }

    @Override
    public int countFeed(String tag) {
        boolean filter = tag != null && !tag.isBlank();
        String sql = ("SELECT COUNT(*) FROM posts " +
                (filter ? "WHERE (',' || LOWER(tags_csv) || ',') LIKE ('%,' || LOWER(?) || ',%')" : ""));
        return filter
                ? Optional.ofNullable(jdbc.queryForObject(sql, Integer.class, tag)).orElse(0)
                : Optional.ofNullable(jdbc.queryForObject(sql, Integer.class)).orElse(0);
    }


    @Override
    public Optional<Post> findById(long id) {
        var sql = """
                SELECT id, title, text, tags_csv, image_data, likes_count, created_at
                FROM posts WHERE id = ?
                """;
        List<Post> list = jdbc.query(sql, (rs, rowNum) -> mapPost(rs, /*withImage*/ true), id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public long create(String title, List<String> tags, String text, byte[] imageBytes) {
        var sql = """
                INSERT INTO posts (title, text, tags_csv, image_data, likes_count, created_at)
                VALUES (?, ?, ?, ?, 0, ?)
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, title);
            ps.setString(2, text);
            ps.setString(3, toCsv(tags));
            if (imageBytes != null && imageBytes.length > 0) {
                ps.setBytes(4, imageBytes);
            } else {
                ps.setNull(4, java.sql.Types.BLOB);
            }
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? 0L : key.longValue();
    }

    @Override
    public void update(long id, String title, List<String> tags, String text, byte[] imageBytesOrNull) {
        if (imageBytesOrNull != null && imageBytesOrNull.length > 0) {
            var sql = """
                    UPDATE posts
                    SET title = ?, text = ?, tags_csv = ?, image_data = ?
                    WHERE id = ?
                    """;
            jdbc.update(sql, title, text, toCsv(tags), imageBytesOrNull, id);
        } else {
            var sql = """
                    UPDATE posts
                    SET title = ?, text = ?, tags_csv = ?
                    WHERE id = ?
                    """;
            jdbc.update(sql, title, text, toCsv(tags), id);
        }
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM posts WHERE id = ?", id);
    }

    @Override
    public void like(long id, boolean likeUp) {
        var sql = likeUp
                ? "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?"
                : "UPDATE posts SET likes_count = CASE WHEN likes_count > 0 THEN likes_count - 1 ELSE 0 END WHERE id = ?";
        jdbc.update(sql, id);
    }


    private static Post mapPost(ResultSet rs, boolean withImage) throws java.sql.SQLException {
        var p = new Post();
        p.setId(rs.getLong("id"));
        p.setTitle(rs.getString("title"));
        p.setText(rs.getString("text"));
        p.setTags(fromCsv(rs.getString("tags_csv")));
        if (withImage) {
            byte[] img = rs.getBytes("image_data");
            p.setImageData(img);
        }
        p.setLikesCount(rs.getInt("likes_count"));
        Timestamp ts = rs.getTimestamp("created_at");
        p.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
        return p;
    }

    private static List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String toCsv(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return tags.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private Map<Long, Integer> loadCommentCounts(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        // H2 поддерживает IN (...)
        String in = postIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT post_id, COUNT(*) AS cnt FROM comments WHERE post_id IN (" + in + ") GROUP BY post_id";
        Object[] args = postIds.toArray();
        Map<Long, Integer> map = new HashMap<>();
        jdbc.query(sql, rs -> {
            map.put(rs.getLong("post_id"), rs.getInt("cnt"));
        }, args);
        return map;
    }

    private static Object[] params(boolean filter, String tag, Object... tail) {
        List<Object> p = new ArrayList<>();
        if (filter) p.add(tag);
        p.addAll(Arrays.asList(tail));
        return p.toArray();
    }
}

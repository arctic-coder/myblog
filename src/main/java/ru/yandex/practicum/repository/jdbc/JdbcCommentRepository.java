package ru.yandex.practicum.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.repository.CommentRepository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Тут все запросы в таблицу comments
 */
@Repository
@RequiredArgsConstructor
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbc;

    @Override
    public List<Comment> findByPostId(long postId) {
        var sql = """
                SELECT id, post_id, text, created_at
                FROM comments
                WHERE post_id = ?
                ORDER BY created_at ASC, id ASC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            var c = new Comment();
            c.setId(rs.getLong("id"));
            c.setPostId(rs.getLong("post_id"));
            c.setText(rs.getString("text"));
            var ts = rs.getTimestamp("created_at");
            c.setCreatedAt(ts == null ? null : ts.toLocalDateTime());
            return c;
        }, postId);
    }

    @Override
    public long create(long postId, String text) {
        var sql = "INSERT INTO comments (post_id, text, created_at) VALUES (?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, postId);
            ps.setString(2, text);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, kh);
        Number key = kh.getKey();
        return key == null ? 0L : key.longValue();
    }

    @Override
    public void update(long postId, long commentId, String text) {
        jdbc.update("UPDATE comments SET text = ? WHERE id = ? AND post_id = ?", text, commentId, postId);
    }

    @Override
    public void delete(long postId, long commentId) {
        jdbc.update("DELETE FROM comments WHERE id = ? AND post_id = ?", commentId, postId);
    }
}

package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.repository.jdbc.JdbcCommentRepository;
import ru.yandex.practicum.repository.jdbc.JdbcPostRepository;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ JdbcPostRepository.class, JdbcCommentRepository.class })
@Sql(statements = {
        "DELETE FROM comments",
        "DELETE FROM posts"
})
class JdbcCommentRepositoryIT {

    @Autowired JdbcPostRepository posts;
    @Autowired JdbcCommentRepository comments;

    @Test
    void create_find_update_delete_comment() {
        long postId = posts.create("T", java.util.List.of("x"), "t", null);
        long cid = comments.create(postId, "hello");

        var list1 = comments.findByPostId(postId);
        assertThat(list1).hasSize(1);
        assertThat(list1.get(0).getText()).isEqualTo("hello");

        comments.update(postId, cid, "upd");
        var list2 = comments.findByPostId(postId);
        assertThat(list2.get(0).getText()).isEqualTo("upd");

        comments.delete(postId, cid);
        assertThat(comments.findByPostId(postId)).isEmpty();
    }
}

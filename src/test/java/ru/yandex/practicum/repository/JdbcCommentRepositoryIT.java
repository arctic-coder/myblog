package ru.yandex.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.repository.jdbc.JdbcCommentRepository;
import ru.yandex.practicum.repository.jdbc.JdbcPostRepository;
import ru.yandex.practicum.testconfig.TestDbConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TestDbConfig.class,
        JdbcPostRepository.class,
        JdbcCommentRepository.class
})
class JdbcCommentRepositoryIT {

    @Autowired JdbcPostRepository posts;
    @Autowired JdbcCommentRepository comments;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM comments");
        jdbc.update("DELETE FROM posts");
    }

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

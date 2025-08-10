package ru.yandex.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.jdbc.JdbcCommentRepository;
import ru.yandex.practicum.repository.jdbc.JdbcPostRepository;
import ru.yandex.practicum.testconfig.TestDbConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TestDbConfig.class,
        JdbcPostRepository.class,
        JdbcCommentRepository.class
})
class JdbcPostRepositoryIT {

    @Autowired JdbcPostRepository posts;
    @Autowired JdbcCommentRepository comments;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        // Чистим БД
        jdbc.update("DELETE FROM comments");
        jdbc.update("DELETE FROM posts");
    }

    @Test
    void create_and_findById_maps_all_fields_including_image() {
        long id = posts.create("T", List.of("java","spring"), "A\nB", new byte[]{1,2,3});
        Post p = posts.findById(id).orElseThrow();
        assertThat(p.getTitle()).isEqualTo("T");
        assertThat(p.getTags()).containsExactlyInAnyOrder("java","spring");
        //assertThat(p.getTextParts()).containsExactly("A","B");
        assertThat(p.getImageData()).containsExactly(1,2,3);
        assertThat(p.getLikesCount()).isZero();
    }

    @Test
    void update_without_image_keeps_previous_image_update_with_image_replaces() {
        long id = posts.create("A", List.of("x"), "t", new byte[]{9});
        assertThat(posts.findById(id).orElseThrow().getImageData()).containsExactly(9);

        posts.update(id, "A2", List.of("x","y"), "t2", null); // без новой картинки
        Post after1 = posts.findById(id).orElseThrow();
        assertThat(after1.getTitle()).isEqualTo("A2");
        assertThat(after1.getImageData()).containsExactly(9);

        posts.update(id, "A3", List.of("y"), "t3", new byte[]{7});
        Post after2 = posts.findById(id).orElseThrow();
        assertThat(after2.getImageData()).containsExactly(7);
    }

    @Test
    void like_increments_and_decrements() {
        long id = posts.create("L", List.of(), "t", null);
        posts.like(id, true);
        assertThat(posts.findById(id).orElseThrow().getLikesCount()).isEqualTo(1);
        posts.like(id, false);
        assertThat(posts.findById(id).orElseThrow().getLikesCount()).isEqualTo(0);
    }

    @Test
    void delete_cascades_comments() {
        long id = posts.create("D", List.of(), "t", null);
        long c1 = comments.create(id, "c1");
        long c2 = comments.create(id, "c2");
        assertThat(comments.findByPostId(id)).hasSize(2);

        posts.delete(id);
        assertThat(comments.findByPostId(id)).isEmpty();
    }

    @Test
    void feed_returns_ordered_paginated_and_filters_by_csv_tag_exactly() {
        long a = posts.create("A", List.of("java","spring"), "a", null);
        sleepTiny(); // чтобы created_at отличался
        long b = posts.create("B", List.of("javascript"), "b", null);
        sleepTiny();
        long c = posts.create("C", List.of("java"), "c", null);

        // без фильтра: порядок по created_at DESC -> C, B, A
        var pageAll = posts.findFeed("", 0, 10);
        assertThat(pageAll).extracting(Post::getTitle).containsExactly("C","B","A");

        // пагинация: limit=2 offset=0 => C, B
        var page1 = posts.findFeed("", 0, 2);
        assertThat(page1).extracting(Post::getTitle).containsExactly("C","B");

        // пагинация: limit=2 offset=2 => A
        var page2 = posts.findFeed("", 2, 2);
        assertThat(page2).extracting(Post::getTitle).containsExactly("A");

        // фильтр по тегу: "java" не должен матчить "javascript"
        var onlyJava = posts.findFeed("java", 0, 10);
        assertThat(onlyJava).extracting(Post::getTitle).containsExactlyInAnyOrder("A","C");

        assertThat(posts.countFeed("")).isEqualTo(3);
        assertThat(posts.countFeed("java")).isEqualTo(2);
        assertThat(posts.countFeed("spring")).isEqualTo(1);
        assertThat(posts.countFeed("javascript")).isEqualTo(1);
    }

    private static void sleepTiny() {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
    }
}

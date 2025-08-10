package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Comment;

import java.util.List;

public interface CommentRepository {
    List<Comment> findByPostId(long postId);

    long create(long postId, String text);

    void update(long postId, long commentId, String text);

    void delete(long postId, long commentId);
}

package ru.yandex.practicum.repository;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<Post> findFeed(String tag, int offset, int limit);
    int countFeed(String tag);

    Optional<Post> findById(long id);

    long create(String title, List<String> tags, String text, byte[] imageBytes);

    void update(long id, String title, List<String> tags, String text, byte[] imageBytesOrNull);

    void delete(long id);

    void like(long id, boolean likeUp);
}

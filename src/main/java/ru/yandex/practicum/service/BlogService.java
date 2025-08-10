package ru.yandex.practicum.service;

import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

public interface BlogService {
    // Лента
    List<Post> findFeed(String tag, int pageNumber, int pageSize);
    int countFeed(String tag);

    // Пост
    Optional<Post> getPost(long id);
    long createPost(String title, List<String> tags, String text, byte[] imageBytes);
    void updatePost(long id, String title, List<String> tags, String text, byte[] imageBytes);
    void deletePost(long id);

    // Лайк
    void likePost(long id, boolean likeUp);

    // Комментарии
    long addComment(long postId, String text);
    void updateComment(long postId, long commentId, String text);
    void deleteComment(long postId, long commentId);

    // Картинка
    Optional<byte[]> loadImage(long postId);
}


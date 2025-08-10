package ru.yandex.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.BlogService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final PostRepository posts;
    private final CommentRepository comments;

    // Лента
    @Override
    public java.util.List<ru.yandex.practicum.model.Post> findFeed(String tag, int pageNumber, int pageSize) {
        String t = tag == null ? "" : tag.trim();
        int page = Math.max(pageNumber, 1);          // 1-based
        int offset = (page - 1) * pageSize;          // → 0-based смещение
        return posts.findFeed(t, offset, pageSize);
    }

    @Override
    public int countFeed(String tag) {
        String t = tag == null ? "" : tag.trim();
        return posts.countFeed(t);
    }

    // Пост
    @Override
    public Optional<Post> getPost(long id) {
        var p = posts.findById(id);
        p.ifPresent(post -> post.setComments(comments.findByPostId(id)));
        return p;
    }

    @Override
    @Transactional
    public long createPost(String title, List<String> tags, String text, byte[] imageBytes) {
        return posts.create(title, tags, text, imageBytes);
    }

    @Override
    @Transactional
    public void updatePost(long id, String title, List<String> tags, String text, byte[] imageBytes) {
        posts.update(id, title, tags, text, imageBytes);
    }

    @Override
    @Transactional
    public void deletePost(long id) {
        posts.delete(id); // comments удалятся каскадом
    }

    // Лайки
    @Override
    @Transactional
    public void likePost(long id, boolean likeUp) {
        posts.like(id, likeUp);
    }

    // Комменты
    @Override
    @Transactional
    public long addComment(long postId, String text) {
        return comments.create(postId, text);
    }

    @Override
    @Transactional
    public void updateComment(long postId, long commentId, String text) {
        comments.update(postId, commentId, text);
    }

    @Override
    @Transactional
    public void deleteComment(long postId, long commentId) {
        comments.delete(postId, commentId);
    }

    @Override
    public Optional<byte[]> loadImage(long postId) {
        return posts.findById(postId).map(Post::getImageData).filter(bytes -> bytes != null && bytes.length > 0);
    }
}

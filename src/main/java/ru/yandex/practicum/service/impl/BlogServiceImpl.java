package ru.yandex.practicum.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.CommentRepository;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.BlogService;

import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {

    private final PostRepository posts;
    private final CommentRepository comments;

    public BlogServiceImpl(PostRepository posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @Override
    public List<Post> findFeed(String tag, int pageNumber, int pageSize) {
        String t = tag == null ? "" : tag.trim();
        int page = Math.max(pageNumber, 1);          // 1-based → гарантируем минимум 1
        int offset = (page - 1) * pageSize;          // → 0-based
        return posts.findFeed(t, offset, pageSize);
    }

    @Override
    public int countFeed(String tag) {
        String t = tag == null ? "" : tag.trim();
        return posts.countFeed(t);
    }

    // остальная логика как у тебя была:
    @Override
    public Optional<Post> getPost(long id) {
        var p = posts.findById(id);
        p.ifPresent(post -> post.setComments(comments.findByPostId(id)));
        return p;
    }

    @Override
    public long createPost(String title, List<String> tags, String text, byte[] image) {
        return posts.create(title, tags, text, image);
    }

    @Override
    public void updatePost(long id, String title, List<String> tags, String text, byte[] image) {
        posts.update(id, title, tags, text, image);
    }

    @Override
    public void deletePost(long id) {
        posts.delete(id);
    }

    @Override
    public void likePost(long id, boolean like) {
        posts.like(id, like);
    }

    @Override
    public long addComment(long postId, String text) {
        return comments.create(postId, text);
    }

    @Override
    public void updateComment(long postId, long commentId, String text) {
        comments.update(postId, commentId, text);
    }

    @Override
    public void deleteComment(long postId, long commentId) {
        comments.delete(postId, commentId);
    }

    @Override
    public java.util.Optional<byte[]> loadImage(long postId) {
        return posts.findById(postId).map(Post::getImageData).filter(b -> b != null && b.length > 0);
    }
}

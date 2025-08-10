package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Paging;
import ru.yandex.practicum.service.BlogService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final BlogService blog;

    public PostController(BlogService blog) {
        this.blog = blog;
    }

    // Лента
    @GetMapping
    public String feed(@RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                       Model model) {
        String tag = (search == null) ? "" : search.trim();
        var posts = blog.findFeed(tag, pageNumber, pageSize);
        int total = blog.countFeed(tag);

        model.addAttribute("posts", posts);
        model.addAttribute("paging", new ru.yandex.practicum.model.Paging(pageNumber, pageSize, total));
        model.addAttribute("search", tag);

        return "posts";
    }


    // Форм создания/редактирования
    @GetMapping("/add")
    public String addForm() {
        return "add-post"; // в шаблоне пост проверяется как post==null
    }

    // Создать пост
    @PostMapping
    public String create(@RequestParam("title") String title,
                         @RequestParam("tags") String tagsText,
                         @RequestParam("text") String text,
                         @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        var tags = parseTags(tagsText);
        byte[] bytes = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        String contentType = (image != null && !image.isEmpty()) ? image.getContentType() : null;

        long id = blog.createPost(title, tags, text, bytes);
        return "redirect:/posts/" + id;
    }

    // Страница поста
    @GetMapping("/{id}")
    public String post(@PathVariable("id") long id, Model model) {
        var post = blog.getPost(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));
        model.addAttribute("post", post);
        return "post";
    }

    // Открыть форму редактирования
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") long id, Model model) {
        var post = blog.getPost(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));
        model.addAttribute("post", post);
        return "add-post";
    }

    // Обновить пост
    @PostMapping("/{id}")
    public String update(@PathVariable("id") long id,
                         @RequestParam("title") String title,
                         @RequestParam("tags") String tagsText,
                         @RequestParam("text") String text,
                         @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        var tags = parseTags(tagsText);
        byte[] bytes = (image != null && !image.isEmpty()) ? image.getBytes() : null;
        String contentType = (image != null && !image.isEmpty()) ? image.getContentType() : null;

        blog.updatePost(id, title, tags, text, bytes);
        return "redirect:/posts/" + id;
    }

    // Удалить пост
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        blog.deletePost(id);
        return "redirect:/posts";
    }

    // Лайк/анлайк
    @PostMapping("/{id}/like")
    public String like(@PathVariable("id") long id,
                       @RequestParam("like") boolean like) {
        blog.likePost(id, like);
        return "redirect:/posts/" + id;
    }

    // Добавить комментарий
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable("id") long postId,
                             @RequestParam("text") String text) {
        blog.addComment(postId, text);
        return "redirect:/posts/" + postId;
    }

    // Обновить комментарий
    @PostMapping("/{id}/comments/{commentId}")
    public String editComment(@PathVariable("id") long postId,
                              @PathVariable("commentId") long commentId,
                              @RequestParam("text") String text) {
        blog.updateComment(postId, commentId, text);
        return "redirect:/posts/" + postId;
    }

    // Удалить комментарий
    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") long postId,
                                @PathVariable("commentId") long commentId) {
        blog.deleteComment(postId, commentId);
        return "redirect:/posts/" + postId;
    }

    // --- helpers ---
    private static List<String> parseTags(String tagsText) {
        if (tagsText == null || tagsText.isBlank()) return List.of();
        return Arrays.stream(tagsText.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}

package ru.yandex.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.model.Paging;
import ru.yandex.practicum.service.BlogService;

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
    public String feed(@RequestParam(value = "search", defaultValue = "") String search,
                       @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
                       @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                       Model model) {

        String tag = search == null ? "" : search.trim();
        var posts = blog.findFeed(tag, pageNumber, pageSize);     // ← передаём 1-based
        int total = blog.countFeed(tag);

        model.addAttribute("posts", posts);
        model.addAttribute("search", tag);                        // ← пустая строка по умолчанию
        model.addAttribute("paging", new Paging(pageNumber, pageSize, total)); // 1-based
        return "posts";
    }

    // (c) страница поста
    @GetMapping("/{id}")
    public String show(@PathVariable("id") long id, Model model) {
        return blog.getPost(id)
                .map(p -> { model.addAttribute("post", p); return "post"; })
                .orElse("redirect:/posts");
    }

    // (d) форма создания
    @GetMapping("/add")
    public String addForm() {
        return "add-post";
    }

    // (d) создание
    @PostMapping
    public String create(@RequestParam("title") String title,
                         @RequestParam(value = "tags", defaultValue = "") String tags,
                         @RequestParam("text") String text,
                         @RequestParam(value = "image", required = false) MultipartFile image)  {
        long id = blog.createPost(title, splitTags(tags), text, bytesOrNull(image));
        return "redirect:/posts/" + id;
    }

    // (g) лайк
    @PostMapping("/{id}/like")
    public String like(@PathVariable("id") long id, @RequestParam("like") boolean like){
        blog.likePost(id, like);
        return "redirect:/posts/" + id;
    }

    // (z по ТЗ) POST /posts/{id}/edit -> редирект на форму редактирования
    @PostMapping("/{id}/edit")
    public String editRedirect(@PathVariable("id") long id) {
        return "redirect:/posts/" + id + "/edit";
    }

    // форма редактирования (GET), чтобы тест editRedirect_post_to_get_edit проходил
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") long id, Model model){
        return blog.getPost(id)
                .map(p -> { model.addAttribute("post", p); return "add-post"; })
                .orElse("redirect:/posts");
    }

    // (i) редактирование
    @PostMapping("/{id}")
    public String update(@PathVariable("id") long id,
                         @RequestParam("title") String title,
                         @RequestParam(value = "tags", defaultValue = "") String tags,
                         @RequestParam("text") String text,
                         @RequestParam(value = "image", required = false) MultipartFile image){
        blog.updatePost(id, title, splitTags(tags), text, bytesOrNull(image));
        return "redirect:/posts/" + id;
    }

    // (k) добавить комментарий
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable("id") long id, @RequestParam("text") String text)  {
        blog.addComment(id, text);
        return "redirect:/posts/" + id;
    }

    // (l) редактировать комментарий
    @PostMapping("/{id}/comments/{commentId}")
    public String editComment(@PathVariable("id") long id,
                              @PathVariable("commentId") long commentId,
                              @RequestParam("text") String text) {
        blog.updateComment(id, commentId, text);
        return "redirect:/posts/" + id;
    }

    // (m) удалить комментарий
    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") long id,
                                @PathVariable("commentId") long commentId) {
        blog.deleteComment(id, commentId);
        return "redirect:/posts/" + id;
    }

    // (n) удалить пост
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        blog.deletePost(id);
        return "redirect:/posts";
    }

    // helpers
    private static byte[] bytesOrNull(MultipartFile f) {
        try { return (f != null && !f.isEmpty()) ? f.getBytes() : null; }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    private static List<String> splitTags(String csv) {
        return (csv == null || csv.isBlank())
                ? java.util.List.of()
                : java.util.Arrays.stream(csv.split("[,\\s]+"))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}

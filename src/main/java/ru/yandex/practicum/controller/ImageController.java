package ru.yandex.practicum.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.service.BlogService;

@Controller
@RequestMapping("/images")
public class ImageController {

    private final BlogService blog;

    public ImageController(BlogService blog) {
        this.blog = blog;
    }

    @GetMapping("/{postId}")
    @ResponseBody
    public ResponseEntity<byte[]> image(@PathVariable("postId") long postId) {
        return blog.loadImage(postId)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}

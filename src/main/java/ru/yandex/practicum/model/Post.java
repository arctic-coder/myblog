package ru.yandex.practicum.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String text; // полный текст с \n

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] imageData;

    private int likesCount;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

}

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

    public String getTextPreview() {
        if (getText().isBlank()) return "";
        String[] lines = getText().split("\\R+");
        StringBuilder sb = new StringBuilder();
        int count = Math.min(lines.length, 3);
        for (int i = 0; i < count; i++) {
            if (!lines[i].isBlank()) {
                if (sb.length() > 0) sb.append(System.lineSeparator());
                sb.append(lines[i].trim());
            }
        }
        return sb.toString();
    }

    public String getTagsAsText() {
        if (tags == null || tags.isEmpty()) return "";
        return String.join(",", tags);
    }

    public java.util.List<String> getTextParts() {
        if (text == null || text.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

}

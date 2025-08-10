package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Paging {
    private final int pageNumber; // 1-based
    private final int pageSize;
    private final int total;

    public int pageNumber() { return pageNumber; }
    public int pageSize()   { return pageSize; }
    public boolean hasPrevious() { return pageNumber > 1; }
    public boolean isHasPrevious() { return hasPrevious(); }

    public boolean hasNext() {
        long shown = (long) pageNumber * pageSize; // 1-based
        return shown < total;
    }
    public boolean isHasNext() { return hasNext(); }
}

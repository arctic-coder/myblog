package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Paging {
    private final int pageNumber;
    private final int pageSize;
    private final int total;

    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    public boolean hasNext() {
        long shown = (long) (pageNumber + 1) * pageSize;
        return shown < total;
    }
}

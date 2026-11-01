package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ResponseItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseItemDto {
        private Long id;
        private String name;
        private Long ownerId;
    }
}

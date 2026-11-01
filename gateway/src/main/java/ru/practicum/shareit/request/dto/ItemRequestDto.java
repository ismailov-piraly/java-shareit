package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
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
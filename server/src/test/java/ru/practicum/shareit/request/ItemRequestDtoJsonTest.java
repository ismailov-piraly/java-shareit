package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    JacksonTester<ItemRequestDto> json;

    @Test
    void serializesCreatedDateAndResponseItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.of(2026, 10, 31, 12, 30), List.of(new ItemRequestDto.ResponseItemDto(5L, "Дрель", 2L)));
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.created").isEqualTo("2026-10-31T12:30:00");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(5);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
    }
}

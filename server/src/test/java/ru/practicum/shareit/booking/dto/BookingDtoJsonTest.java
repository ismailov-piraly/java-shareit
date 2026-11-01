package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeDatesAndHideWriteOnlyItemId() throws Exception {
        BookingDto booking = BookingDto.builder().id(10L).start(LocalDateTime.of(2026, 11, 1, 10, 0)).end(LocalDateTime.of(2026, 11, 2, 10, 0)).item(ItemDto.builder().id(5L).name("Дрель").build()).itemId(5L).booker(UserDto.builder().id(2L).name("Покупатель").email("buyer@example.com").build()).status(BookingStatus.WAITING).build();

        assertThat(json.write(booking)).extractingJsonPathStringValue("$.start").isEqualTo("2026-11-01T10:00:00");
        assertThat(json.write(booking)).extractingJsonPathStringValue("$.end").isEqualTo("2026-11-02T10:00:00");
        assertThat(json.write(booking)).doesNotHaveJsonPath("$.itemId");
        assertThat(json.write(booking)).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void shouldDeserializeItemIdForCreateRequest() throws Exception {
        String source = "{\"itemId\":5,\"start\":\"2026-11-01T10:00:00\",\"end\":\"2026-11-02T10:00:00\"}";
        BookingDto booking = new ObjectMapper().findAndRegisterModules().readValue(source, BookingDto.class);

        assertThat(booking.getItemId()).isEqualTo(5L);
        assertThat(booking.getStart()).isEqualTo(LocalDateTime.of(2026, 11, 1, 10, 0));
    }
}

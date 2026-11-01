package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository requestRepository;
    @InjectMocks
    private ItemServiceImpl service;

    private ItemDto validDto() {
        return ItemDto.builder().name("Дрель").description("Для ремонта").available(true).build();
    }

    private User owner() {
        return User.builder().id(1L).name("Владелец").email("owner@example.com").build();
    }

    private Item item() {
        return Item.builder().id(10L).name("Дрель").description("Для ремонта").available(true).owner(owner()).build();
    }

    @Test
    void addItemSavesItemForOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ItemDto result = service.addItem(1L, validDto());

        assertEquals(10L, result.getId());
        assertEquals("Дрель", result.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItemWithRequest() {
        ItemDto dto = validDto();
        dto.setRequestId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(requestRepository.findById(5L)).thenReturn(Optional.of(ItemRequest.builder().id(5L).description("Нужна дрель").build()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(5L, service.addItem(1L, dto).getRequestId());
    }

    @Test
    void addItemRejectsUnknownOwnerAndInvalidFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.addItem(1L, validDto()));

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        ItemDto invalidName = validDto();
        invalidName.setName(" ");
        assertThrows(ValidationException.class, () -> service.addItem(1L, invalidName));
        ItemDto invalidDescription = validDto();
        invalidDescription.setDescription(null);
        assertThrows(ValidationException.class, () -> service.addItem(1L, invalidDescription));
        ItemDto invalidAvailable = validDto();
        invalidAvailable.setAvailable(null);
        assertThrows(ValidationException.class, () -> service.addItem(1L, invalidAvailable));
    }

    @Test
    void addItemRejectsUnknownRequest() {
        ItemDto dto = validDto();
        dto.setRequestId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addItem(1L, dto));
    }

    @Test
    void updateItemChangesProvidedFields() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ItemDto patch = ItemDto.builder().name("Новое имя").description("Новое описание").available(false).build();

        ItemDto result = service.updateItem(1L, 10L, patch);

        assertEquals("Новое имя", result.getName());
        assertEquals("Новое описание", result.getDescription());
        assertFalse(result.getAvailable());
    }

    @Test
    void updateItemRejectsMissingItemWrongOwnerAndBlankValues() {
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.updateItem(1L, 10L, validDto()));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        assertThrows(NotFoundException.class, () -> service.updateItem(2L, 10L, validDto()));

        ItemDto blankName = ItemDto.builder().name(" ").build();
        assertThrows(ValidationException.class, () -> service.updateItem(1L, 10L, blankName));

        ItemDto blankDescription = ItemDto.builder().description(" ").build();
        assertThrows(ValidationException.class, () -> service.updateItem(1L, 10L, blankDescription));
    }

    @Test
    void getItemReturnsComments() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of());

        assertEquals(10L, service.getItemById(10L).getId());
        assertTrue(service.getItemById(10L).getComments().isEmpty());
    }

    @Test
    void getItemRejectsUnknownId() {
        when(itemRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getItemById(404L));
    }

    @Test
    void getItemsByOwnerRejectsUnknownUserAndReturnsItems() {
        when(userRepository.existsById(404L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.getItemsByOwner(404L));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item()));
        when(commentRepository.findByItemIdOrderByCreatedDesc(10L)).thenReturn(List.of());

        assertEquals(1, service.getItemsByOwner(1L).size());
    }

    @Test
    void searchReturnsEmptyForBlankAndSearchesTrimmedText() {
        assertTrue(service.searchItems("  ").isEmpty());
        when(itemRepository.searchAvailable("дрель")).thenReturn(List.of(item()));

        assertEquals(1, service.searchItems(" дрель ").size());
    }

    @Test
    void addCommentValidatesAndSavesAfterCompletedBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).name("Пользователь").email("user@example.com").build()));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndLessThanEqual(eq(10L), eq(2L), any(), any())).thenReturn(true);
        when(commentRepository.save(any())).thenAnswer(invocation -> {
            ru.practicum.shareit.item.Comment comment = invocation.getArgument(0);
            comment.setId(7L);
            return comment;
        });

        assertEquals("Хорошая вещь", service.addComment(2L, 10L, "Хорошая вещь").getText());
    }

    @Test
    void addCommentRejectsMissingEntitiesBlankTextAndNoCompletedBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.addComment(2L, 10L, "Отзыв"));

        when(userRepository.findById(2L)).thenReturn(Optional.of(owner()));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.addComment(2L, 10L, "Отзыв"));

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item()));
        assertThrows(ValidationException.class, () -> service.addComment(2L, 10L, " "));
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndLessThanEqual(eq(10L), eq(2L), any(), any())).thenReturn(false);
        assertThrows(ValidationException.class, () -> service.addComment(2L, 10L, "Отзыв"));
    }
}

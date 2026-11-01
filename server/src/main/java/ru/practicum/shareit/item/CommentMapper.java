package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder().id(comment.getId()).text(comment.getText())
                .authorName(comment.getAuthor().getName()).created(comment.getCreated()).build();
    }
}
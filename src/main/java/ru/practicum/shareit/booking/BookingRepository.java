package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemIdAndStartBeforeAndStatus(Long itemId, LocalDateTime time, BookingStatus status);

    List<Booking> findByItemIdAndStartAfterAndStatus(Long itemId, LocalDateTime time, BookingStatus status);

    @Query("select b from Booking b where b.item.id = :itemId and b.status = :status " + "and b.end <= :now order by b.end desc")
    List<Booking> findLastForItem(@Param("itemId") Long itemId, @Param("status") BookingStatus status, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.item.id = :itemId and b.status = :status " + "and b.start >= :now order by b.start asc")
    List<Booking> findNextForItem(@Param("itemId") Long itemId, @Param("status") BookingStatus status, @Param("now") LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndEndLessThanEqualOrderByEndDesc(Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartGreaterThanEqualOrderByStartAsc(Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndStatusAndEndLessThanEqual(Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);
}

package dev.muho.booking.domain.booking.repository;

import dev.muho.booking.domain.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository {

    Optional<Booking> findByBookingId(String bookingId);

    Optional<Booking> findById(Long id);

    Page<Booking> findByHotelId(Long hotelId, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Booking save(Booking booking);

    void deleteByBookingId(String bookingId);
}


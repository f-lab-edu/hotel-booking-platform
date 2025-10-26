package dev.muho.booking.domain.booking.repository;

import dev.muho.booking.domain.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingId(String bookingId);

    Page<Booking> findByHotelId(Long hotelId, Pageable pageable);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    void deleteByBookingId(String bookingId);
}


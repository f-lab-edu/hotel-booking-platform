package dev.muho.booking.domain.booking.service;

import dev.muho.booking.domain.booking.dto.api.BookingStatusUpdateRequest;
import dev.muho.booking.domain.booking.dto.command.BookingCreateCommand;
import dev.muho.booking.domain.booking.dto.command.BookingInfoResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

public interface BookingService {

    Page<BookingInfoResult> getBookingsByHotel(Long hotelId, Pageable pageable);

    Page<BookingInfoResult> getMyBookings(Long userId, Pageable pageable);

    BookingInfoResult getBooking(String bookingId);

    BookingInfoResult createBooking(@Valid BookingCreateCommand command, Long userId);

    BookingInfoResult changeBookingStatus(String bookingId, BookingStatusUpdateRequest statusUpdateRequest);

    void deleteBooking(String bookingId);
}


package dev.muho.booking.domain.booking.service;

import dev.muho.booking.domain.booking.dto.api.BookingStatusUpdateRequest;
import dev.muho.booking.domain.booking.dto.command.BookingCreateCommand;
import dev.muho.booking.domain.booking.dto.command.BookingInfoResult;
import dev.muho.booking.domain.booking.entity.Booking;
import dev.muho.booking.domain.booking.error.BookingNotFoundException;
import dev.muho.booking.domain.booking.repository.BookingRepository;
import dev.muho.booking.domain.booking.client.HotelClient;
import dev.muho.booking.domain.booking.client.RoomTypeClient;
import dev.muho.booking.domain.booking.client.RatePlanClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;
    private final RoomTypeClient roomTypeClient;
    private final RatePlanClient ratePlanClient;

    @Override
    public Page<BookingInfoResult> getBookingsByHotel(Long hotelId, Pageable pageable) {
        return bookingRepository.findByHotelId(hotelId, pageable).map(BookingInfoResult::from);
    }

    @Override
    public Page<BookingInfoResult> getMyBookings(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(BookingInfoResult::from);
    }

    @Override
    public BookingInfoResult getBooking(String bookingId) {
        Booking b = bookingRepository.findByBookingId(bookingId).orElseThrow(BookingNotFoundException::new);
        return BookingInfoResult.from(b);
    }

    @Override
    @Transactional
    public BookingInfoResult createBooking(BookingCreateCommand command, Long userId) {
        String bookingId = UUID.randomUUID().toString();
        String hotelName = hotelClient.findHotelName(command.hotelId()).orElse(null);
        String roomTypeName = roomTypeClient.findRoomTypeName(command.roomTypeId()).orElse(null);
        String ratePlanName = ratePlanClient.findRatePlanName(command.ratePlanId()).orElse(null);

        Booking booking = Booking.createNew(
                bookingId,
                userId,
                null, // guestName not provided in request
                command.hotelId(),
                hotelName,
                command.roomTypeId(),
                roomTypeName,
                command.ratePlanId(),
                ratePlanName,
                command.checkInDate(),
                command.checkOutDate(),
                command.guestCount(),
                command.basePrice(),
                command.finalPrice()
        );
        Booking saved = bookingRepository.save(booking);
        return BookingInfoResult.from(saved);
    }

    @Override
    @Transactional
    public BookingInfoResult changeBookingStatus(String bookingId, BookingStatusUpdateRequest statusUpdateRequest) {
        Booking b = bookingRepository.findByBookingId(bookingId).orElseThrow(BookingNotFoundException::new);
        b.updateStatus(statusUpdateRequest.getStatus());
        Booking saved = bookingRepository.save(b);
        return BookingInfoResult.from(saved);
    }

    @Override
    @Transactional
    public void deleteBooking(String bookingId) {
        bookingRepository.deleteByBookingId(bookingId);
    }
}


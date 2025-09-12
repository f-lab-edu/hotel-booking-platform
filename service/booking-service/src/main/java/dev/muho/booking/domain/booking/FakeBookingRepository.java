package dev.muho.booking.domain.booking;

import dev.muho.booking.domain.booking.dto.BookingRequest;
import dev.muho.booking.domain.booking.dto.BookingResponse;
import dev.muho.booking.domain.booking.dto.BookingStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FakeBookingRepository {

    private final Map<String, BookingResponse> db = new HashMap<>();

    public Page<BookingResponse> findAll(Pageable pageable, Long userId, Long hotelId) {
        List<BookingResponse> bookings = new ArrayList<>(db.values());

        if (userId != null) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getUserId().equals(userId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (hotelId != null) {
            bookings = bookings.stream()
                    .filter(booking -> booking.getHotelId().equals(hotelId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        bookings.sort(Comparator.comparing(BookingResponse::getBookingId));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), bookings.size());

        if (start > bookings.size()) {
            return Page.empty(pageable);
        }

        List<BookingResponse> content = bookings.subList(start, end);
        return new PageImpl<>(content, pageable, bookings.size());
    }

    public BookingResponse findById(String bookingId) {
        return db.get(bookingId);
    }

    public BookingResponse save(BookingRequest request) {
        BookingResponse newBooking = BookingResponse.builder()
                .bookingId(UUID.randomUUID().toString())
                .userId(1L) // Sample user ID
                .guestName("홍길동") // Sample guest name
                .hotelId(request.getHotelId())
                .hotelName("Sample Hotel") // Sample data
                .roomTypeName("Deluxe Room") // Sample data
                .ratePlanName("Standard Rate") // Sample data
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .guestCount(request.getGuestCount())
                .basePrice(request.getBasePrice())
                .finalPrice(request.getFinalPrice())
                .status(BookingStatus.RESERVED) // Default status
                .promotions(List.of()) // Empty list for simplicity
                .rateAdjustments(List.of()) // Empty list for simplicity
                .build();
        db.put(newBooking.getBookingId(), newBooking);
        return newBooking;
    }

    public BookingResponse changeBookingStatus(String bookingId, BookingStatusUpdateRequest request) {
        BookingResponse bookingResponse = db.get(bookingId);
        BookingResponse updateBookingResponse = BookingResponse.builder()
                .bookingId(bookingResponse.getBookingId())
                .userId(bookingResponse.getUserId())
                .guestName(bookingResponse.getGuestName())
                .hotelId(bookingResponse.getHotelId())
                .hotelName(bookingResponse.getHotelName())
                .roomTypeName(bookingResponse.getRoomTypeName())
                .ratePlanName(bookingResponse.getRatePlanName())
                .checkInDate(bookingResponse.getCheckInDate())
                .checkOutDate(bookingResponse.getCheckOutDate())
                .guestCount(bookingResponse.getGuestCount())
                .basePrice(bookingResponse.getBasePrice())
                .finalPrice(bookingResponse.getFinalPrice())
                .status(request.getStatus())
                .promotions(bookingResponse.getPromotions())
                .rateAdjustments(bookingResponse.getRateAdjustments())
                .build();
        db.put(bookingId, updateBookingResponse);
        return updateBookingResponse;
    }

    public void delete(String bookingId) {
        db.remove(bookingId);
    }

    public void clear() {
        db.clear();
    }
}

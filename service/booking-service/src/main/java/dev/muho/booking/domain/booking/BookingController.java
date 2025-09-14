package dev.muho.booking.domain.booking;

import dev.muho.booking.domain.booking.dto.api.BookingRequest;
import dev.muho.booking.domain.booking.dto.api.BookingResponse;
import dev.muho.booking.domain.booking.dto.api.BookingStatusUpdateRequest;
import dev.muho.booking.domain.booking.dto.command.BookingCreateCommand;
import dev.muho.booking.domain.booking.dto.command.BookingInfoResult;
import dev.muho.booking.domain.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/hotels/{hotelId}")
    public Page<BookingResponse> getBookingsByHotel(@PathVariable Long hotelId, Pageable pageable) {
        return bookingService.getBookingsByHotel(hotelId, pageable).map(BookingResponse::from);
    }

    @GetMapping("/my")
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        Long currentUserId = currentUserId();
        return bookingService.getMyBookings(currentUserId, pageable).map(BookingResponse::from);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        BookingInfoResult r = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(BookingResponse.from(r));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        BookingCreateCommand command = BookingCreateCommand.from(request);
        BookingInfoResult created = bookingService.createBooking(command, currentUserId());
        return BookingResponse.from(created);
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> changeBookingStatus(@PathVariable String bookingId, @Valid @RequestBody BookingStatusUpdateRequest request) {
        BookingInfoResult updated = bookingService.changeBookingStatus(bookingId, request);
        return ResponseEntity.ok(BookingResponse.from(updated));
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable String bookingId) {
        bookingService.deleteBooking(bookingId);
    }

    // TODO: 인증 연동 필요
    private Long currentUserId() {
        return 1L;
    }
}

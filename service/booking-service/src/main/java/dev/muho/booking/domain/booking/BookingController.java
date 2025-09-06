package dev.muho.booking.domain.booking;

import dev.muho.booking.domain.booking.dto.BookingRequest;
import dev.muho.booking.domain.booking.dto.BookingResponse;
import dev.muho.booking.domain.booking.dto.BookingStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final FakeBookingRepository bookingRepository;

    @GetMapping("/hotels/{hotelId}")
    public Page<BookingResponse> getBookingsByHotel(@PathVariable Long hotelId, Pageable pageable) {
        return bookingRepository.findAll(pageable, null, hotelId);
    }

    @GetMapping("/my")
    public Page<BookingResponse> getMyBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable, 1L, null);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingId) {
        BookingResponse bookingResponse = bookingRepository.findById(bookingId);
        if (bookingResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookingResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingRepository.save(request);
    }

    @PatchMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> changeBookingStatus(String bookingId, @Valid @RequestBody BookingStatusUpdateRequest request) {
        BookingResponse bookingResponse = bookingRepository.findById(bookingId);
        if (bookingResponse == null) {
            return ResponseEntity.notFound().build();
        }
        bookingResponse = bookingRepository.changeBookingStatus(bookingId, request);
        return ResponseEntity.ok(bookingResponse);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable String bookingId) {
        bookingRepository.delete(bookingId);
    }
}

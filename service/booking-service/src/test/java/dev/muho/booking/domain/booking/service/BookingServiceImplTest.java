package dev.muho.booking.domain.booking.service;

import dev.muho.booking.domain.booking.dto.command.BookingCreateCommand;
import dev.muho.booking.domain.booking.dto.command.BookingInfoResult;
import dev.muho.booking.domain.booking.entity.Booking;
import dev.muho.booking.domain.booking.error.BookingNotFoundException;
import dev.muho.booking.domain.booking.repository.BookingRepository;
import dev.muho.booking.domain.booking.client.HotelClient;
import dev.muho.booking.domain.booking.client.RoomTypeClient;
import dev.muho.booking.domain.booking.client.RatePlanClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    HotelClient hotelClient;
    @Mock
    RoomTypeClient roomTypeClient;
    @Mock
    RatePlanClient ratePlanClient;

    @InjectMocks
    BookingServiceImpl bookingService;

    @Test
    @DisplayName("getBookingsByHotel: 결과 매핑")
    void getBookingsByHotel() {
        Booking b1 = Booking.createNew(UUID.randomUUID().toString(), 1L, "guest", 10L, "Hotel A", 100L, "RT A", 1000L, "RP A",
                LocalDate.now(), LocalDate.now().plusDays(1), 2, BigDecimal.valueOf(100), BigDecimal.valueOf(120));
        Booking b2 = Booking.createNew(UUID.randomUUID().toString(), 2L, "guest2", 10L, "Hotel A", 101L, "RT B", 1001L, "RP B",
                LocalDate.now(), LocalDate.now().plusDays(2), 1, BigDecimal.valueOf(200), BigDecimal.valueOf(220));
        Pageable pageable = PageRequest.of(0, 10);
        given(bookingRepository.findByHotelId(10L, pageable)).willReturn(new PageImpl<>(List.of(b1, b2), pageable, 2));

        var page = bookingService.getBookingsByHotel(10L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(BookingInfoResult::hotelName)
                .containsExactlyInAnyOrder("Hotel A", "Hotel A");
        verify(bookingRepository).findByHotelId(10L, pageable);
    }

    @Test
    @DisplayName("getBooking: 존재하지 않음 예외")
    void getBooking_notFound() {
        given(bookingRepository.findByBookingId("no-such")).willReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getBooking("no-such")).isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    @DisplayName("createBooking: 정상 생성 및 클라이언트 호출")
    void createBooking_success() {
        BookingCreateCommand cmd = new BookingCreateCommand(10L, 100L, 1000L, LocalDate.now(), LocalDate.now().plusDays(1), 2, BigDecimal.valueOf(100), BigDecimal.valueOf(120));
        given(hotelClient.findHotelName(10L)).willReturn(Optional.of("Hotel A"));
        given(roomTypeClient.findRoomTypeName(100L)).willReturn(Optional.of("Deluxe"));
        given(ratePlanClient.findRatePlanName(1000L)).willReturn(Optional.of("Best"));

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        given(bookingRepository.save(any(Booking.class))).willAnswer(inv -> inv.getArgument(0));

        BookingInfoResult result = bookingService.createBooking(cmd, 99L);

        verify(hotelClient).findHotelName(10L);
        verify(roomTypeClient).findRoomTypeName(100L);
        verify(ratePlanClient).findRatePlanName(1000L);
        verify(bookingRepository).save(captor.capture());

        Booking saved = captor.getValue();
        assertThat(saved.getHotelId()).isEqualTo(10L);
        assertThat(saved.getUserId()).isEqualTo(99L);
        assertThat(result.hotelName()).isEqualTo("Hotel A");
    }

    @Test
    @DisplayName("changeBookingStatus: 성공")
    void changeBookingStatus_success() {
        String bookingId = UUID.randomUUID().toString();
        Booking b = Booking.createNew(bookingId, 1L, "guest", 10L, "Hotel A", 100L, "RT A", 1000L, "RP A",
                LocalDate.now(), LocalDate.now().plusDays(1), 2, BigDecimal.valueOf(100), BigDecimal.valueOf(120));
        given(bookingRepository.findByBookingId(bookingId)).willReturn(Optional.of(b));
        // build request via Lombok builder
        dev.muho.booking.domain.booking.dto.api.BookingStatusUpdateRequest req = dev.muho.booking.domain.booking.dto.api.BookingStatusUpdateRequest
                .builder()
                .status(dev.muho.booking.domain.booking.entity.BookingStatus.CANCELLED)
                .build();

        given(bookingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        var res = bookingService.changeBookingStatus(bookingId, req);

        assertThat(res.status()).isEqualTo(dev.muho.booking.domain.booking.entity.BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("deleteBooking: delete 호출 위임")
    void deleteBooking() {
        String bookingId = "to-delete";
        doNothing().when(bookingRepository).deleteByBookingId(bookingId);
        bookingService.deleteBooking(bookingId);
        verify(bookingRepository).deleteByBookingId(bookingId);
    }
}



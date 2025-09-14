package dev.muho.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.booking.domain.booking.BookingController;
import dev.muho.booking.domain.booking.FakeBookingRepository;
import dev.muho.booking.domain.booking.dto.api.BookingRequest;
import dev.muho.booking.domain.booking.dto.api.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(FakeDBConfiguration.class)
public class BookingE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeBookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.clear();
    }

    @Test
    void 호텔_예약_목록_조회_성공() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .hotelId(1L)
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        BookingRequest bookingRequest2 = BookingRequest.builder()
                .hotelId(2L)
                .roomTypeId(2L)
                .ratePlanId(2L)
                .checkInDate(LocalDate.of(2026, 2, 1))
                .checkOutDate(LocalDate.of(2026, 2, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        bookingRepository.save(bookingRequest1);
        bookingRepository.save(bookingRequest2);

        mockMvc.perform(get("/v1/bookings/hotels/1"))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].hotelId").value(1L));
    }

    @Test
    void 내_예약_목록_조회_성공() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .hotelId(1L)
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        BookingRequest bookingRequest2 = BookingRequest.builder()
                .hotelId(2L)
                .roomTypeId(2L)
                .ratePlanId(2L)
                .checkInDate(LocalDate.of(2026, 2, 1))
                .checkOutDate(LocalDate.of(2026, 2, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        bookingRepository.save(bookingRequest1);
        bookingRepository.save(bookingRequest2);

        mockMvc.perform(get("/v1/bookings/my"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].hotelId").value(1L))
                .andExpect(jsonPath("$.content[1].hotelId").value(2L));
    }

    @Test
    void 예약_조회_성공() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .hotelId(1L)
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        BookingResponse bookingResponse = bookingRepository.save(bookingRequest1);

        mockMvc.perform(get("/v1/bookings/{bookingId}", bookingResponse.getBookingId()))
                .andExpect(jsonPath("$.hotelId").value(1L));
    }

    @Test
    void 예약_조회_실패_없는_예약번호() throws Exception {
        mockMvc.perform(get("/v1/bookings/a"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 예약_생성_성공() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .hotelId(1L)
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        String requestJson = objectMapper.writeValueAsString(bookingRequest1);

        mockMvc.perform(post("/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());
    }

    @Test
    void 예약_실패_잘못된_입력값() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        String requestJson = objectMapper.writeValueAsString(bookingRequest1);

        mockMvc.perform(post("/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 예약_삭제_성공() throws Exception {
        BookingRequest bookingRequest1 = BookingRequest.builder()
                .hotelId(1L)
                .roomTypeId(1L)
                .ratePlanId(1L)
                .checkInDate(LocalDate.of(2026, 1, 1))
                .checkOutDate(LocalDate.of(2026, 1, 2))
                .guestCount(2)
                .basePrice(BigDecimal.TEN)
                .finalPrice(BigDecimal.valueOf(20))
                .build();
        BookingResponse bookingResponse = bookingRepository.save(bookingRequest1);

        mockMvc.perform(delete("/v1/bookings/{bookingId}", bookingResponse.getBookingId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/bookings/{bookingId}", bookingResponse.getBookingId()))
                .andExpect(status().isNotFound());
    }
}

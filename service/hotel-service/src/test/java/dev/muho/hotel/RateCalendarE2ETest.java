package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.ratecalendar.AdjustmentType;
import dev.muho.hotel.domain.ratecalendar.FakeRateCalendarRepository;
import dev.muho.hotel.domain.ratecalendar.RateCalendarController;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarCreateRequest;
import dev.muho.hotel.domain.ratecalendar.dto.RateCalendarUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateCalendarController.class)
@Import(FakeDBConfiguration.class)
public class RateCalendarE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeRateCalendarRepository rateCalendarRepository;

    @BeforeEach
    void setUp() {
        rateCalendarRepository.clear();
    }

    @Test
    void 캘린더_목록_조회_성공() throws Exception {
        // 여러 캘린더 생성
        RateCalendarCreateRequest request1 = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        RateCalendarCreateRequest request2 = RateCalendarCreateRequest.builder()
                .name("캘린더 2")
                .startDate(LocalDate.of(2024, 8, 1))
                .endDate(LocalDate.of(2024, 8, 31))
                .adjustmentType(AdjustmentType.PERCENTAGE)
                .adjustmentValue(java.math.BigDecimal.valueOf(10))
                .build();
        rateCalendarRepository.save(1L, request1);
        rateCalendarRepository.save(1L, request2);

        // 목록 조회
        mockMvc.perform(get("/v1/hotels/1/rate-calendars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("캘린더 1"))
                .andExpect(jsonPath("$.content[1].name").value("캘린더 2"));
    }

    @Test
    void 캘린더_조회_성공() throws Exception {
        // 캘린더 생성
        RateCalendarCreateRequest request = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        var createdCalendar = rateCalendarRepository.save(1L, request);

        // 단건 조회
        mockMvc.perform(get("/v1/hotels/1/rate-calendars/" + createdCalendar.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("캘린더 1"))
                .andExpect(jsonPath("$.adjustmentType").value("FIXED_AMOUNT"))
                .andExpect(jsonPath("$.startDate").value("2024-07-01"))
                .andExpect(jsonPath("$.endDate").value("2024-07-31"))
                .andExpect(jsonPath("$.adjustmentValue").value(10000));
    }

    @Test
    void 캘린더_조회_실패_존재하지_않는_캘린더() throws Exception {
        mockMvc.perform(get("/v1/hotels/1/rate-calendars/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 캘린더_생성_성공() throws Exception {
        RateCalendarCreateRequest request = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .applicableDays(Set.of(DayOfWeek.MONDAY))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/hotels/1/rate-calendars")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("캘린더 1"))
                .andExpect(jsonPath("$.adjustmentType").value("FIXED_AMOUNT"))
                .andExpect(jsonPath("$.startDate").value("2024-07-01"))
                .andExpect(jsonPath("$.endDate").value("2024-07-31"))
                .andExpect(jsonPath("$.applicableDays").isArray())
                .andExpect(jsonPath("$.adjustmentValue").value(10000));
    }

    @Test
    void 캘린더_생성_실패_잘못된_입력값() throws Exception {
        RateCalendarCreateRequest request = RateCalendarCreateRequest.builder()
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentValue(java.math.BigDecimal.valueOf(-1))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/hotels/1/rate-calendars")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("규칙 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.adjustmentType").value("조정 유형은 필수입니다."))
                .andExpect(jsonPath("$.validation.adjustmentValue").value("조정 값은 양수여야 합니다."));
    }

    @Test
    void 캘린더_수정_성공() throws Exception {
        // 캘린더 생성
        RateCalendarCreateRequest createRequest = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        var createdCalendar = rateCalendarRepository.save(1L, createRequest);

        // 캘린더 수정
        var updateRequest = RateCalendarUpdateRequest.builder()
                .name("수정된 캘린더")
                .startDate(LocalDate.of(2024, 7, 5))
                .endDate(LocalDate.of(2024, 7, 25))
                .applicableDays(Set.of(DayOfWeek.FRIDAY))
                .adjustmentType(AdjustmentType.PERCENTAGE)
                .adjustmentValue(java.math.BigDecimal.valueOf(15))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/hotels/1/rate-calendars/" + createdCalendar.getId())
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된 캘린더"))
                .andExpect(jsonPath("$.adjustmentType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.startDate").value("2024-07-05"))
                .andExpect(jsonPath("$.endDate").value("2024-07-25"))
                .andExpect(jsonPath("$.applicableDays").isArray())
                .andExpect(jsonPath("$.adjustmentValue").value(15));
    }

    @Test
    void 캘린더_수정_실패_존재하지_않는_캘린더() throws Exception {
        var updateRequest = RateCalendarUpdateRequest.builder()
                .name("수정된 캘린더")
                .startDate(LocalDate.of(2024, 7, 5))
                .endDate(LocalDate.of(2024, 7, 25))
                .applicableDays(Set.of(DayOfWeek.FRIDAY))
                .adjustmentType(AdjustmentType.PERCENTAGE)
                .adjustmentValue(java.math.BigDecimal.valueOf(15))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/hotels/1/rate-calendars/999")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void 캘린더_수정_실패_잘못된_입력값() throws Exception {
        // 캘린더 생성
        RateCalendarCreateRequest createRequest = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        var createdCalendar = rateCalendarRepository.save(1L, createRequest);

        // 캘린더 수정 - 잘못된 입력값
        var updateRequest = RateCalendarUpdateRequest.builder()
                .startDate(LocalDate.of(2024, 7, 5))
                .endDate(LocalDate.of(2024, 7, 25))
                .adjustmentValue(java.math.BigDecimal.valueOf(-1))
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/hotels/1/rate-calendars/" + createdCalendar.getId())
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("규칙 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.adjustmentType").value("조정 유형은 필수입니다."))
                .andExpect(jsonPath("$.validation.adjustmentValue").value("조정 값은 양수여야 합니다."));
    }

    @Test
    void 캘린더_삭제_성공() throws Exception {
        // 캘린더 생성
        RateCalendarCreateRequest createRequest = RateCalendarCreateRequest.builder()
                .name("캘린더 1")
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 7, 31))
                .adjustmentType(AdjustmentType.FIXED_AMOUNT)
                .adjustmentValue(java.math.BigDecimal.valueOf(10000))
                .build();
        var createdCalendar = rateCalendarRepository.save(1L, createRequest);

        // 캘린더 삭제
        mockMvc.perform(delete("/v1/hotels/1/rate-calendars/" + createdCalendar.getId()))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/v1/hotels/1/rate-calendars/" + createdCalendar.getId()))
                .andExpect(status().isNotFound());
    }
}

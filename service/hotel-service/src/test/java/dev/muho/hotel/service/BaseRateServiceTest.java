package dev.muho.hotel.service;

import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.BaseRateBulkUpdateRequest;
import dev.muho.hotel.dto.request.BaseRateSearchRequest;
import dev.muho.hotel.dto.response.BaseRateResponse;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.repository.BaseRateRepository;
import dev.muho.hotel.repository.RatePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class BaseRateServiceTest {

    @InjectMocks
    private BaseRateService baseRateService;

    @Mock
    private RatePlanRepository ratePlanRepository;
    @Mock
    private BaseRateRepository baseRateRepository;

    private RatePlan ratePlan;
    private BaseRate baseRate;
    private BaseRateSearchRequest baseRateSearchRequest;
    private BaseRateBulkUpdateRequest baseRateBulkUpdateRequest;

    @BeforeEach
    public void setUp() {
        Hotel hotel = Hotel.builder()
                .name("테스트 호텔")
                .address("서울시 강남구")
                .rating(5)
                .build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        RoomType roomType = RoomType.builder()
                .hotel(hotel)
                .name("디럭스 룸")
                .standardCapacity(2)
                .maxCapacity(4)
                .build();
        ReflectionTestUtils.setField(roomType, "id", 1L);

        ratePlan = RatePlan.builder()
                .roomType(roomType)
                .name("스탠다드 요금제")
                .minNights(1)
                .includesBreakfast(false)
                .build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        baseRate = BaseRate.builder()
                .ratePlan(ratePlan)
                .date(LocalDate.of(2024, 1, 1))
                .price(BigDecimal.valueOf(100000))
                .build();
        ReflectionTestUtils.setField(baseRate, "id", 1L);

        baseRateSearchRequest = new BaseRateSearchRequest();
        ReflectionTestUtils.setField(baseRateSearchRequest, "startDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(baseRateSearchRequest, "endDate", LocalDate.of(2024, 1, 3));

        baseRateBulkUpdateRequest = new BaseRateBulkUpdateRequest();
        ReflectionTestUtils.setField(baseRateBulkUpdateRequest, "ratePlanId", 1L);
        ReflectionTestUtils.setField(baseRateBulkUpdateRequest, "startDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(baseRateBulkUpdateRequest, "endDate", LocalDate.of(2024, 1, 3));
        ReflectionTestUtils.setField(baseRateBulkUpdateRequest, "price", BigDecimal.valueOf(120000));
    }

    @Nested
    @DisplayName("기본 요금 조회")
    class GetBaseRatesTests {

        @Test
        @DisplayName("기본 요금 조회 성공 - 기존 요금이 있는 경우")
        void getBaseRates_Success_WithExistingRates() {
            // given
            Long ratePlanId = 1L;
            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );

            BaseRate rate2 = BaseRate.builder()
                    .ratePlan(ratePlan)
                    .date(LocalDate.of(2024, 1, 2))
                    .price(BigDecimal.valueOf(110000))
                    .build();

            List<BaseRate> existingRates = Arrays.asList(baseRate, rate2);

            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange)))
                    .willReturn(existingRates);

            // when
            List<BaseRateResponse> responses = baseRateService.getBaseRates(ratePlanId, baseRateSearchRequest);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);

            // 첫 번째 날짜 (기존 요금 있음)
            BaseRateResponse response1 = responses.get(0);
            assertThat(response1.getDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(response1.getPrice()).isEqualTo(BigDecimal.valueOf(100000));

            // 두 번째 날짜 (기존 요금 있음)
            BaseRateResponse response2 = responses.get(1);
            assertThat(response2.getDate()).isEqualTo(LocalDate.of(2024, 1, 2));
            assertThat(response2.getPrice()).isEqualTo(BigDecimal.valueOf(110000));

            // 세 번째 날짜 (기존 요금 없음 - null)
            BaseRateResponse response3 = responses.get(2);
            assertThat(response3.getDate()).isEqualTo(LocalDate.of(2024, 1, 3));
            assertThat(response3.getPrice()).isNull();

            then(ratePlanRepository).should().findById(ratePlanId);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
        }

        @Test
        @DisplayName("기본 요금 조회 성공 - 기존 요금이 없는 경우")
        void getBaseRates_Success_WithNoExistingRates() {
            // given
            Long ratePlanId = 1L;
            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );

            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange)))
                    .willReturn(Collections.emptyList());

            // when
            List<BaseRateResponse> responses = baseRateService.getBaseRates(ratePlanId, baseRateSearchRequest);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);

            responses.forEach(response -> {
                assertThat(response.getPrice()).isNull();
            });

            then(ratePlanRepository).should().findById(ratePlanId);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
        }

        @Test
        @DisplayName("존재하지 않는 요금제로 기본 요금 조회 시 예외 발생")
        void getBaseRates_RatePlanNotFound_ThrowsException() {
            // given
            Long ratePlanId = 999L;

            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> baseRateService.getBaseRates(ratePlanId, baseRateSearchRequest))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("단일 날짜 기본 요금 조회 성공")
        void getBaseRates_Success_SingleDate() {
            // given
            Long ratePlanId = 1L;
            BaseRateSearchRequest singleDateRequest = new BaseRateSearchRequest();
            ReflectionTestUtils.setField(singleDateRequest, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(singleDateRequest, "endDate", LocalDate.of(2024, 1, 1));

            List<LocalDate> dateRange = Arrays.asList(LocalDate.of(2024, 1, 1));

            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange)))
                    .willReturn(Arrays.asList(baseRate));

            // when
            List<BaseRateResponse> responses = baseRateService.getBaseRates(ratePlanId, singleDateRequest);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(responses.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(100000));

            then(ratePlanRepository).should().findById(ratePlanId);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
        }
    }

    @Nested
    @DisplayName("기본 요금 대량 업데이트")
    class BulkUpdateTests {

        @Test
        @DisplayName("기본 요금 대량 업데이트 성공 - 기존 요금 있는 경우")
        void bulkUpdate_Success_WithExistingRates() {
            // given
            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );

            given(ratePlanRepository.findById(1L)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dateRange))
                    .willReturn(Arrays.asList(baseRate)); // 첫 번째 날짜만 기존 요금 있음

            // when
            baseRateService.bulkUpdate(baseRateBulkUpdateRequest);

            // then
            then(ratePlanRepository).should().findById(1L);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
            then(baseRateRepository).should(times(2)).save(any(BaseRate.class)); // 새로운 요금 2개 생성
        }

        @Test
        @DisplayName("기본 요금 대량 업데이트 성공 - 기존 요금 없는 경우")
        void bulkUpdate_Success_WithNoExistingRates() {
            // given
            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );

            given(ratePlanRepository.findById(1L)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dateRange))
                    .willReturn(Collections.emptyList());

            // when
            baseRateService.bulkUpdate(baseRateBulkUpdateRequest);

            // then
            then(ratePlanRepository).should().findById(1L);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
            then(baseRateRepository).should(times(3)).save(any(BaseRate.class)); // 새로운 요금 3개 생성
        }

        @Test
        @DisplayName("존재하지 않는 요금제로 대량 업데이트 시 예외 발생")
        void bulkUpdate_RatePlanNotFound_ThrowsException() {
            // given
            ReflectionTestUtils.setField(baseRateBulkUpdateRequest, "ratePlanId", 999L);
            given(ratePlanRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> baseRateService.bulkUpdate(baseRateBulkUpdateRequest))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(999L);
        }

        @Test
        @DisplayName("단일 날짜 대량 업데이트 성공")
        void bulkUpdate_Success_SingleDate() {
            // given
            BaseRateBulkUpdateRequest singleDateRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(singleDateRequest, "ratePlanId", 1L);
            ReflectionTestUtils.setField(singleDateRequest, "startDate", LocalDate.of(2024, 1, 15));
            ReflectionTestUtils.setField(singleDateRequest, "endDate", LocalDate.of(2024, 1, 15));
            ReflectionTestUtils.setField(singleDateRequest, "price", BigDecimal.valueOf(150000));

            List<LocalDate> dateRange = Arrays.asList(LocalDate.of(2024, 1, 15));

            given(ratePlanRepository.findById(1L)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dateRange))
                    .willReturn(Arrays.asList(baseRate));

            // when
            baseRateService.bulkUpdate(singleDateRequest);

            // then
            then(ratePlanRepository).should().findById(1L);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
            // 기존 요금이 있으므로 save는 호출되지 않음 (updatePrice만 호출)
        }

        @Test
        @DisplayName("장기간 대량 업데이트 성공")
        void bulkUpdate_Success_LongPeriod() {
            // given
            BaseRateBulkUpdateRequest longPeriodRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(longPeriodRequest, "ratePlanId", 1L);
            ReflectionTestUtils.setField(longPeriodRequest, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(longPeriodRequest, "endDate", LocalDate.of(2024, 1, 10));
            ReflectionTestUtils.setField(longPeriodRequest, "price", BigDecimal.valueOf(130000));

            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 3),
                    LocalDate.of(2024, 1, 4), LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 6),
                    LocalDate.of(2024, 1, 7), LocalDate.of(2024, 1, 8), LocalDate.of(2024, 1, 9),
                    LocalDate.of(2024, 1, 10)
            );

            given(ratePlanRepository.findById(1L)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dateRange))
                    .willReturn(Collections.emptyList());

            // when
            baseRateService.bulkUpdate(longPeriodRequest);

            // then
            then(ratePlanRepository).should().findById(1L);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
            then(baseRateRepository).should(times(10)).save(any(BaseRate.class));
        }

        @Test
        @DisplayName("혼합 상황 대량 업데이트 성공 - 일부는 기존 요금 있고 일부는 없는 경우")
        void bulkUpdate_Success_MixedScenario() {
            // given
            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2),
                    LocalDate.of(2024, 1, 3)
            );

            BaseRate existingRate1 = BaseRate.builder()
                    .ratePlan(ratePlan)
                    .date(LocalDate.of(2024, 1, 1))
                    .price(BigDecimal.valueOf(100000))
                    .build();

            BaseRate existingRate3 = BaseRate.builder()
                    .ratePlan(ratePlan)
                    .date(LocalDate.of(2024, 1, 3))
                    .price(BigDecimal.valueOf(100000))
                    .build();

            List<BaseRate> existingRates = Arrays.asList(existingRate1, existingRate3);

            given(ratePlanRepository.findById(1L)).willReturn(Optional.of(ratePlan));
            given(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dateRange))
                    .willReturn(existingRates);

            // when
            baseRateService.bulkUpdate(baseRateBulkUpdateRequest);

            // then
            then(ratePlanRepository).should().findById(1L);
            then(baseRateRepository).should().findByRatePlanAndDateIn(eq(ratePlan), eq(dateRange));
            then(baseRateRepository).should(times(1)).save(any(BaseRate.class)); // 1월 2일만 새로 생성
        }
    }
}

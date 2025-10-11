package dev.muho.hotel.service;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.PriceAdjustmentCreateRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentSearchRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentUpdateRequest;
import dev.muho.hotel.dto.response.PriceAdjustmentResponse;
import dev.muho.hotel.global.exception.PriceAdjustmentNotFoundException;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.repository.PriceAdjustmentRepository;
import dev.muho.hotel.repository.RatePlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class PriceAdjustmentServiceTest {

    @InjectMocks
    private PriceAdjustmentService priceAdjustmentService;

    @Mock
    private RatePlanRepository ratePlanRepository;
    @Mock
    private PriceAdjustmentRepository priceAdjustmentRepository;

    private RatePlan ratePlan;
    private PriceAdjustment priceAdjustment;
    private PriceAdjustmentCreateRequest priceAdjustmentCreateRequest;
    private PriceAdjustmentUpdateRequest priceAdjustmentUpdateRequest;

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
                .name("얼리버드 특가")
                .includesBreakfast(true)
                .refundable(false)
                .minNights(2)
                .maxNights(7)
                .bookingStartDate(LocalDate.of(2024, 1, 1))
                .bookingEndDate(LocalDate.of(2024, 12, 31))
                .checkInStartDate(LocalDate.of(2024, 3, 1))
                .checkInEndDate(LocalDate.of(2024, 11, 30))
                .build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        priceAdjustment = PriceAdjustment.builder()
                .ratePlan(ratePlan)
                .name("성수기 할증")
                .adjustmentType(AdjustmentType.SURCHARGE)
                .calculationType(CalculationType.PERCENTAGE)
                .amount(BigDecimal.valueOf(20.0))
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 8, 31))
                .bookingDaysBeforeArrival(null)
                .build();
        ReflectionTestUtils.setField(priceAdjustment, "id", 1L);

        priceAdjustmentCreateRequest = new PriceAdjustmentCreateRequest();
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "ratePlanId", 1L);
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "name", "얼리버드 할인");
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "adjustmentType", AdjustmentType.DISCOUNT);
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "calculationType", CalculationType.FIXED_AMOUNT);
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "amount", BigDecimal.valueOf(50000));
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "startDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "endDate", LocalDate.of(2024, 12, 31));
        ReflectionTestUtils.setField(priceAdjustmentCreateRequest, "bookingDaysBeforeArrival", 30);

        priceAdjustmentUpdateRequest = new PriceAdjustmentUpdateRequest();
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "name", "수정된 가격 조정");
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "adjustmentType", AdjustmentType.DISCOUNT);
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "calculationType", CalculationType.PERCENTAGE);
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "amount", BigDecimal.valueOf(15.0));
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "startDate", LocalDate.of(2024, 6, 1));
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "endDate", LocalDate.of(2024, 9, 30));
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "status", Status.ACTIVE);
        ReflectionTestUtils.setField(priceAdjustmentUpdateRequest, "bookingDaysBeforeArrival", 14);
    }

    @Nested
    @DisplayName("가격 조정 검색")
    class SearchPriceAdjustment {

        @Test
        @DisplayName("성공적으로 가격 조정을 검색할 수 있다")
        void searchPriceAdjustments_Success() {
            // given
            PriceAdjustmentSearchRequest request = new PriceAdjustmentSearchRequest();
            Pageable pageable = PageRequest.of(0, 10);
            List<PriceAdjustment> adjustmentList = List.of(priceAdjustment);
            Page<PriceAdjustment> adjustmentPage = new PageImpl<>(adjustmentList, pageable, 1);

            given(priceAdjustmentRepository.findAll(any(Specification.class), eq(pageable)))
                    .willReturn(adjustmentPage);

            // when
            Page<PriceAdjustmentResponse> result = priceAdjustmentService.search(request, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("성수기 할증");
            then(priceAdjustmentRepository).should().findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("가격 조정 조회")
    class FindPriceAdjustment {

        @Test
        @DisplayName("ID로 가격 조정을 성공적으로 조회할 수 있다")
        void findById_Success() {
            // given
            Long adjustmentId = 1L;
            given(priceAdjustmentRepository.findById(adjustmentId))
                    .willReturn(Optional.of(priceAdjustment));

            // when
            PriceAdjustmentResponse result = priceAdjustmentService.findById(adjustmentId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("성수기 할증");
            assertThat(result.getAdjustmentType()).isEqualTo(AdjustmentType.SURCHARGE);
            then(priceAdjustmentRepository).should().findById(adjustmentId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
        void findById_NotFound() {
            // given
            Long adjustmentId = 999L;
            given(priceAdjustmentRepository.findById(adjustmentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> priceAdjustmentService.findById(adjustmentId))
                    .isInstanceOf(PriceAdjustmentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("가격 조정 생성")
    class CreatePriceAdjustment {

        @Test
        @DisplayName("새로운 가격 조정을 성공적으로 생성할 수 있다")
        void create_Success() {
            // given
            given(ratePlanRepository.findById(1L))
                    .willReturn(Optional.of(ratePlan));

            PriceAdjustment newAdjustment = PriceAdjustment.builder()
                    .ratePlan(ratePlan)
                    .name("얼리버드 할인")
                    .adjustmentType(AdjustmentType.DISCOUNT)
                    .calculationType(CalculationType.FIXED_AMOUNT)
                    .amount(BigDecimal.valueOf(50000))
                    .startDate(LocalDate.of(2024, 1, 1))
                    .endDate(LocalDate.of(2024, 12, 31))
                    .bookingDaysBeforeArrival(30)
                    .build();
            ReflectionTestUtils.setField(newAdjustment, "id", 2L);

            given(priceAdjustmentRepository.save(any(PriceAdjustment.class)))
                    .willReturn(newAdjustment);

            // when
            PriceAdjustmentResponse result = priceAdjustmentService.create(priceAdjustmentCreateRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("얼리버드 할인");
            assertThat(result.getAdjustmentType()).isEqualTo(AdjustmentType.DISCOUNT);
            assertThat(result.getCalculationType()).isEqualTo(CalculationType.FIXED_AMOUNT);
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(50000));
            then(ratePlanRepository).should().findById(1L);
            then(priceAdjustmentRepository).should().save(any(PriceAdjustment.class));
        }

        @Test
        @DisplayName("존재하지 않는 요금제 ID로 생성 시 예외가 발생한다")
        void create_RatePlanNotFound() {
            // given
            given(ratePlanRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> priceAdjustmentService.create(priceAdjustmentCreateRequest))
                    .isInstanceOf(RatePlanNotFoundException.class);
            then(priceAdjustmentRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("가격 조정 수정")
    class UpdatePriceAdjustment {

        @Test
        @DisplayName("기존 가격 조정을 성공적으로 수정할 수 있다")
        void update_Success() {
            // given
            Long adjustmentId = 1L;
            given(priceAdjustmentRepository.findById(adjustmentId))
                    .willReturn(Optional.of(priceAdjustment));

            // when
            PriceAdjustmentResponse result = priceAdjustmentService.update(adjustmentId, priceAdjustmentUpdateRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("수정된 가격 조정");
            assertThat(result.getAdjustmentType()).isEqualTo(AdjustmentType.DISCOUNT);
            assertThat(result.getCalculationType()).isEqualTo(CalculationType.PERCENTAGE);
            assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(15.0));
            then(priceAdjustmentRepository).should().findById(adjustmentId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 수정 시 예외가 발생한다")
        void update_NotFound() {
            // given
            Long adjustmentId = 999L;
            given(priceAdjustmentRepository.findById(adjustmentId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> priceAdjustmentService.update(adjustmentId, priceAdjustmentUpdateRequest))
                    .isInstanceOf(PriceAdjustmentNotFoundException.class);
        }
    }
}

package dev.muho.hotel.service;

import dev.muho.hotel.domain.*;
import dev.muho.hotel.dto.request.AvailabilityRequest;
import dev.muho.hotel.dto.response.AvailabilityResponse;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.repository.*;
import dev.muho.hotel.util.TestDateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceTest {

    @InjectMocks // 테스트 대상 클래스. @Mock으로 생성된 객체들이 여기에 주입됩니다.
    private AvailabilityService availabilityService;

    // --- 의존하는 Repository들을 모두 @Mock으로 선언 ---
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private RoomInventoryRepository roomInventoryRepository;
    @Mock
    private BaseRateRepository baseRateRepository;
    @Mock
    private PriceAdjustmentRepository priceAdjustmentRepository;

    @Test
    @DisplayName("예약 가능 조회 성공: 조건에 맞는 객실 1개를 반환한다")
    void checkAvailability_Success_ReturnsAvailableRoom() {
        // given (테스트 데이터 및 Mock 객체 동작 정의)
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        RatePlan ratePlan = RatePlan.builder().name("기본 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고 설정 (2일 모두 5개씩 재고 있음)
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);

        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        List<RoomInventory> inventories = List.of(inventory1, inventory2);

        // 요금 설정 (1박에 100,000원)
        List<BaseRate> baseRates = List.of(
                BaseRate.builder().date(dates.get(0)).price(new BigDecimal("100000")).build(),
                BaseRate.builder().date(dates.get(1)).price(new BigDecimal("100000")).build()
        );

        // Repository Mocking
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(inventories);
        when(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dates)).thenReturn(baseRates);
        when(priceAdjustmentRepository.findActiveAdjustmentsForPlanInDateRange(any(), any(), any())).thenReturn(Collections.emptyList());

        // when (실제 테스트할 메서드 호출)
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then (결과 검증)
        assertThat(response.getAvailableRoomTypes()).hasSize(1);
        var resultRoom = response.getAvailableRoomTypes().get(0);
        assertThat(resultRoom.getRemainingRooms()).isEqualTo(5);
        assertThat(resultRoom.getAvailableRatePlans().get(0).getTotalPrice()).isEqualTo(new BigDecimal("200000"));
    }

    @Test
    @DisplayName("예약 가능 조회 실패: 기간 중 하루 재고가 없어 빈 리스트를 반환한다")
    void checkAvailability_Fail_WhenNoInventory() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        RatePlan ratePlan = RatePlan.builder().name("기본 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고 설정 (하루는 5개, 하루는 0개)
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);

        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 10);

        List<RoomInventory> inventories = List.of(inventory1, inventory2);

        // Repository Mocking
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(inventories);

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("호텔을 찾을 수 없는 경우 IllegalArgumentException을 발생시킨다")
    void checkAvailability_ThrowsException_WhenHotelNotFound() {
        // given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        assertThatThrownBy(() -> availabilityService.checkAvailability(999L, request))
                .isInstanceOf(HotelNotFoundException.class)
                .hasMessage("존재하지 않는 호텔입니다.");
    }

    @Test
    @DisplayName("객실 수용 인원 초과 시 빈 리스트를 반환한다")
    void checkAvailability_ReturnsEmpty_WhenExceedsRoomCapacity() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 최대 수용 인원이 2명인 객실
        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(2).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));

        // when (성인 2명 + 아동 2명 = 총 4명, 객실 수용 인원 2명 초과)
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 2);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("요금제가 판매 중단된 경우 빈 리스트를 반환한다")
    void checkAvailability_ReturnsEmpty_WhenRatePlanNotOnSale() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 판매 중단된 요금제
        RatePlan ratePlan = RatePlan.builder().name("중단된 플랜").minNights(1).build();
        ratePlan.updateStatus(Status.INACTIVE);
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고는 충분
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);
        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory1, inventory2));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("최소 숙박일 조건 미달 시 빈 리스트를 반환한다")
    void checkAvailability_ReturnsEmpty_WhenBelowMinNights() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(31); // 1박 (최소 3박 필요한 요금제)
        List<LocalDate> dates = List.of(checkIn);

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 최소 3박 이상 필요한 요금제
        RatePlan ratePlan = RatePlan.builder().name("장기 플랜").minNights(3).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고는 충분
        RoomInventory inventory = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory, "reservedQuantity", 5);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("최대 숙박일 조건 초과 시 빈 리스트를 반환한다")
    void checkAvailability_ReturnsEmpty_WhenExceedsMaxNights() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(37); // 7박 (최대 5박까지 허용하는 요금제)
        List<LocalDate> dates = checkIn.datesUntil(checkOut).toList();

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 최대 5박까지만 허용하는 요금제
        RatePlan ratePlan = RatePlan.builder().name("단기 플랜").minNights(1).maxNights(5).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고는 모든 날짜에 충분
        List<RoomInventory> inventories = dates.stream()
                .map(date -> {
                    RoomInventory inventory = RoomInventory.builder().date(date).totalQuantity(10).build();
                    ReflectionTestUtils.setField(inventory, "reservedQuantity", 5);
                    return inventory;
                })
                .toList();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(inventories);

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("할인 적용 시 정확한 총 요금을 계산한다")
    void checkAvailability_CalculatesCorrectPriceWithDiscount() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        RatePlan ratePlan = RatePlan.builder().name("할인 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고 설정
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);
        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        // 기본 요금 설정 (1박에 100,000원)
        List<BaseRate> baseRates = List.of(
                BaseRate.builder().date(dates.get(0)).price(new BigDecimal("100000")).build(),
                BaseRate.builder().date(dates.get(1)).price(new BigDecimal("100000")).build()
        );

        // 10% 할인 설정
        PriceAdjustment discount = PriceAdjustment.builder()
                .ratePlan(ratePlan)
                .startDate(checkIn)
                .endDate(checkOut)
                .amount(new BigDecimal("10"))
                .calculationType(PriceAdjustment.CalculationType.PERCENTAGE)
                .adjustmentType(PriceAdjustment.AdjustmentType.DISCOUNT)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory1, inventory2));
        when(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dates)).thenReturn(baseRates);
        when(priceAdjustmentRepository.findActiveAdjustmentsForPlanInDateRange(any(), any(), any())).thenReturn(List.of(discount));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).hasSize(1);
        var resultRoom = response.getAvailableRoomTypes().get(0);
        // 원래 200,000원에서 10% 할인 = 180,000원
        BigDecimal expectedPrice = new BigDecimal("180000");
        BigDecimal actualPrice = resultRoom.getAvailableRatePlans().get(0).getTotalPrice();
        assertThat(actualPrice.compareTo(expectedPrice)).isEqualTo(0);
    }

    @Test
    @DisplayName("할증 적용 시 정확한 총 요금을 계산한다")
    void checkAvailability_CalculatesCorrectPriceWithSurcharge() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        RatePlan ratePlan = RatePlan.builder().name("할증 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고 설정
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);
        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        // 기본 요금 설정 (1박에 100,000원)
        List<BaseRate> baseRates = List.of(
                BaseRate.builder().date(dates.get(0)).price(new BigDecimal("100000")).build(),
                BaseRate.builder().date(dates.get(1)).price(new BigDecimal("100000")).build()
        );

        // 고정 금액 20,000원 할증 설정
        PriceAdjustment surcharge = PriceAdjustment.builder()
                .ratePlan(ratePlan)
                .startDate(checkIn)
                .endDate(checkOut)
                .amount(new BigDecimal("20000"))
                .calculationType(PriceAdjustment.CalculationType.FIXED_AMOUNT)
                .adjustmentType(PriceAdjustment.AdjustmentType.SURCHARGE)
                .build();

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory1, inventory2));
        when(baseRateRepository.findByRatePlanAndDateIn(ratePlan, dates)).thenReturn(baseRates);
        when(priceAdjustmentRepository.findActiveAdjustmentsForPlanInDateRange(any(), any(), any())).thenReturn(List.of(surcharge));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).hasSize(1);
        var resultRoom = response.getAvailableRoomTypes().get(0);
        // 원래 200,000원에서 각 날짜별로 20,000원씩 할증 = 240,000원
        BigDecimal expectedPrice = new BigDecimal("240000");
        BigDecimal actualPrice = resultRoom.getAvailableRatePlans().get(0).getTotalPrice();
        assertThat(actualPrice.compareTo(expectedPrice)).isEqualTo(0);
    }

    @Test
    @DisplayName("여러 객실 타입이 있을 때 조건에 맞는 것들만 반환한다")
    void checkAvailability_ReturnsMultipleRoomTypes_WhenAvailable() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 두 개의 요금제
        RatePlan ratePlan1 = RatePlan.builder().name("기본 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan1, "id", 1L);
        RatePlan ratePlan2 = RatePlan.builder().name("프리미엄 플랜").minNights(1).build();
        ReflectionTestUtils.setField(ratePlan2, "id", 2L);

        // 두 개의 객실 타입
        RoomType roomType1 = RoomType.builder().hotel(hotel).name("스탠다드").maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType1, "id", 1L);
        ReflectionTestUtils.setField(roomType1, "ratePlans", List.of(ratePlan1));

        RoomType roomType2 = RoomType.builder().hotel(hotel).name("디럭스").maxCapacity(6).build();
        ReflectionTestUtils.setField(roomType2, "id", 2L);
        ReflectionTestUtils.setField(roomType2, "ratePlans", List.of(ratePlan2));

        // 첫 번째 객실 타입 재고
        RoomInventory inventory1_1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1_1, "reservedQuantity", 5);
        RoomInventory inventory1_2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1_2, "reservedQuantity", 5);

        // 두 번째 객실 타입 재고
        RoomInventory inventory2_1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(5).build();
        ReflectionTestUtils.setField(inventory2_1, "reservedQuantity", 2);
        RoomInventory inventory2_2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(5).build();
        ReflectionTestUtils.setField(inventory2_2, "reservedQuantity", 2);

        // 기본 요금 설정
        List<BaseRate> baseRates1 = List.of(
                BaseRate.builder().date(dates.get(0)).price(new BigDecimal("100000")).build(),
                BaseRate.builder().date(dates.get(1)).price(new BigDecimal("100000")).build()
        );
        List<BaseRate> baseRates2 = List.of(
                BaseRate.builder().date(dates.get(0)).price(new BigDecimal("150000")).build(),
                BaseRate.builder().date(dates.get(1)).price(new BigDecimal("150000")).build()
        );

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType1, roomType2));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType1, dates)).thenReturn(List.of(inventory1_1, inventory1_2));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType2, dates)).thenReturn(List.of(inventory2_1, inventory2_2));
        when(baseRateRepository.findByRatePlanAndDateIn(ratePlan1, dates)).thenReturn(baseRates1);
        when(baseRateRepository.findByRatePlanAndDateIn(ratePlan2, dates)).thenReturn(baseRates2);
        when(priceAdjustmentRepository.findActiveAdjustmentsForPlanInDateRange(any(), any(), any())).thenReturn(Collections.emptyList());

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).hasSize(2);

        var standardRoom = response.getAvailableRoomTypes().stream()
                .filter(room -> room.getRoomTypeName().equals("스탠다드"))
                .findFirst().orElseThrow();
        assertThat(standardRoom.getRemainingRooms()).isEqualTo(5);
        assertThat(standardRoom.getAvailableRatePlans().get(0).getTotalPrice()).isEqualTo(new BigDecimal("200000"));

        var deluxeRoom = response.getAvailableRoomTypes().stream()
                .filter(room -> room.getRoomTypeName().equals("디럭스"))
                .findFirst().orElseThrow();
        assertThat(deluxeRoom.getRemainingRooms()).isEqualTo(3);
        assertThat(deluxeRoom.getAvailableRatePlans().get(0).getTotalPrice()).isEqualTo(new BigDecimal("300000"));
    }

    @Test
    @DisplayName("예약 기간 조건에 맞지 않는 요금제는 제외된다")
    void checkAvailability_ExcludesRatePlan_WhenOutsideBookingWindow() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 예약 기간이 지난 요금제 (예약 마감일이 과거)
        RatePlan ratePlan = RatePlan.builder()
                .name("마감된 플랜")
                
                .minNights(1)
                .bookingStartDate(TestDateUtils.getFutureLocalDatePlusDays(-60))
                .bookingEndDate(TestDateUtils.getFutureLocalDatePlusDays(-30)) // 예약 마감일이 과거
                .build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고는 충분
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);
        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory1, inventory2));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("숙박 기간 조건에 맞지 않는 요금제는 제외된다")
    void checkAvailability_ExcludesRatePlan_WhenOutsideStayWindow() {
        // given
        LocalDate checkIn = TestDateUtils.getFutureLocalDatePlusDays(30);
        LocalDate checkOut = TestDateUtils.getFutureLocalDatePlusDays(32);
        List<LocalDate> dates = List.of(checkIn, checkIn.plusDays(1));

        Hotel hotel = Hotel.builder().name("테스트 호텔").build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        // 숙박 가능 기간이 지난 요금제 (체크인 허용 마감일이 과거)
        RatePlan ratePlan = RatePlan.builder()
                .name("기간 만료 플랜")
                
                .minNights(1)
                .checkInStartDate(TestDateUtils.getFutureLocalDatePlusDays(-60))
                .checkInEndDate(TestDateUtils.getFutureLocalDatePlusDays(-30)) // 체크인 허용 마감일이 과거
                .build();
        ReflectionTestUtils.setField(ratePlan, "id", 1L);

        RoomType roomType = RoomType.builder().hotel(hotel).maxCapacity(4).build();
        ReflectionTestUtils.setField(roomType, "id", 1L);
        ReflectionTestUtils.setField(roomType, "ratePlans", List.of(ratePlan));

        // 재고는 충분
        RoomInventory inventory1 = RoomInventory.builder().date(dates.get(0)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory1, "reservedQuantity", 5);
        RoomInventory inventory2 = RoomInventory.builder().date(dates.get(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(inventory2, "reservedQuantity", 5);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel(hotel)).thenReturn(List.of(roomType));
        when(roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates)).thenReturn(List.of(inventory1, inventory2));

        // when
        AvailabilityRequest request = new AvailabilityRequest(checkIn, checkOut, 2, 0);
        AvailabilityResponse response = availabilityService.checkAvailability(1L, request);

        // then
        assertThat(response.getAvailableRoomTypes()).isEmpty();
    }
}

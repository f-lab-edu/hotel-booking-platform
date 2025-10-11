package dev.muho.hotel.util;

import dev.muho.hotel.domain.*;
import dev.muho.hotel.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TestDataSetupService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final BaseRateRepository baseRateRepository;
    private final PriceAdjustmentRepository priceAdjustmentRepository;

    public TestDataSetupService(
        HotelRepository hotelRepository,
        RoomTypeRepository roomTypeRepository,
        RatePlanRepository ratePlanRepository,
        RoomInventoryRepository roomInventoryRepository,
        BaseRateRepository baseRateRepository,
        PriceAdjustmentRepository priceAdjustmentRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.roomInventoryRepository = roomInventoryRepository;
        this.baseRateRepository = baseRateRepository;
        this.priceAdjustmentRepository = priceAdjustmentRepository;
    }

    @Transactional
    public Hotel setupHotel() {
        Hotel hotel = hotelRepository.save(Hotel.builder().name("테스트 호텔").address("서울").rating(5).build());
        RoomType roomType = roomTypeRepository.save(RoomType.builder().hotel(hotel).name("스탠다드 더블").maxCapacity(4).standardCapacity(2).build());
        RatePlan ratePlan = ratePlanRepository.save(RatePlan.builder().roomType(roomType).name("기본 플랜").minNights(1).build());

        // 동적 날짜로 변경 - 30일 후부터 데이터 설정
        LocalDate startDate = TestDateUtils.getFutureLocalDatePlusDays(30);

        RoomInventory roomInventory1 = RoomInventory.builder().roomType(roomType).date(startDate).totalQuantity(10).build();
        ReflectionTestUtils.setField(roomInventory1, "reservedQuantity", 5);
        RoomInventory roomInventory2 = RoomInventory.builder().roomType(roomType).date(startDate.plusDays(1)).totalQuantity(10).build();
        ReflectionTestUtils.setField(roomInventory2, "reservedQuantity", 5);

        roomInventoryRepository.saveAll(List.of(
                roomInventory1, roomInventory2
        ));
        baseRateRepository.saveAll(List.of(
                BaseRate.builder().ratePlan(ratePlan).date(startDate).price(new BigDecimal("150000")).build(),
                BaseRate.builder().ratePlan(ratePlan).date(startDate.plusDays(1)).price(new BigDecimal("150000")).build()
        ));

        return hotelRepository.findById(hotel.getId()).get();
    }

    @Transactional
    public RoomType setupRoomType() {
        Hotel hotel = hotelRepository.save(Hotel.builder().name("테스트 호텔").address("서울").rating(5).build());
        return roomTypeRepository.save(RoomType.builder().hotel(hotel).name("스탠다드 더블").maxCapacity(4).standardCapacity(2).build());
    }

    @Transactional
    public RatePlan setupRatePlan(RoomType roomType) {
        return ratePlanRepository.save(
            RatePlan.builder()
                .roomType(roomType)
                .name("기본 요금제")
                .includesBreakfast(false)
                .refundable(true)
                .minNights(1)
                .maxNights(7)
                .bookingStartDate(LocalDate.of(2024, 1, 1))
                .bookingEndDate(LocalDate.of(2024, 12, 31))
                .checkInStartDate(LocalDate.of(2024, 1, 1))
                .checkInEndDate(LocalDate.of(2024, 12, 31))
                .build()
        );
    }

    @Transactional
    public RoomInventory setupRoomInventory(RoomType roomType) {
        // 현재 날짜 기준으로 테스트 재고 생성
        LocalDate testDate = LocalDate.now().plusDays(1); // 내일 날짜로 설정

        RoomInventory roomInventory = RoomInventory.builder()
                .roomType(roomType)
                .date(testDate)
                .totalQuantity(15) // 총 재고 15개
                .build();

        // 예약된 수량을 5개로 설정
        ReflectionTestUtils.setField(roomInventory, "reservedQuantity", 5);

        return roomInventoryRepository.save(roomInventory);
    }

    @Transactional
    public RoomType setupSecondRoomType() {
        // 기존 호텔을 찾거나 새로 생성
        Hotel hotel = hotelRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> hotelRepository.save(
                        Hotel.builder()
                                .name("테스트 호텔")
                                .address("서울")
                                .rating(5)
                                .build()));

        return roomTypeRepository.save(
                RoomType.builder()
                        .hotel(hotel)
                        .name("디럭스 트윈")
                        .standardCapacity(2)
                        .maxCapacity(3)
                        .build());
    }

    @Transactional
    public List<Hotel> setupHotels() {
        Hotel hotel1 = hotelRepository.save(Hotel.builder().name("테스트 호텔1").address("서울").rating(5).build());
        Hotel hotel2 = hotelRepository.save(Hotel.builder().name("테스트 호텔2").address("부산").rating(4).build());
        Hotel hotel3 = hotelRepository.save(Hotel.builder().name("테스트 호텔3").address("제주").rating(3).build());
        return List.of(hotel1, hotel2, hotel3);
    }

    @Transactional
    public PriceAdjustment setupPriceAdjustment(RatePlan ratePlan) {
        return priceAdjustmentRepository.save(
            PriceAdjustment.builder()
                .ratePlan(ratePlan)
                .name("성수기 할증")
                .adjustmentType(AdjustmentType.SURCHARGE)
                .calculationType(CalculationType.PERCENTAGE)
                .amount(BigDecimal.valueOf(20.0))
                .startDate(LocalDate.of(2024, 7, 1))
                .endDate(LocalDate.of(2024, 8, 31))
                .bookingDaysBeforeArrival(null)
                .build()
        );
    }
}

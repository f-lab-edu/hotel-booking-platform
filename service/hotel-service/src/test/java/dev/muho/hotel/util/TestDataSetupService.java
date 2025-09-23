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

    public TestDataSetupService(
        HotelRepository hotelRepository,
        RoomTypeRepository roomTypeRepository,
        RatePlanRepository ratePlanRepository,
        RoomInventoryRepository roomInventoryRepository,
        BaseRateRepository baseRateRepository
    ) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.ratePlanRepository = ratePlanRepository;
        this.roomInventoryRepository = roomInventoryRepository;
        this.baseRateRepository = baseRateRepository;
    }

    @Transactional
    public Hotel setupHotel() {
        Hotel hotel = hotelRepository.save(Hotel.builder().name("테스트 호텔").address("서울").rating(5).build());
        RoomType roomType = roomTypeRepository.save(RoomType.builder().hotel(hotel).name("스탠다드 더블").maxCapacity(4).standardCapacity(2).build());
        RatePlan ratePlan = ratePlanRepository.save(RatePlan.builder().roomType(roomType).name("기본 플랜").onSale(true).minNights(1).build());

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

        return hotel;
    }

    @Transactional
    public List<Hotel> setupHotels() {
        Hotel hotel1 = hotelRepository.save(Hotel.builder().name("테스트 호텔1").address("서울").rating(5).build());
        Hotel hotel2 = hotelRepository.save(Hotel.builder().name("테스트 호텔2").address("부산").rating(4).build());
        Hotel hotel3 = hotelRepository.save(Hotel.builder().name("테스트 호텔3").address("제주").rating(3).build());
        return List.of(hotel1, hotel2, hotel3);
    }
}

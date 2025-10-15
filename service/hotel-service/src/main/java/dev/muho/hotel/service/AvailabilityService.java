package dev.muho.hotel.service;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomInventory;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.AvailabilityRequest;
import dev.muho.hotel.dto.response.AvailabilityResponse;
import dev.muho.hotel.dto.response.AvailableRatePlanDto;
import dev.muho.hotel.dto.response.AvailableRoomTypeDto;
import dev.muho.hotel.global.exception.BaseRateNotFoundException;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 객실 예약 가능 여부(Availability) 조회를 처리하는 서비스 클래스입니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>호텔 및 객실 타입, 요금제, 재고, 가격 조정 데이터 조회</li>
 *   <li>투숙 인원수에 맞는 객실 타입 필터링</li>
 *   <li>숙박 기간 중 최소 재고량 확인</li>
 *   <li>요금제 판매 조건 검증 (판매 기간, 예약 기간, 최소/최대 숙박일)</li>
 *   <li>일별 기본 요금에 가격 조정(할인/할증) 적용하여 총 숙박비 계산</li>
 *   <li>예약 가능한 객실 타입과 요금제를 응답 DTO로 변환</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    // 필요한 모든 Repository를 주입받습니다.
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final BaseRateRepository baseRateRepository;
    private final PriceAdjustmentRepository priceAdjustmentRepository;

    /**
     * 특정 호텔의 객실 예약 가능 여부를 조회합니다.
     *
     * <p>조회 로직:</p>
     * <ol>
     *   <li>호텔 정보 조회 및 판매 여부 확인</li>
     *   <li>투숙 인원수에 맞는 객실 타입 필터링</li>
     *   <li>각 객실 타입별 재고 확인 (숙박 기간 중 최소 재고량)</li>
     *   <li>요금제 판매 조건 확인 (판매 기간, 예약 기간, 최소/최대 숙박일)</li>
     *   <li>일별 기본 요금 + 가격 조정(할인/할증) 적용하여 총 숙박비 계산</li>
     *   <li>예약 가능한 객실 타입과 요금제를 응답 DTO로 변환</li>
     * </ol>
     *
     * @param hotelId 조회할 호텔 ID
     * @param request 조회 조건 (체크인/체크아웃 날짜, 투숙 인원수 등)
     * @return 예약 가능한 객실 타입과 요금제를 포함한 응답 DTO
     * @throws HotelNotFoundException 호텔 ID에 해당하는 호텔이 없는 경우
     */
    public AvailabilityResponse checkAvailability(Long hotelId, AvailabilityRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        // 호텔이 판매 중이지 않으면 빈 결과 반환
        if (!hotel.isOnSale()) {
            return AvailabilityResponse.builder()
                    .hotelId(hotel.getId())
                    .hotelName(hotel.getName())
                    .checkInDate(request.getCheckInDate())
                    .checkOutDate(request.getCheckOutDate())
                    .availableRoomTypes(List.of())
                    .build();
        }

        List<RoomType> roomTypes = roomTypeRepository.findByHotelIdWithRatePlans(hotelId);
        List<RatePlan> ratePlans = roomTypes.stream()
                .flatMap(rt -> rt.getRatePlans().stream())
                .toList();

        List<LocalDate> stayDates = request.getCheckInDate().datesUntil(request.getCheckOutDate()).toList();

        // 숙박 기간에 해당하는 객실 재고, 기본 요금, 가격 조정 데이터를 모두 가져온 뒤 Id로 그룹화합니다.
        Map<Long, List<RoomInventory>> inventoriesByRoomType = roomInventoryRepository
                .findByRoomTypeInAndDateIn(roomTypes, stayDates)
                .stream()
                .collect(Collectors.groupingBy(ri -> ri.getRoomType().getId()));

        Map<Long, List<BaseRate>> baseRatesByRatePlan = baseRateRepository
                .findByRatePlanInAndDateIn(ratePlans, stayDates)
                .stream()
                .collect(Collectors.groupingBy(br -> br.getRatePlan().getId()));

        Map<Long, List<PriceAdjustment>> adjustmentsByRatePlan = priceAdjustmentRepository
                .findActiveAdjustmentsByRatePlanInDateRange(
                        ratePlans,
                        stayDates.get(0),
                        stayDates.get(stayDates.size() - 1))
                .stream()
                .collect(Collectors.groupingBy(pa -> pa.getRatePlan().getId()));

        List<AvailableRoomTypeDto> availableRoomTypes = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            // 객실이 판매 중인지, 인원수 조건에 맞는지 확인
            if (!roomType.isOnSale() || !roomType.validateCapacity(request.getAdults(), request.getChildren())) {
                continue;
            }

            // 해당 객실 타입의 숙박 기간 중 최소 재고 수량 확인
            List<RoomInventory> inventories = inventoriesByRoomType.getOrDefault(roomType.getId(), List.of());
            int minAvailableRooms = getMinimumAvailableRooms(inventories, stayDates);
            if (minAvailableRooms <= 0) {
                continue;
            }

            // 이 객실 타입의 각 요금제별로 판매 조건 확인 및 총 숙박비 계산
            List<AvailableRatePlanDto> availableRatePlans = new ArrayList<>();
            for (RatePlan ratePlan : roomType.getRatePlans()) {
                // 숙박 기간이 요금제의 판매 조건에 맞는지 확인
                if (!ratePlan.isAvailableFor(request.getCheckInDate(), request.getCheckOutDate())) {
                    continue;
                }

                List<BaseRate> baseRates = baseRatesByRatePlan.getOrDefault(ratePlan.getId(), List.of());
                List<PriceAdjustment> adjustments = adjustmentsByRatePlan.getOrDefault(ratePlan.getId(), List.of());

                // 이 요금제의 총 숙박비 계산
                try {
                    BigDecimal totalPrice = calculateTotalPriceForPlan(baseRates, adjustments, stayDates);
                    var availableRatePlanDto = AvailableRatePlanDto.builder()
                            .ratePlanId(ratePlan.getId())
                            .ratePlanName(ratePlan.getName())
                            .totalPrice(totalPrice)
                            .build();
                    availableRatePlans.add(availableRatePlanDto);
                } catch (BaseRateNotFoundException e) {
                    log.warn("객실 타입 [{}(id:{})]의 요금제 [{}(id:{})]는 기본 요금이 없어 판매 불가합니다.",
                            roomType.getName(),
                            roomType.getId(),
                            ratePlan.getName(),
                            ratePlan.getId());
                }
            }

            // 이 객실 타입에 예약 가능한 요금제가 하나도 없으면 제외
            if (availableRatePlans.isEmpty()) {
                continue;
            }
            var availableRoomTypeDto = AvailableRoomTypeDto.builder()
                    .roomTypeId(roomType.getId())
                    .roomTypeName(roomType.getName())
                    .standardCapacity(roomType.getStandardCapacity())
                    .maxCapacity(roomType.getMaxCapacity())
                    .remainingRooms(minAvailableRooms)
                    .availableRatePlans(availableRatePlans)
                    .build();
            availableRoomTypes.add(availableRoomTypeDto);
        }

        return AvailabilityResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .availableRoomTypes(availableRoomTypes)
                .build();
    }

    /**
     * 일별 기본 요금에 적용 가능한 모든 가격 조정을 반영하여 최종 일일 요금을 계산합니다.
     *
     * <p>가격 조정 적용 로직:</p>
     * <ol>
     *   <li>각 가격 조정 항목이 해당 숙박일에 적용되는지 확인 (적용 기간 체크)</li>
     *   <li>얼리버드 할인의 경우 예약일 조건도 추가 확인</li>
     *   <li>적용 가능한 조정 항목의 계산 방식에 따라 할인/할증 금액 계산:
     *       <ul>
     *         <li>PERCENTAGE: 기본 요금의 퍼센트로 계산</li>
     *         <li>FIXED_AMOUNT: 고정 금액으로 계산</li>
     *       </ul>
     *   </li>
     *   <li>할인/할증 유형에 따라 최종 금액에서 차감 또는 가산</li>
     *   <li>최종 금액이 0보다 작아지지 않도록 보정</li>
     * </ol>
     *
     * @param dailyBasePrice 해당 날짜의 기본 요금
     * @param stayDate 숙박 날짜
     * @param bookingDate 예약 날짜 (오늘)
     * @param adjustments 적용 가능한 가격 조정 항목 목록
     * @return 모든 가격 조정이 반영된 최종 일일 요금 (최소 0 이상)
     */
    private BigDecimal applyAdjustments(BigDecimal dailyBasePrice, LocalDate stayDate, LocalDate bookingDate, List<PriceAdjustment> adjustments) {
        BigDecimal finalPrice = dailyBasePrice;

        for (PriceAdjustment adj : adjustments) {
            // 이 조정 항목이 해당 숙박일에 적용되는지 확인
            boolean isApplicable = !stayDate.isBefore(adj.getStartDate()) && !stayDate.isAfter(adj.getEndDate());

            // 얼리버드 같은 예약일 조건 확인
            if (isApplicable && adj.getBookingDaysBeforeArrival() != null) {
                long daysBetween = ChronoUnit.DAYS.between(bookingDate, stayDate);
                if (daysBetween < adj.getBookingDaysBeforeArrival()) {
                    isApplicable = false;
                }
            }

            if (isApplicable) {
                BigDecimal adjustmentAmount;
                if (adj.getCalculationType() == CalculationType.PERCENTAGE) {
                    // 퍼센트 할인은 기준가(dailyBasePrice)를 기준으로 계산합니다.
                    adjustmentAmount = dailyBasePrice.multiply(adj.getAmount().divide(new BigDecimal("100")));
                } else { // FIXED_AMOUNT
                    adjustmentAmount = adj.getAmount();
                }

                if (adj.getAdjustmentType() == AdjustmentType.DISCOUNT) {
                    finalPrice = finalPrice.subtract(adjustmentAmount);
                } else { // SURCHARGE
                    finalPrice = finalPrice.add(adjustmentAmount);
                }
            }
        }
        // 최종 금액이 음수가 되지 않도록 보정
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }

    /**
     * 주어진 숙박 날짜들에 대해 각 날짜의 객실 재고 중 최소 재고 수량을 반환합니다.
     *
     * <p>재고 수량이 충분하지 않은 경우(재고 데이터가 없거나, 날짜 수와 재고 데이터 수가 일치하지 않는 경우) 0을 반환합니다.</p>
     *
     * @param inventories 객실 타입과 숙박 날짜에 해당하는 재고 목록
     * @param stayDates 조회 대상 숙박 날짜 목록
     * @return 숙박 기간 중 최소 재고 수량 (0 이상)
     */
    private int getMinimumAvailableRooms(List<RoomInventory> inventories, List<LocalDate> stayDates) {
        if (inventories.size() != stayDates.size()) { return 0; }
        return inventories.stream()
                .mapToInt(RoomInventory::getAvailableQuantity)
                .min()
                .orElse(0);
    }

    /**
     * 특정 요금제에 대해 숙박 기간 동안의 총 요금을 계산합니다.
     *
     * <p>계산 로직:</p>
     * <ol>
     *   <li>숙박 날짜별로 기본 요금을 조회</li>
     *   <li>각 날짜별로 적용 가능한 가격 조정을 반영하여 최종 일일 요금 계산</li>
     *   <li>모든 숙박 날짜의 최종 일일 요금을 합산하여 총 요금 산출</li>
     * </ol>
     *
     * @param baseRates 해당 요금제의 기본 요금 목록 (숙박 날짜에 해당하는 데이터만 포함)
     * @param adjustments 해당 요금제에 적용 가능한 가격 조정 목록 (숙박 기간에 해당하는 데이터만 포함)
     * @param stayDates 숙박 날짜 목록
     * @return 숙박 기간 동안의 총 요금
     * @throws BaseRateNotFoundException 숙박 날짜 중 하나라도 기본 요금이 없는 경우
     */
    private BigDecimal calculateTotalPriceForPlan(List<BaseRate> baseRates, List<PriceAdjustment> adjustments, List<LocalDate> stayDates) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();

        for (LocalDate stayDate : stayDates) {
            BigDecimal dailyBasePrice = baseRates.stream()
                    .filter(br -> br.getDate().equals(stayDate))
                    .findFirst()
                    .orElseThrow(() -> new BaseRateNotFoundException(stayDate))
                    .getPrice();

            BigDecimal finalDailyPrice = applyAdjustments(dailyBasePrice, stayDate, today, adjustments);

            totalPrice = totalPrice.add(finalDailyPrice);
        }

        return totalPrice;
    }
}

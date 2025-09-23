package dev.muho.hotel.service;

import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 호텔 객실 예약 가능성을 조회하는 서비스 클래스입니다.
 *
 * <p>이 서비스는 다음과 같은 주요 기능을 제공합니다:</p>
 * <ul>
 *   <li>특정 호텔의 예약 가능한 객실 타입 조회</li>
 *   <li>각 객실 타입별 재고 확인</li>
 *   <li>판매 가능한 요금제 필터링</li>
 *   <li>기본 요금 + 할인/할증 적용한 최종 가격 계산</li>
 * </ul>
 *
 * <p>주요 비즈니스 로직:</p>
 * <ol>
 *   <li>호텔 정보 조회 및 유효성 검증</li>
 *   <li>투숙 인원수 기준 객실 타입 필터링</li>
 *   <li>각 객실 타입별 재고 확인 (숙박 기간 중 최소 재고량)</li>
 *   <li>요금제 판매 조건 확인 (판매 기간, 예약 기간, 최소/최대 숙박일)</li>
 *   <li>일별 기본 요금 + 가격 조정(할인/할증) 적용하여 총 숙박비 계산</li>
 * </ol>
 *
 * @author muho
 * @since 1.0
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
     * 지정된 조건에 따라 호텔의 예약 가능한 객실과 요금 정보를 조회합니다.
     *
     * <p>이 메서드는 다음과 같은 순서로 처리됩니다:</p>
     * <ol>
     *   <li>호텔 정보 조회 및 존재 여부 확인</li>
     *   <li>호텔의 모든 객실 타입 조회</li>
     *   <li>투숙 인원수 기준으로 수용 가능한 객실 타입 필터링</li>
     *   <li>각 객실 타입별 예약 가능성 및 요금 계산</li>
     *   <li>판매 가능한 요금제가 있는 객실 타입만 최종 결과에 포함</li>
     * </ol>
     *
     * @param hotelId 조회할 호텔의 ID
     * @param request 예약 가능성 조회 요청 객체
     * @return 예약 가능한 객실 타입과 요금제 정보가 포함된 응답 객체
     * @throws HotelNotFoundException 존재하지 않는 호텔 ID인 경우
     */
    public AvailabilityResponse checkAvailability(Long hotelId, AvailabilityRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        List<RoomType> roomTypes = roomTypeRepository.findByHotel(hotel);

        List<AvailableRoomTypeDto> availableRoomTypeDtos = roomTypes.stream()
                .filter(roomType -> roomType.getMaxCapacity() >= (request.getAdults() + request.getChildren()))
                .map(roomType -> toAvailableRoomTypeDto(roomType, request.getCheckInDate(), request.getCheckOutDate()))
                .filter(dto -> dto != null && !dto.getAvailableRatePlans().isEmpty()) // 판매 가능한 요금제가 하나라도 있는 경우만 필터링
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .availableRoomTypes(availableRoomTypeDtos)
                .build();
    }

    /**
     * RoomType 엔터티를 AvailableRoomTypeDto로 변환하고 예약 가능성을 확인합니다.
     *
     * <p>이 메서드는 다음과 같은 작업을 수행합니다:</p>
     * <ul>
     *   <li>숙박 기간 동안의 최소 가용 객실 수 계산</li>
     *   <li>객실 타입에 연결된 모든 요금제의 판매 가능 여부 확인</li>
     *   <li>판매 가능한 요금제별 총 숙박비 계산</li>
     * </ul>
     *
     * @param roomType 변환할 객실 타입 엔터티
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @return 예약 가능한 경우 AvailableRoomTypeDto, 불가능한 경우 null
     */
    private AvailableRoomTypeDto toAvailableRoomTypeDto(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        List<LocalDate> dates = checkInDate.datesUntil(checkOutDate).collect(Collectors.toList());

        int minAvailableRooms = getMinimumAvailableRooms(roomType, dates);
        if (minAvailableRooms <= 0) {
            return null;
        }

        List<AvailableRatePlanDto> availableRatePlans = roomType.getRatePlans().stream()
                .filter(ratePlan -> isPlanAvailable(ratePlan, checkInDate, checkOutDate)) // 요금제 판매 기간 등 조건 체크
                .map(ratePlan -> {
                    try {
                        BigDecimal totalPrice = calculateTotalPriceForPlan(ratePlan, dates);
                        return AvailableRatePlanDto.builder()
                                .ratePlanId(ratePlan.getId())
                                .ratePlanName(ratePlan.getName())
                                .totalPrice(totalPrice)
                                .build();
                    } catch (BaseRateNotFoundException e) {
                        log.warn("객실 타입 [{}(id:{})]의 요금제 [{}(id:{})]는 기본 요금이 없어 판매 불가합니다.",
                                roomType.getName(),
                                roomType.getId(),
                                ratePlan.getName(),
                                ratePlan.getId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return AvailableRoomTypeDto.builder()
                .roomTypeId(roomType.getId())
                .roomTypeName(roomType.getName())
                .standardCapacity(roomType.getStandardCapacity())
                .maxCapacity(roomType.getMaxCapacity())
                .remainingRooms(minAvailableRooms)
                .availableRatePlans(availableRatePlans) // 3. 계산된 요금제 목록을 DTO에 담는다.
                .build();
    }

    /**
     * 요금제가 주어진 예약 조건에 대해 판매 가능한지 확인합니다.
     *
     * <p>다음과 같은 조건들을 순차적으로 검증합니다:</p>
     * <ul>
     *   <li><strong>판매 상태:</strong> 요금제가 현재 판매 중인지 확인</li>
     *   <li><strong>예약 가능 기간:</strong> 오늘 날짜가 요금제의 예약 접수 기간 내인지 확인</li>
     *   <li><strong>숙박 가능 기간:</strong> 체크인 날짜가 요금제의 숙박 허용 기간 내인지 확인</li>
     *   <li><strong>최소/최대 숙박일:</strong> 숙박 일수가 요금제의 최소/최대 조건을 만족하는지 확인</li>
     * </ul>
     *
     * @param ratePlan 확인할 요금제
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @return 모든 조건을 만족하면 true, 그렇지 않으면 false
     */
    private boolean isPlanAvailable(RatePlan ratePlan, LocalDate checkInDate, LocalDate checkOutDate) {
        if (!ratePlan.isOnSale()) {
            return false;
        }

        // 예약 가능 기간(Booking Window) 확인
        // "오늘" 날짜가 요금제를 예약할 수 있는 기간에 속하는지 검사합니다.
        LocalDate today = LocalDate.now();
        if (ratePlan.getBookingStartDate() != null && today.isBefore(ratePlan.getBookingStartDate())) {
            return false;
        }
        if (ratePlan.getBookingEndDate() != null && today.isAfter(ratePlan.getBookingEndDate())) {
            return false;
        }

        // 숙박 가능 기간(Stay Window) 확인
        // 사용자가 요청한 "체크인 날짜"가 요금제가 허용하는 숙박 기간에 속하는지 검사합니다.
        if (ratePlan.getCheckInStartDate() != null && checkInDate.isBefore(ratePlan.getCheckInStartDate())) {
            return false;
        }
        if (ratePlan.getCheckInEndDate() != null && checkInDate.isAfter(ratePlan.getCheckInEndDate())) {
            return false;
        }

        // 최소/최대 숙박일(Duration) 확인
        // 사용자가 요청한 숙박 기간이 요금제의 최소/최대 숙박일 조건을 만족하는지 검사합니다.
        long duration = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (duration < ratePlan.getMinNights()) {
            return false;
        }
        if (ratePlan.getMaxNights() != null && duration > ratePlan.getMaxNights()) {
            return false;
        }

        // 모든 조건을 통과하면 판매 가능한 것으로 판단
        return true;
    }

    /**
     * 특정 요금제에 대해 숙박 기간 전체의 총 요금을 계산합니다.
     *
     * <p>요금 계산 프로세스:</p>
     * <ol>
     *   <li>숙박 기간 중 각 날짜별 기본 요금 조회</li>
     *   <li>해당 요금제에 적용 가능한 모든 가격 조정(할인/할증) 항목 조회</li>
     *   <li>각 날짜별로 기본 요금 + 가격 조정을 적용하여 최종 일일 요금 계산</li>
     *   <li>모든 일일 요금을 합산하여 총 숙박비 반환</li>
     * </ol>
     *
     * <p>가격 조정에는 다음과 같은 유형이 있습니다:</p>
     * <ul>
     *   <li>얼리버드 할인 (예약일 기준)</li>
     *   <li>시즌 할인/할증 (숙박일 기준)</li>
     *   <li>고정 금액 할인/할증</li>
     *   <li>퍼센트 할인/할증</li>
     * </ul>
     *
     * @param ratePlan 요금을 계산할 요금제
     * @param dates 숙박 기간의 날짜 목록 (체크인 ~ 체크아웃-1)
     * @return 총 숙박 요금
     * @throws BaseRateNotFoundException 특정 날짜의 기본 요금이 존재하지 않는 경우
     */
    private BigDecimal calculateTotalPriceForPlan(RatePlan ratePlan, List<LocalDate> dates) {
        // 1. DB 조회 최소화를 위해 필요한 데이터를 미리 한 번에 가져옵니다.
        final List<BaseRate> baseRates = baseRateRepository.findByRatePlanAndDateIn(ratePlan, dates);
        final List<PriceAdjustment> adjustments = priceAdjustmentRepository.findActiveAdjustmentsForPlanInDateRange(
                ratePlan, dates.get(0), dates.get(dates.size() - 1)
        );

        BigDecimal totalPrice = BigDecimal.ZERO;
        final LocalDate today = LocalDate.now();

        // 2. 하루씩 순회하며 일별 최종 요금을 계산하고 합산합니다.
        for (LocalDate date : dates) {
            // 해당 날짜의 기본 요금을 찾습니다.
            BigDecimal dailyBasePrice = baseRates.stream()
                    .filter(br -> br.getDate().equals(date))
                    .findFirst()
                    .orElseThrow(() -> new BaseRateNotFoundException(date))
                    .getPrice();

            // 기본 요금에 적용 가능한 모든 조정 항목(할인/할증)을 반영합니다.
            BigDecimal finalDailyPrice = applyAdjustments(dailyBasePrice, date, today, adjustments);

            totalPrice = totalPrice.add(finalDailyPrice);
        }
        return totalPrice;
    }

    /**
     * 숙박 기간 동안 해당 객실 타입의 최소 가용 객실 수를 계산합니다.
     *
     * <p>이 메서드는 숙박 기간의 각 날짜별로 다음을 계산합니다:</p>
     * <ul>
     *   <li>전체 객실 수 - 이미 예약된 객실 수 = 가용 객실 수</li>
     * </ul>
     *
     * <p>그 중에서 가장 적은 가용 객실 수를 반환하여,
     * 숙박 기간 전체에 걸쳐 예약 가능한 최대 객실 수를 보장합니다.</p>
     *
     * @param roomType 확인할 객실 타입
     * @param dates 확인할 날짜 목록
     * @return 숙박 기간 중 최소 가용 객실 수 (모든 날짜의 재고 데이터가 없는 경우 0)
     */
    private int getMinimumAvailableRooms(RoomType roomType, List<LocalDate> dates) {
        var inventories = roomInventoryRepository.findByRoomTypeAndDateIn(roomType, dates);
        if (inventories.size() != dates.size()) { return 0; }
        return inventories.stream()
                .mapToInt(inventory -> inventory.getTotalQuantity() - inventory.getReservedQuantity())
                .min()
                .orElse(0);
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
                if (adj.getCalculationType() == PriceAdjustment.CalculationType.PERCENTAGE) {
                    // 퍼센트 할인은 기준가(dailyBasePrice)를 기준으로 계산합니다.
                    adjustmentAmount = dailyBasePrice.multiply(adj.getAmount().divide(new BigDecimal("100")));
                } else { // FIXED_AMOUNT
                    adjustmentAmount = adj.getAmount();
                }

                if (adj.getAdjustmentType() == PriceAdjustment.AdjustmentType.DISCOUNT) {
                    finalPrice = finalPrice.subtract(adjustmentAmount);
                } else { // SURCHARGE
                    finalPrice = finalPrice.add(adjustmentAmount);
                }
            }
        }
        // 최종 금액이 음수가 되지 않도록 보정
        return finalPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalPrice;
    }
}

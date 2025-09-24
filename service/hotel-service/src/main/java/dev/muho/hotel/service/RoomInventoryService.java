package dev.muho.hotel.service;

import dev.muho.hotel.domain.RoomInventory;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.dto.response.RoomInventoryResponse;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.RoomInventoryRepository;
import dev.muho.hotel.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomInventoryService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository roomInventoryRepository;

    public List<RoomInventoryResponse> getRoomInventories(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(RoomTypeNotFoundException::new);

        // 조회하려는 전체 날짜 목록을 생성합니다.
        List<LocalDate> allDatesInRange = startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());

        // DB에서 해당 기간에 "존재하는" 재고 정보만 한 번에 조회합니다.
        List<RoomInventory> existingInventories = roomInventoryRepository.findByRoomTypeAndDateIn(roomType, allDatesInRange);

        // 빠른 조회를 위해 조회된 재고 목록을 날짜를 Key로 하는 Map으로 변환합니다.
        Map<LocalDate, RoomInventory> inventoryMap = existingInventories.stream()
                .collect(Collectors.toMap(RoomInventory::getDate, Function.identity()));

        // 전체 날짜 목록을 순회하면서, 재고가 있으면 해당 재고를, 없으면 기본값(0)으로 채워진 DTO를 생성합니다.
        return allDatesInRange.stream()
                .map(date -> {
                    // Map에 해당 날짜의 재고가 있는지 확인합니다.
                    RoomInventory inventory = inventoryMap.get(date);
                    if (inventory != null) {
                        // 재고가 있으면: 엔터티를 DTO로 변환
                        return RoomInventoryResponse.from(inventory);
                    } else {
                        // 재고가 없으면: 기본값(0)으로 DTO를 생성
                        return RoomInventoryResponse.builder()
                                .date(date)
                                .totalQuantity(0)
                                .reservedQuantity(0)
                                .availableQuantity(0)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void bulkUpdate(RoomInventoryBulkUpdateRequest request) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(RoomTypeNotFoundException::new);

        List<LocalDate> dates = request.getStartDate().datesUntil(request.getEndDate().plusDays(1))
                .collect(Collectors.toList());

        for (LocalDate date : dates) {
            RoomInventory inventory = roomInventoryRepository.findByRoomTypeAndDate(roomType, date)
                    .orElseGet(() -> RoomInventory.builder()
                            .roomType(roomType)
                            .date(date)
                            .build());

            inventory.updateTotalQuantity(request.getTotalQuantity());

            roomInventoryRepository.save(inventory);
        }
    }
}

package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RoomInventory;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.dto.response.RoomInventoryResponse;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.RoomInventoryRepository;
import dev.muho.hotel.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
public class RoomInventoryServiceTest {

    @InjectMocks
    private RoomInventoryService roomInventoryService;

    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private RoomInventoryRepository roomInventoryRepository;

    private RoomType roomType;
    private RoomInventory roomInventory;
    private RoomInventoryBulkUpdateRequest roomInventoryBulkUpdateRequest;

    @BeforeEach
    public void setUp() {
        Hotel hotel = Hotel.builder()
                .name("테스트 호텔")
                .address("서울시 강남구")
                .rating(5)
                .build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        roomType = RoomType.builder()
                .hotel(hotel)
                .name("디럭스 룸")
                .standardCapacity(2)
                .maxCapacity(4)
                .build();
        ReflectionTestUtils.setField(roomType, "id", 1L);

        roomInventory = RoomInventory.builder()
                .roomType(roomType)
                .date(LocalDate.of(2024, 1, 15))
                .totalQuantity(10)
                .build();
        ReflectionTestUtils.setField(roomInventory, "id", 1L);
        ReflectionTestUtils.setField(roomInventory, "reservedQuantity", 3);

        roomInventoryBulkUpdateRequest = new RoomInventoryBulkUpdateRequest();
        ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "roomTypeId", 1L);
        ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "startDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "endDate", LocalDate.of(2024, 1, 3));
        ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "totalQuantity", 15);
    }

    @Nested
    @DisplayName("객실 재고 조회")
    class GetRoomInventoriesTests {

        @Test
        @DisplayName("객실 재고 조회 성공 - 기존 재고가 있는 경우")
        void getRoomInventories_Success_WithExistingInventory() {
            // given
            Long roomTypeId = 1L;
            LocalDate startDate = LocalDate.of(2024, 1, 15);
            LocalDate endDate = LocalDate.of(2024, 1, 17);

            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 1, 16),
                    LocalDate.of(2024, 1, 17)
            );

            RoomInventory inventory2 = RoomInventory.builder()
                    .roomType(roomType)
                    .date(LocalDate.of(2024, 1, 16))
                    .totalQuantity(8)
                    .build();
            ReflectionTestUtils.setField(inventory2, "reservedQuantity", 2);

            List<RoomInventory> existingInventories = Arrays.asList(roomInventory, inventory2);

            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));
            given(roomInventoryRepository.findByRoomTypeAndDateIn(eq(roomType), eq(dateRange)))
                    .willReturn(existingInventories);

            // when
            List<RoomInventoryResponse> responses = roomInventoryService.getRoomInventories(roomTypeId, startDate, endDate);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);

            // 첫 번째 날짜 (기존 재고 있음)
            RoomInventoryResponse response1 = responses.get(0);
            assertThat(response1.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(response1.getTotalQuantity()).isEqualTo(10);
            assertThat(response1.getReservedQuantity()).isEqualTo(3);
            assertThat(response1.getAvailableQuantity()).isEqualTo(7);

            // 두 번째 날짜 (기존 재고 있음)
            RoomInventoryResponse response2 = responses.get(1);
            assertThat(response2.getDate()).isEqualTo(LocalDate.of(2024, 1, 16));
            assertThat(response2.getTotalQuantity()).isEqualTo(8);
            assertThat(response2.getReservedQuantity()).isEqualTo(2);
            assertThat(response2.getAvailableQuantity()).isEqualTo(6);

            // 세 번째 날짜 (기존 재고 없음 - 기본값)
            RoomInventoryResponse response3 = responses.get(2);
            assertThat(response3.getDate()).isEqualTo(LocalDate.of(2024, 1, 17));
            assertThat(response3.getTotalQuantity()).isEqualTo(0);
            assertThat(response3.getReservedQuantity()).isEqualTo(0);
            assertThat(response3.getAvailableQuantity()).isEqualTo(0);

            then(roomTypeRepository).should().findById(roomTypeId);
            then(roomInventoryRepository).should().findByRoomTypeAndDateIn(eq(roomType), eq(dateRange));
        }

        @Test
        @DisplayName("객실 재고 조회 성공 - 기존 재고가 없는 경우")
        void getRoomInventories_Success_WithNoExistingInventory() {
            // given
            Long roomTypeId = 1L;
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 2);

            List<LocalDate> dateRange = Arrays.asList(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2)
            );

            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));
            given(roomInventoryRepository.findByRoomTypeAndDateIn(eq(roomType), eq(dateRange)))
                    .willReturn(Collections.emptyList()); // 빈 리스트

            // when
            List<RoomInventoryResponse> responses = roomInventoryService.getRoomInventories(roomTypeId, startDate, endDate);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);

            responses.forEach(response -> {
                assertThat(response.getTotalQuantity()).isEqualTo(0);
                assertThat(response.getReservedQuantity()).isEqualTo(0);
                assertThat(response.getAvailableQuantity()).isEqualTo(0);
            });

            then(roomTypeRepository).should().findById(roomTypeId);
            then(roomInventoryRepository).should().findByRoomTypeAndDateIn(eq(roomType), eq(dateRange));
        }

        @Test
        @DisplayName("존재하지 않는 객실 타입으로 재고 조회 시 예외 발생")
        void getRoomInventories_RoomTypeNotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 2);

            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomInventoryService.getRoomInventories(roomTypeId, startDate, endDate))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
        }
    }

    @Nested
    @DisplayName("객실 재고 대량 업데이트")
    class BulkUpdateTests {

        @Test
        @DisplayName("객실 재고 대량 업데이트 성공 - 기존 재고 있는 경우")
        void bulkUpdate_Success_WithExistingInventory() {
            // given
            given(roomTypeRepository.findById(1L)).willReturn(Optional.of(roomType));

            // 첫 번째 날짜는 기존 재고 있음
            given(roomInventoryRepository.findByRoomTypeAndDate(roomType, LocalDate.of(2024, 1, 1)))
                    .willReturn(Optional.of(roomInventory));

            // 나머지 날짜들은 기존 재고 없음
            given(roomInventoryRepository.findByRoomTypeAndDate(roomType, LocalDate.of(2024, 1, 2)))
                    .willReturn(Optional.empty());
            given(roomInventoryRepository.findByRoomTypeAndDate(roomType, LocalDate.of(2024, 1, 3)))
                    .willReturn(Optional.empty());

            // when
            roomInventoryService.bulkUpdate(roomInventoryBulkUpdateRequest);

            // then
            then(roomTypeRepository).should().findById(1L);
            then(roomInventoryRepository).should(times(3)).findByRoomTypeAndDate(eq(roomType), any(LocalDate.class));
            then(roomInventoryRepository).should(times(3)).save(any(RoomInventory.class));
        }

        @Test
        @DisplayName("객실 재고 대량 업데이트 성공 - 기존 재고 없는 경우")
        void bulkUpdate_Success_WithNoExistingInventory() {
            // given
            given(roomTypeRepository.findById(1L)).willReturn(Optional.of(roomType));

            // 모든 날짜에 기존 재고 없음
            given(roomInventoryRepository.findByRoomTypeAndDate(eq(roomType), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            // when
            roomInventoryService.bulkUpdate(roomInventoryBulkUpdateRequest);

            // then
            then(roomTypeRepository).should().findById(1L);
            then(roomInventoryRepository).should(times(3)).findByRoomTypeAndDate(eq(roomType), any(LocalDate.class));
            then(roomInventoryRepository).should(times(3)).save(any(RoomInventory.class));
        }

        @Test
        @DisplayName("존재하지 않는 객실 타입으로 대량 업데이트 시 예외 발생")
        void bulkUpdate_RoomTypeNotFound_ThrowsException() {
            // given
            ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "roomTypeId", 999L);
            given(roomTypeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomInventoryService.bulkUpdate(roomInventoryBulkUpdateRequest))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(999L);
        }

        @Test
        @DisplayName("단일 날짜 대량 업데이트 성공")
        void bulkUpdate_Success_SingleDate() {
            // given
            ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "endDate", LocalDate.of(2024, 1, 1));

            given(roomTypeRepository.findById(1L)).willReturn(Optional.of(roomType));
            given(roomInventoryRepository.findByRoomTypeAndDate(roomType, LocalDate.of(2024, 1, 1)))
                    .willReturn(Optional.of(roomInventory));

            // when
            roomInventoryService.bulkUpdate(roomInventoryBulkUpdateRequest);

            // then
            then(roomTypeRepository).should().findById(1L);
            then(roomInventoryRepository).should().findByRoomTypeAndDate(roomType, LocalDate.of(2024, 1, 1));
            then(roomInventoryRepository).should().save(any(RoomInventory.class));
        }

        @Test
        @DisplayName("장기간 대량 업데이트 성공")
        void bulkUpdate_Success_LongPeriod() {
            // given
            ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(roomInventoryBulkUpdateRequest, "endDate", LocalDate.of(2024, 1, 10));

            given(roomTypeRepository.findById(1L)).willReturn(Optional.of(roomType));
            given(roomInventoryRepository.findByRoomTypeAndDate(eq(roomType), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            // when
            roomInventoryService.bulkUpdate(roomInventoryBulkUpdateRequest);

            // then
            then(roomTypeRepository).should().findById(1L);
            then(roomInventoryRepository).should(times(10)).findByRoomTypeAndDate(eq(roomType), any(LocalDate.class));
            then(roomInventoryRepository).should(times(10)).save(any(RoomInventory.class));
        }
    }
}

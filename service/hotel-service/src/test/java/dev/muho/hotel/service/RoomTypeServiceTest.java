package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RoomTypeCreateRequest;
import dev.muho.hotel.dto.request.RoomTypeSearchRequest;
import dev.muho.hotel.dto.request.RoomTypeStatusUpdateRequest;
import dev.muho.hotel.dto.request.RoomTypeUpdateRequest;
import dev.muho.hotel.dto.response.RoomTypeResponse;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.HotelRepository;
import dev.muho.hotel.repository.RoomTypeRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class RoomTypeServiceTest {

    @InjectMocks
    private RoomTypeService roomTypeService;

    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private HotelRepository hotelRepository;

    private Hotel hotel;
    private RoomType roomType;
    private RoomTypeCreateRequest createRequest;
    private RoomTypeUpdateRequest updateRequest;
    private RoomTypeStatusUpdateRequest statusUpdateRequest;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
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

        ReflectionTestUtils.setField(createRequest = new RoomTypeCreateRequest(), "hotelId", 1L);
        ReflectionTestUtils.setField(createRequest, "name", "새로운 룸타입");
        ReflectionTestUtils.setField(createRequest, "standardCapacity", 2);
        ReflectionTestUtils.setField(createRequest, "maxCapacity", 4);

        ReflectionTestUtils.setField(updateRequest = new RoomTypeUpdateRequest(), "name", "수정된 룸타입");
        ReflectionTestUtils.setField(updateRequest, "standardCapacity", 3);
        ReflectionTestUtils.setField(updateRequest, "maxCapacity", 6);
        ReflectionTestUtils.setField(updateRequest, "status", Status.ACTIVE);

        ReflectionTestUtils.setField(statusUpdateRequest = new RoomTypeStatusUpdateRequest(), "status", Status.UNDER_MAINTENANCE);
    }

    @Nested
    @DisplayName("룸타입 조회")
    class FindRoomTypeTests {

        @Test
        @DisplayName("호텔 ID로 룸타입 목록 조회 성공")
        void findByHotelId_Success() {
            // given
            Long hotelId = 1L;
            Pageable pageable = PageRequest.of(0, 10);

            RoomType roomType2 = RoomType.builder()
                    .hotel(hotel)
                    .name("스탠다드 룸")
                    .standardCapacity(2)
                    .maxCapacity(3)
                    .build();
            ReflectionTestUtils.setField(roomType2, "id", 2L);

            RoomTypeSearchRequest searchRequest = new RoomTypeSearchRequest(hotelId, null, null);

            List<RoomType> roomTypes = Arrays.asList(roomType, roomType2);
            Page<RoomType> roomTypePage = new PageImpl<>(roomTypes, pageable, roomTypes.size());

            given(roomTypeRepository.findByHotelId(hotelId, pageable)).willReturn(roomTypePage);

            // when
            Page<RoomTypeResponse> responses = roomTypeService.searchRoomTypes(searchRequest, pageable);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses.getContent()).hasSize(2);
            assertThat(responses.getContent().get(0).getName()).isEqualTo("디럭스 룸");
            assertThat(responses.getContent().get(1).getName()).isEqualTo("스탠다드 룸");

            then(roomTypeRepository).should().findByHotelId(hotelId, pageable);
        }

        @Test
        @DisplayName("호텔 ID, 이름으로 룸타입 목록 조회 성공")
        void findByHotelIdAndName_Success() {
            // given
            Long hotelId = 1L;
            String name = "디럭스";
            Pageable pageable = PageRequest.of(0, 10);

            RoomType roomType2 = RoomType.builder()
                    .hotel(hotel)
                    .name("스탠다드 룸")
                    .standardCapacity(2)
                    .maxCapacity(3)
                    .build();
            ReflectionTestUtils.setField(roomType2, "id", 2L);

            RoomTypeSearchRequest searchRequest = new RoomTypeSearchRequest(hotelId, name, null);

            List<RoomType> roomTypes = List.of(roomType);
            Page<RoomType> roomTypePage = new PageImpl<>(roomTypes, pageable, roomTypes.size());

            given(roomTypeRepository.findByHotelIdAndNameContainingIgnoreCase(
                    hotelId, name, pageable)).willReturn(roomTypePage);

            // when
            Page<RoomTypeResponse> responses = roomTypeService.searchRoomTypes(searchRequest, pageable);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses.getContent()).hasSize(1);
            assertThat(responses.getContent().get(0).getName()).isEqualTo("디럭스 룸");

            then(roomTypeRepository).should().findByHotelIdAndNameContainingIgnoreCase(
                    hotelId, name, pageable);
        }

        @Test
        @DisplayName("ID로 룸타입 조회 성공")
        void findById_Success() {
            // given
            Long roomTypeId = 1L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            RoomTypeResponse response = roomTypeService.findById(roomTypeId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("디럭스 룸");
            assertThat(response.getHotelId()).isEqualTo(1L);
            assertThat(response.getStandardCapacity()).isEqualTo(2);
            assertThat(response.getMaxCapacity()).isEqualTo(4);

            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 ID로 조회 시 예외 발생")
        void findById_NotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomTypeService.findById(roomTypeId))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
        }
    }

    @Nested
    @DisplayName("룸타입 생성")
    class CreateRoomTypeTests {

        @Test
        @DisplayName("룸타입 생성 성공")
        void create_Success() {
            // given
            given(hotelRepository.findById(1L)).willReturn(Optional.of(hotel));

            RoomType savedRoomType = RoomType.builder()
                    .hotel(hotel)
                    .name("새로운 룸타입")
                    .standardCapacity(2)
                    .maxCapacity(4)
                    .build();
            ReflectionTestUtils.setField(savedRoomType, "id", 2L);

            given(roomTypeRepository.save(any(RoomType.class))).willReturn(savedRoomType);

            // when
            RoomTypeResponse response = roomTypeService.create(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getName()).isEqualTo("새로운 룸타입");
            assertThat(response.getHotelId()).isEqualTo(1L);
            assertThat(response.getStandardCapacity()).isEqualTo(2);
            assertThat(response.getMaxCapacity()).isEqualTo(4);

            then(hotelRepository).should().findById(1L);
            then(roomTypeRepository).should().save(any(RoomType.class));
        }

        @Test
        @DisplayName("존재하지 않는 호텔로 룸타입 생성 시 예외 발생")
        void create_HotelNotFound_ThrowsException() {
            // given
            Long hotelId = 999L;
            ReflectionTestUtils.setField(createRequest, "hotelId", hotelId);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomTypeService.create(createRequest))
                    .isInstanceOf(HotelNotFoundException.class);

            then(hotelRepository).should().findById(hotelId);
            then(roomTypeRepository).should(never()).save(any(RoomType.class));
        }
    }

    @Nested
    @DisplayName("룸타입 수정")
    class UpdateRoomTypeTests {

        @Test
        @DisplayName("룸타입 정보 수정 성공")
        void update_Success() {
            // given
            Long roomTypeId = 1L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            RoomTypeResponse response = roomTypeService.update(roomTypeId, updateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("수정된 룸타입");
            assertThat(response.getStandardCapacity()).isEqualTo(3);
            assertThat(response.getMaxCapacity()).isEqualTo(6);

            // 엔티티 메서드 호출 검증
            assertThat(roomType.getName()).isEqualTo("수정된 룸타입");
            assertThat(roomType.getStandardCapacity()).isEqualTo(3);
            assertThat(roomType.getMaxCapacity()).isEqualTo(6);

            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 수정 시 예외 발생")
        void update_NotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomTypeService.update(roomTypeId, updateRequest))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
        }
    }

    @Nested
    @DisplayName("룸타입 삭제")
    class DeleteRoomTypeTests {

        @Test
        @DisplayName("룸타입 삭제 성공")
        void delete_Success() {
            // given
            Long roomTypeId = 1L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            roomTypeService.delete(roomTypeId);

            // then
            assertThat(roomType.getStatus()).isEqualTo(Status.INACTIVE);
            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 삭제 시 예외 발생")
        void delete_NotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomTypeService.delete(roomTypeId))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
            then(roomTypeRepository).should(never()).delete(any(RoomType.class));
        }
    }

    @Nested
    @DisplayName("룸타입 상태 변경")
    class UpdateRoomTypeStatusTests {

        @Test
        @DisplayName("룸타입 상태 변경 성공")
        void updateStatus_Success() {
            // given
            Long roomTypeId = 1L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            RoomTypeResponse response = roomTypeService.updateStatus(roomTypeId, statusUpdateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(roomType.getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);

            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 상태 변경 시 예외 발생")
        void updateStatus_NotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomTypeService.updateStatus(roomTypeId, statusUpdateRequest))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("룸타입 상태를 유지보수로 변경")
        void updateStatus_ToUnderMaintenance() {
            // given
            Long roomTypeId = 1L;
            RoomTypeStatusUpdateRequest maintenanceRequest = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(maintenanceRequest, "status", Status.UNDER_MAINTENANCE);
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            RoomTypeResponse response = roomTypeService.updateStatus(roomTypeId, maintenanceRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(roomType.getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);

            then(roomTypeRepository).should().findById(roomTypeId);
        }

        @Test
        @DisplayName("룸타입 상태를 활성으로 변경")
        void updateStatus_ToActive() {
            // given
            Long roomTypeId = 1L;
            // 먼저 룸타입을 비활성 상태로 설정
            roomType.changeStatus(Status.INACTIVE);

            RoomTypeStatusUpdateRequest activeRequest = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(activeRequest, "status", Status.ACTIVE);
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.of(roomType));

            // when
            RoomTypeResponse response = roomTypeService.updateStatus(roomTypeId, activeRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(roomType.getStatus()).isEqualTo(Status.ACTIVE);

            then(roomTypeRepository).should().findById(roomTypeId);
        }
    }
}

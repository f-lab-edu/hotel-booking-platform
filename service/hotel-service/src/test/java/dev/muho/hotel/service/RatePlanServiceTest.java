package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RatePlanCreateRequest;
import dev.muho.hotel.dto.request.RatePlanSearchRequest;
import dev.muho.hotel.dto.request.RatePlanStatusUpdateRequest;
import dev.muho.hotel.dto.request.RatePlanUpdateRequest;
import dev.muho.hotel.dto.response.RatePlanResponse;
import dev.muho.hotel.global.exception.RatePlanNotFoundException;
import dev.muho.hotel.global.exception.RoomTypeNotFoundException;
import dev.muho.hotel.repository.RatePlanRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class RatePlanServiceTest {

    @InjectMocks
    private RatePlanService ratePlanService;

    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private RatePlanRepository ratePlanRepository;

    private Hotel hotel;
    private RoomType roomType;
    private RatePlan ratePlan;
    private RatePlanCreateRequest ratePlanCreateRequest;
    private RatePlanUpdateRequest ratePlanUpdateRequest;
    private RatePlanStatusUpdateRequest ratePlanStatusUpdateRequest;

    @BeforeEach
    public void setUp() {
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

        ReflectionTestUtils.setField(ratePlanCreateRequest = new RatePlanCreateRequest(), "roomTypeId", 1L);
        ReflectionTestUtils.setField(ratePlanCreateRequest, "name", "새로운 요금제");
        ReflectionTestUtils.setField(ratePlanCreateRequest, "includesBreakfast", true);
        ReflectionTestUtils.setField(ratePlanCreateRequest, "refundable", true);
        ReflectionTestUtils.setField(ratePlanCreateRequest, "minNights", 1);
        ReflectionTestUtils.setField(ratePlanCreateRequest, "maxNights", 5);
        ReflectionTestUtils.setField(ratePlanCreateRequest, "bookingStartDate", LocalDate.of(2024, 1, 1));
        ReflectionTestUtils.setField(ratePlanCreateRequest, "bookingEndDate", LocalDate.of(2024, 12, 31));
        ReflectionTestUtils.setField(ratePlanCreateRequest, "checkInStartDate", LocalDate.of(2024, 2, 1));
        ReflectionTestUtils.setField(ratePlanCreateRequest, "checkInEndDate", LocalDate.of(2024, 11, 30));

        ReflectionTestUtils.setField(ratePlanUpdateRequest = new RatePlanUpdateRequest(), "name", "수정된 요금제");
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "includesBreakfast", false);
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "refundable", true);
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "minNights", 2);
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "maxNights", 10);
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "status", Status.ACTIVE);
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "bookingStartDate", LocalDate.of(2024, 2, 1));
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "bookingEndDate", LocalDate.of(2024, 11, 30));
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "checkInStartDate", LocalDate.of(2024, 3, 1));
        ReflectionTestUtils.setField(ratePlanUpdateRequest, "checkInEndDate", LocalDate.of(2024, 10, 31));

        ReflectionTestUtils.setField(ratePlanStatusUpdateRequest = new RatePlanStatusUpdateRequest(), "status", Status.UNDER_MAINTENANCE);
    }

    @Nested
    @DisplayName("요금제 조회")
    class FindRatePlanTests {

        @Test
        @DisplayName("요금제 검색 성공")
        void searchRatePlans_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            RatePlanSearchRequest searchRequest = new RatePlanSearchRequest();
            ReflectionTestUtils.setField(searchRequest, "roomTypeId", 1L);

            RatePlan ratePlan2 = RatePlan.builder()
                    .roomType(roomType)
                    .name("주말 특가")
                    .includesBreakfast(false)
                    .refundable(true)
                    .minNights(1)
                    .maxNights(3)
                    .build();
            ReflectionTestUtils.setField(ratePlan2, "id", 2L);

            List<RatePlan> ratePlans = Arrays.asList(ratePlan, ratePlan2);
            Page<RatePlan> ratePlanPage = new PageImpl<>(ratePlans, pageable, ratePlans.size());

            given(ratePlanRepository.findAll(any(Specification.class), eq(pageable))).willReturn(ratePlanPage);

            // when
            Page<RatePlanResponse> responses = ratePlanService.searchRatePlans(searchRequest, pageable);

            // then
            assertThat(responses).isNotNull();
            assertThat(responses.getContent()).hasSize(2);
            assertThat(responses.getContent().get(0).getName()).isEqualTo("얼리버드 특가");
            assertThat(responses.getContent().get(1).getName()).isEqualTo("주말 특가");

            then(ratePlanRepository).should().findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("ID로 요금제 조회 성공")
        void findById_Success() {
            // given
            Long ratePlanId = 1L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            RatePlanResponse response = ratePlanService.findById(ratePlanId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("얼리버드 특가");
            assertThat(response.getRoomTypeId()).isEqualTo(1L);
            assertThat(response.isIncludesBreakfast()).isTrue();
            assertThat(response.isRefundable()).isFalse();
            assertThat(response.getMinNights()).isEqualTo(2);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 ID로 조회 시 예외 발생")
        void findById_NotFound_ThrowsException() {
            // given
            Long ratePlanId = 999L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratePlanService.findById(ratePlanId))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(ratePlanId);
        }
    }

    @Nested
    @DisplayName("요금제 생성")
    class CreateRatePlanTests {

        @Test
        @DisplayName("요금제 생성 성공")
        void create_Success() {
            // given
            given(roomTypeRepository.findById(1L)).willReturn(Optional.of(roomType));

            RatePlan savedRatePlan = RatePlan.builder()
                    .roomType(roomType)
                    .name("새로운 요금제")
                    .includesBreakfast(true)
                    .refundable(true)
                    .minNights(1)
                    .maxNights(5)
                    .bookingStartDate(LocalDate.of(2024, 1, 1))
                    .bookingEndDate(LocalDate.of(2024, 12, 31))
                    .checkInStartDate(LocalDate.of(2024, 2, 1))
                    .checkInEndDate(LocalDate.of(2024, 11, 30))
                    .build();
            ReflectionTestUtils.setField(savedRatePlan, "id", 2L);

            given(ratePlanRepository.save(any(RatePlan.class))).willReturn(savedRatePlan);

            // when
            RatePlanResponse response = ratePlanService.create(ratePlanCreateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(2L);
            assertThat(response.getName()).isEqualTo("새로운 요금제");
            assertThat(response.getRoomTypeId()).isEqualTo(1L);
            assertThat(response.isIncludesBreakfast()).isTrue();
            assertThat(response.isRefundable()).isTrue();
            assertThat(response.getMinNights()).isEqualTo(1);

            then(roomTypeRepository).should().findById(1L);
            then(ratePlanRepository).should().save(any(RatePlan.class));
        }

        @Test
        @DisplayName("존재하지 않는 룸타입으로 요금제 생성 시 예외 발생")
        void create_RoomTypeNotFound_ThrowsException() {
            // given
            Long roomTypeId = 999L;
            ReflectionTestUtils.setField(ratePlanCreateRequest, "roomTypeId", roomTypeId);
            given(roomTypeRepository.findById(roomTypeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratePlanService.create(ratePlanCreateRequest))
                    .isInstanceOf(RoomTypeNotFoundException.class);

            then(roomTypeRepository).should().findById(roomTypeId);
            then(ratePlanRepository).should(never()).save(any(RatePlan.class));
        }
    }

    @Nested
    @DisplayName("요금제 수정")
    class UpdateRatePlanTests {

        @Test
        @DisplayName("요금제 정보 수정 성공")
        void update_Success() {
            // given
            Long ratePlanId = 1L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            RatePlanResponse response = ratePlanService.update(ratePlanId, ratePlanUpdateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("수정된 요금제");
            assertThat(response.isIncludesBreakfast()).isFalse();
            assertThat(response.isRefundable()).isTrue();
            assertThat(response.getMinNights()).isEqualTo(2);

            // 엔티티 메서드 호출 검증
            assertThat(ratePlan.getName()).isEqualTo("수정된 요금제");
            assertThat(ratePlan.isIncludesBreakfast()).isFalse();
            assertThat(ratePlan.isRefundable()).isTrue();
            assertThat(ratePlan.getMinNights()).isEqualTo(2);
            assertThat(ratePlan.getMaxNights()).isEqualTo(10);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 수정 시 예외 발생")
        void update_NotFound_ThrowsException() {
            // given
            Long ratePlanId = 999L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratePlanService.update(ratePlanId, ratePlanUpdateRequest))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(ratePlanId);
        }
    }

    @Nested
    @DisplayName("요금제 삭제")
    class DeleteRatePlanTests {

        @Test
        @DisplayName("요금제 삭제 성공")
        void delete_Success() {
            // given
            Long ratePlanId = 1L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            ratePlanService.delete(ratePlanId);

            // then
            assertThat(ratePlan.getStatus()).isEqualTo(Status.INACTIVE);
            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 삭제 시 예외 발생")
        void delete_NotFound_ThrowsException() {
            // given
            Long ratePlanId = 999L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratePlanService.delete(ratePlanId))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(ratePlanId);
        }
    }

    @Nested
    @DisplayName("요금제 상태 변경")
    class UpdateRatePlanStatusTests {

        @Test
        @DisplayName("요금제 상태 변경 성공")
        void updateStatus_Success() {
            // given
            Long ratePlanId = 1L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            RatePlanResponse response = ratePlanService.updateStatus(ratePlanId, ratePlanStatusUpdateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(ratePlan.getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 상태 변경 시 예외 발생")
        void updateStatus_NotFound_ThrowsException() {
            // given
            Long ratePlanId = 999L;
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratePlanService.updateStatus(ratePlanId, ratePlanStatusUpdateRequest))
                    .isInstanceOf(RatePlanNotFoundException.class);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("요금제 상태를 유지보수로 변경")
        void updateStatus_ToUnderMaintenance() {
            // given
            Long ratePlanId = 1L;
            RatePlanStatusUpdateRequest maintenanceRequest = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(maintenanceRequest, "status", Status.UNDER_MAINTENANCE);
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            RatePlanResponse response = ratePlanService.updateStatus(ratePlanId, maintenanceRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(ratePlan.getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);

            then(ratePlanRepository).should().findById(ratePlanId);
        }

        @Test
        @DisplayName("요금제 상태를 활성으로 변경")
        void updateStatus_ToActive() {
            // given
            Long ratePlanId = 1L;
            // 먼저 요금제를 비활성 상태로 설정
            ratePlan.updateStatus(Status.INACTIVE);

            RatePlanStatusUpdateRequest activeRequest = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(activeRequest, "status", Status.ACTIVE);
            given(ratePlanRepository.findById(ratePlanId)).willReturn(Optional.of(ratePlan));

            // when
            RatePlanResponse response = ratePlanService.updateStatus(ratePlanId, activeRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(ratePlan.getStatus()).isEqualTo(Status.ACTIVE);

            then(ratePlanRepository).should().findById(ratePlanId);
        }
    }
}

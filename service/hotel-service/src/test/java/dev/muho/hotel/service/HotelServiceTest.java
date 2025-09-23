package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.HotelStatus;
import dev.muho.hotel.dto.request.HotelCreateRequest;
import dev.muho.hotel.dto.request.HotelStatusUpdateRequest;
import dev.muho.hotel.dto.request.HotelUpdateRequest;
import dev.muho.hotel.dto.response.HotelResponse;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
public class HotelServiceTest {

    @InjectMocks
    private HotelService hotelService;

    @Mock
    private HotelRepository hotelRepository;

    private Hotel hotel;
    private HotelCreateRequest createRequest;
    private HotelUpdateRequest updateRequest;
    private HotelStatusUpdateRequest statusUpdateRequest;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .name("테스트 호텔")
                .address("서울시 강남구")
                .rating(5)
                .build();
        ReflectionTestUtils.setField(hotel, "id", 1L);

        ReflectionTestUtils.setField(createRequest = new HotelCreateRequest(), "name", "새로운 호텔");
        ReflectionTestUtils.setField(createRequest, "address", "서울시 종로구");
        ReflectionTestUtils.setField(createRequest, "rating", 4);

        ReflectionTestUtils.setField(updateRequest = new HotelUpdateRequest(), "name", "수정된 호텔");
        ReflectionTestUtils.setField(updateRequest, "address", "서울시 마포구");
        ReflectionTestUtils.setField(updateRequest, "rating", 3);

        ReflectionTestUtils.setField(statusUpdateRequest = new HotelStatusUpdateRequest(), "status", HotelStatus.SUSPENDED);
    }

    @Nested
    @DisplayName("호텔 조회")
    class FindHotelTests {

        @Test
        @DisplayName("ID로 호텔 조회 성공")
        void findHotelById_Success() {
            // given
            Long hotelId = 1L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.findHotelById(hotelId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getHotelId()).isEqualTo(1L);
            assertThat(response.getHotelName()).isEqualTo("테스트 호텔");
            assertThat(response.getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getStatus()).isEqualTo(HotelStatus.OPERATING);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 ID로 조회 시 예외 발생")
        void findHotelById_NotFound_ThrowsException() {
            // given
            Long hotelId = 999L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hotelService.findHotelById(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("모든 호텔 조회 성공")
        void findAllHotels_Success() {
            // given
            Hotel hotel2 = Hotel.builder()
                    .name("테스트 호텔 2")
                    .address("서울시 서초구")
                    .rating(4)
                    .build();
            ReflectionTestUtils.setField(hotel2, "id", 2L);

            List<Hotel> hotels = Arrays.asList(hotel, hotel2);
            given(hotelRepository.findAll()).willReturn(hotels);

            // when
            List<HotelResponse> responses = hotelService.findAllHotels();

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getHotelName()).isEqualTo("테스트 호텔");
            assertThat(responses.get(1).getHotelName()).isEqualTo("테스트 호텔 2");

            then(hotelRepository).should().findAll();
        }

        @Test
        @DisplayName("빈 목록 조회 성공")
        void findAllHotels_EmptyList_Success() {
            // given
            given(hotelRepository.findAll()).willReturn(List.of());

            // when
            List<HotelResponse> responses = hotelService.findAllHotels();

            // then
            assertThat(responses).isEmpty();

            then(hotelRepository).should().findAll();
        }
    }

    @Nested
    @DisplayName("호텔 생성")
    class CreateHotelTests {

        @Test
        @DisplayName("호텔 생성 성공")
        void createHotel_Success() {
            // given
            Hotel savedHotel = Hotel.builder()
                    .name("새로운 호텔")
                    .address("서울시 종로구")
                    .rating(4)
                    .build();
            ReflectionTestUtils.setField(savedHotel, "id", 2L);

            given(hotelRepository.save(any(Hotel.class))).willReturn(savedHotel);

            // when
            HotelResponse response = hotelService.createHotel(createRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getHotelId()).isEqualTo(2L);
            assertThat(response.getHotelName()).isEqualTo("새로운 호텔");
            assertThat(response.getAddress()).isEqualTo("서울시 종로구");
            assertThat(response.getRating()).isEqualTo(4);
            assertThat(response.getStatus()).isEqualTo(HotelStatus.OPERATING);

            then(hotelRepository).should().save(any(Hotel.class));
        }
    }

    @Nested
    @DisplayName("호텔 수정")
    class UpdateHotelTests {

        @Test
        @DisplayName("호텔 정보 수정 성공")
        void updateHotel_Success() {
            // given
            Long hotelId = 1L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.updateHotel(hotelId, updateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getHotelId()).isEqualTo(1L);
            assertThat(response.getHotelName()).isEqualTo("수정된 호텔");
            assertThat(response.getAddress()).isEqualTo("서울시 마포구");
            assertThat(response.getRating()).isEqualTo(3);

            // 엔티티 메서드 호출 검증
            assertThat(hotel.getName()).isEqualTo("수정된 호텔");
            assertThat(hotel.getAddress()).isEqualTo("서울시 마포구");
            assertThat(hotel.getRating()).isEqualTo(3);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 수정 시 예외 발생")
        void updateHotel_NotFound_ThrowsException() {
            // given
            Long hotelId = 999L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hotelService.updateHotel(hotelId, updateRequest))
                    .isInstanceOf(HotelNotFoundException.class);

            then(hotelRepository).should().findById(hotelId);
            then(hotelRepository).should(never()).save(any(Hotel.class));
        }
    }

    @Nested
    @DisplayName("호텔 삭제")
    class DeleteHotelTests {

        @Test
        @DisplayName("호텔 삭제(상태 변경) 성공")
        void deleteHotel_Success() {
            // given
            Long hotelId = 1L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            hotelService.deleteHotel(hotelId);

            // then
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.CLOSED);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 삭제 시 예외 발생")
        void deleteHotel_NotFound_ThrowsException() {
            // given
            Long hotelId = 999L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hotelService.deleteHotel(hotelId))
                    .isInstanceOf(HotelNotFoundException.class);

            then(hotelRepository).should().findById(hotelId);
        }
    }

    @Nested
    @DisplayName("호텔 상태 변경")
    class UpdateHotelStatusTests {

        @Test
        @DisplayName("호텔 상태 변경 성공")
        void updateHotelStatus_Success() {
            // given
            Long hotelId = 1L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.updateHotelStatus(hotelId, statusUpdateRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getHotelId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(HotelStatus.SUSPENDED);
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.SUSPENDED);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 상태 변경 시 예외 발생")
        void updateHotelStatus_NotFound_ThrowsException() {
            // given
            Long hotelId = 999L;
            given(hotelRepository.findById(hotelId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hotelService.updateHotelStatus(hotelId, statusUpdateRequest))
                    .isInstanceOf(HotelNotFoundException.class);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("호텔 상태를 중지로 변경")
        void updateHotelStatus_ToSuspended() {
            // given
            Long hotelId = 1L;
            HotelStatusUpdateRequest suspendRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(suspendRequest, "status", HotelStatus.SUSPENDED);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.updateHotelStatus(hotelId, suspendRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(HotelStatus.SUSPENDED);
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.SUSPENDED);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("호텔 상태를 미운영으로 변경")
        void updateHotelStatus_ToClosed() {
            // given
            Long hotelId = 1L;
            HotelStatusUpdateRequest closedRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(closedRequest, "status", HotelStatus.CLOSED);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.updateHotelStatus(hotelId, closedRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(HotelStatus.CLOSED);
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.CLOSED);

            then(hotelRepository).should().findById(hotelId);
        }

        @Test
        @DisplayName("호텔 상태를 운영 중으로 변경")
        void updateHotelStatus_ToOperating() {
            // given
            Long hotelId = 1L;
            // 먼저 호텔을 중지 상태로 설정
            hotel.changeStatus(HotelStatus.SUSPENDED);

            HotelStatusUpdateRequest operatingRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(operatingRequest, "status", HotelStatus.OPERATING);
            given(hotelRepository.findById(hotelId)).willReturn(Optional.of(hotel));

            // when
            HotelResponse response = hotelService.updateHotelStatus(hotelId, operatingRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(HotelStatus.OPERATING);
            assertThat(hotel.getStatus()).isEqualTo(HotelStatus.OPERATING);

            then(hotelRepository).should().findById(hotelId);
        }
    }
}

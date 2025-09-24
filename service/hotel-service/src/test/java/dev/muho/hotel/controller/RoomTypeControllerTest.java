package dev.muho.hotel.controller;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RoomTypeCreateRequest;
import dev.muho.hotel.dto.request.RoomTypeStatusUpdateRequest;
import dev.muho.hotel.dto.request.RoomTypeUpdateRequest;
import dev.muho.hotel.dto.response.RoomTypeResponse;
import dev.muho.hotel.global.exception.ErrorResponse;
import dev.muho.hotel.util.DatabaseCleaner;
import dev.muho.hotel.util.TestDataSetupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomTypeControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private TestDataSetupService testDataSetupService;

    private Hotel defaultHotel;
    private RoomType defaultRoomType;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        // 기본 룸타입 조회 (setupHotel에서 생성된 룸타입)
        defaultRoomType = testDataSetupService.setupRoomType();
        defaultHotel = defaultRoomType.getHotel();

        baseUrl = "http://localhost:" + port + "/api/v1/room-types";
    }

    @Nested
    @DisplayName("룸타입 조회 테스트")
    class GetRoomTypeTests {

        @Test
        @DisplayName("ID로 룸타입 조회 성공")
        void getRoomTypeById_Success() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId();

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.getForEntity(url, RoomTypeResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRoomType.getId());
            assertThat(response.getBody().getName()).isEqualTo(defaultRoomType.getName());
            assertThat(response.getBody().getHotelId()).isEqualTo(defaultHotel.getId());
            assertThat(response.getBody().getStandardCapacity()).isEqualTo(defaultRoomType.getStandardCapacity());
            assertThat(response.getBody().getMaxCapacity()).isEqualTo(defaultRoomType.getMaxCapacity());
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 ID로 조회 시 404 반환")
        void getRoomTypeById_NotFound() {
            // given
            String url = baseUrl + "/999";

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("호텔 ID로 룸타입 목록 조회 성공")
        void getRoomTypesByHotelId_Success() {
            // given
            String url = baseUrl + "?hotelId=" + defaultHotel.getId() + "&page=0&size=10";

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).contains("\"content\"");
            assertThat(response.getBody()).contains("\"hotelId\":" + defaultHotel.getId());
        }
    }

    @Nested
    @DisplayName("룸타입 생성 테스트")
    class CreateRoomTypeTests {

        @Test
        @DisplayName("룸타입 생성 성공")
        void createRoomType_Success() {
            // given
            RoomTypeCreateRequest request = new RoomTypeCreateRequest();
            ReflectionTestUtils.setField(request, "hotelId", defaultHotel.getId());
            ReflectionTestUtils.setField(request, "name", "디럭스 룸");
            ReflectionTestUtils.setField(request, "standardCapacity", 2);
            ReflectionTestUtils.setField(request, "maxCapacity", 4);

            HttpEntity<RoomTypeCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.postForEntity(baseUrl, entity, RoomTypeResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("디럭스 룸");
            assertThat(response.getBody().getHotelId()).isEqualTo(defaultHotel.getId());
            assertThat(response.getBody().getStandardCapacity()).isEqualTo(2);
            assertThat(response.getBody().getMaxCapacity()).isEqualTo(4);
            assertThat(response.getBody().getId()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 호텔로 룸타입 생성 시 404 반환")
        void createRoomType_HotelNotFound() {
            // given
            RoomTypeCreateRequest request = new RoomTypeCreateRequest();
            ReflectionTestUtils.setField(request, "hotelId", 999L);
            ReflectionTestUtils.setField(request, "name", "디럭스 룸");
            ReflectionTestUtils.setField(request, "standardCapacity", 2);
            ReflectionTestUtils.setField(request, "maxCapacity", 4);

            HttpEntity<RoomTypeCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 데이터로 룸타입 생성 시 400 반환")
        void createRoomType_BadRequest() {
            // given
            RoomTypeCreateRequest request = new RoomTypeCreateRequest();
            ReflectionTestUtils.setField(request, "hotelId", defaultHotel.getId());
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "standardCapacity", 2);
            ReflectionTestUtils.setField(request, "maxCapacity", 4);

            HttpEntity<RoomTypeCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("유효하지 않은 인원수로 룸타입 생성 시 400 반환")
        void createRoomType_InvalidCapacity() {
            // given
            RoomTypeCreateRequest request = new RoomTypeCreateRequest();
            ReflectionTestUtils.setField(request, "hotelId", defaultHotel.getId());
            ReflectionTestUtils.setField(request, "name", "테스트 룸");
            ReflectionTestUtils.setField(request, "standardCapacity", 0); // 유효하지 않은 인원수
            ReflectionTestUtils.setField(request, "maxCapacity", 4);

            HttpEntity<RoomTypeCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("룸타입 수정 테스트")
    class UpdateRoomTypeTests {

        @Test
        @DisplayName("룸타입 정보 수정 성공")
        void updateRoomType_Success() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId();

            RoomTypeUpdateRequest request = new RoomTypeUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 룸타입");
            ReflectionTestUtils.setField(request, "standardCapacity", 3);
            ReflectionTestUtils.setField(request, "maxCapacity", 6);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<RoomTypeUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    RoomTypeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRoomType.getId());
            assertThat(response.getBody().getName()).isEqualTo("수정된 룸타입");
            assertThat(response.getBody().getStandardCapacity()).isEqualTo(3);
            assertThat(response.getBody().getMaxCapacity()).isEqualTo(6);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 수정 시 404 반환")
        void updateRoomType_NotFound() {
            // given
            String url = baseUrl + "/999";

            RoomTypeUpdateRequest request = new RoomTypeUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 룸타입");
            ReflectionTestUtils.setField(request, "standardCapacity", 3);
            ReflectionTestUtils.setField(request, "maxCapacity", 6);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<RoomTypeUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 데이터로 룸타입 수정 시 400 반환")
        void updateRoomType_BadRequest() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId();

            RoomTypeUpdateRequest request = new RoomTypeUpdateRequest();
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "standardCapacity", 3);
            ReflectionTestUtils.setField(request, "maxCapacity", 6);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<RoomTypeUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("룸타입 삭제 테스트")
    class DeleteRoomTypeTests {

        @Test
        @DisplayName("룸타입 삭제 성공")
        void deleteRoomType_Success() {
            // given
            Long roomTypeId = defaultRoomType.getId();
            String url = baseUrl + "/" + roomTypeId;

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 삭제 후 조회해서 상태 확인
            ResponseEntity<RoomTypeResponse> finalGetResponse = restTemplate.getForEntity(
                    baseUrl + "/" + roomTypeId,
                    RoomTypeResponse.class
            );

            assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalGetResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 삭제 시 404 반환")
        void deleteRoomType_NotFound() {
            // given
            String url = baseUrl + "/999";

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    null,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("룸타입 상태 변경 테스트")
    class UpdateRoomTypeStatusTests {

        @Test
        @DisplayName("룸타입 상태를 비활성으로 변경 성공")
        void updateRoomTypeStatus_ToInactive_Success() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId() + "/status";

            RoomTypeStatusUpdateRequest request = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.INACTIVE);

            HttpEntity<RoomTypeStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RoomTypeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRoomType.getId());
        }

        @Test
        @DisplayName("룸타입 상태를 유지보수로 변경 성공")
        void updateRoomTypeStatus_ToUnderMaintenance_Success() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId() + "/status";

            RoomTypeStatusUpdateRequest request = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.UNDER_MAINTENANCE);

            HttpEntity<RoomTypeStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RoomTypeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRoomType.getId());
        }

        @Test
        @DisplayName("룸타입 상태를 활성으로 변경 성공")
        void updateRoomTypeStatus_ToActive_Success() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId() + "/status";

            // 먼저 비활성 상태로 변경
            RoomTypeStatusUpdateRequest inactiveRequest = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(inactiveRequest, "status", Status.INACTIVE);
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(inactiveRequest), RoomTypeResponse.class);

            // 활성으로 다시 변경
            RoomTypeStatusUpdateRequest activeRequest = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(activeRequest, "status", Status.ACTIVE);
            HttpEntity<RoomTypeStatusUpdateRequest> entity = new HttpEntity<>(activeRequest);

            // when
            ResponseEntity<RoomTypeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RoomTypeResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRoomType.getId());
        }

        @Test
        @DisplayName("존재하지 않는 룸타입 상태 변경 시 404 반환")
        void updateRoomTypeStatus_NotFound() {
            // given
            String url = baseUrl + "/999/status";

            RoomTypeStatusUpdateRequest request = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.INACTIVE);

            HttpEntity<RoomTypeStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 상태값으로 변경 시 400 반환")
        void updateRoomTypeStatus_BadRequest() {
            // given
            String url = baseUrl + "/" + defaultRoomType.getId() + "/status";

            // null 상태값
            RoomTypeStatusUpdateRequest request = new RoomTypeStatusUpdateRequest();
            // status를 null로 설정 (ReflectionTestUtils로 null 설정하지 않음)

            HttpEntity<RoomTypeStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("룸타입 생성 -> 조회 -> 수정 -> 상태변경 -> 삭제 시나리오")
        void roomTypeLifecycleScenario() {
            // 1. 룸타입 생성
            RoomTypeCreateRequest createRequest = new RoomTypeCreateRequest();
            ReflectionTestUtils.setField(createRequest, "hotelId", defaultHotel.getId());
            ReflectionTestUtils.setField(createRequest, "name", "시나리오 룸타입");
            ReflectionTestUtils.setField(createRequest, "standardCapacity", 2);
            ReflectionTestUtils.setField(createRequest, "maxCapacity", 4);

            ResponseEntity<RoomTypeResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    new HttpEntity<>(createRequest),
                    RoomTypeResponse.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long roomTypeId = createResponse.getBody().getId();

            // 2. 생성된 룸타입 조회
            ResponseEntity<RoomTypeResponse> getResponse = restTemplate.getForEntity(
                    baseUrl + "/" + roomTypeId,
                    RoomTypeResponse.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getName()).isEqualTo("시나리오 룸타입");

            // 3. 룸타입 정보 수정
            RoomTypeUpdateRequest updateRequest = new RoomTypeUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "name", "수정된 시나리오 룸타입");
            ReflectionTestUtils.setField(updateRequest, "standardCapacity", 3);
            ReflectionTestUtils.setField(updateRequest, "maxCapacity", 6);
            ReflectionTestUtils.setField(updateRequest, "status", Status.ACTIVE);

            ResponseEntity<RoomTypeResponse> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + roomTypeId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    RoomTypeResponse.class
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody().getName()).isEqualTo("수정된 시나리오 룸타입");

            // 4. 룸타입 상태를 유지보수로 변경
            RoomTypeStatusUpdateRequest statusRequest = new RoomTypeStatusUpdateRequest();
            ReflectionTestUtils.setField(statusRequest, "status", Status.UNDER_MAINTENANCE);

            ResponseEntity<RoomTypeResponse> statusResponse = restTemplate.exchange(
                    baseUrl + "/" + roomTypeId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(statusRequest),
                    RoomTypeResponse.class
            );

            assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 5. 룸타입 삭제
            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    baseUrl + "/" + roomTypeId,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 6. 삭제 후 조회하여 404 확인
            ResponseEntity<RoomTypeResponse> finalGetResponse = restTemplate.getForEntity(
                    baseUrl + "/" + roomTypeId,
                    RoomTypeResponse.class
            );

            assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalGetResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }
    }
}

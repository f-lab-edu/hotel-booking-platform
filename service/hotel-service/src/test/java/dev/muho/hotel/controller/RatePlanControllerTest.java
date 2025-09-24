package dev.muho.hotel.controller;

import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.RatePlanCreateRequest;
import dev.muho.hotel.dto.request.RatePlanStatusUpdateRequest;
import dev.muho.hotel.dto.request.RatePlanUpdateRequest;
import dev.muho.hotel.dto.response.RatePlanResponse;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RatePlanControllerTest {

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

    private RoomType defaultRoomType;
    private RatePlan defaultRatePlan;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        // 기본 룸타입 조회 (setupHotel에서 생성된 룸타입)
        defaultRoomType = testDataSetupService.setupRoomType();
        // 기본 요금제 설정
        defaultRatePlan = testDataSetupService.setupRatePlan(defaultRoomType);

        baseUrl = "http://localhost:" + port + "/api/v1/rate-plans";
    }

    @Nested
    @DisplayName("요금제 조회 테스트")
    class GetRatePlanTests {

        @Test
        @DisplayName("ID로 요금제 조회 성공")
        void getRatePlanById_Success() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId();

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.getForEntity(url, RatePlanResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRatePlan.getId());
            assertThat(response.getBody().getName()).isEqualTo(defaultRatePlan.getName());
            assertThat(response.getBody().getRoomTypeId()).isEqualTo(defaultRoomType.getId());
            assertThat(response.getBody().isIncludesBreakfast()).isEqualTo(defaultRatePlan.isIncludesBreakfast());
            assertThat(response.getBody().isRefundable()).isEqualTo(defaultRatePlan.isRefundable());
            assertThat(response.getBody().getMinNights()).isEqualTo(defaultRatePlan.getMinNights());
        }

        @Test
        @DisplayName("존재하지 않는 요금제 ID로 조회 시 404 반환")
        void getRatePlanById_NotFound() {
            // given
            String url = baseUrl + "/999";

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("룸타입 ID로 요금제 목록 조회 성공")
        void getRatePlansByRoomTypeId_Success() {
            // given
            String url = baseUrl + "?hotelId=1&roomTypeId=" + defaultRoomType.getId() + "&page=0&size=10";

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
            assertThat(response.getBody()).contains("\"roomTypeId\":" + defaultRoomType.getId());
        }
    }

    @Nested
    @DisplayName("요금제 생성 테스트")
    class CreateRatePlanTests {

        @Test
        @DisplayName("요금제 생성 성공")
        void createRatePlan_Success() {
            // given
            RatePlanCreateRequest request = new RatePlanCreateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "name", "얼리버드 특가");
            ReflectionTestUtils.setField(request, "includesBreakfast", true);
            ReflectionTestUtils.setField(request, "refundable", false);
            ReflectionTestUtils.setField(request, "minNights", 2);
            ReflectionTestUtils.setField(request, "maxNights", 7);
            ReflectionTestUtils.setField(request, "bookingStartDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "bookingEndDate", LocalDate.of(2024, 12, 31));
            ReflectionTestUtils.setField(request, "checkInStartDate", LocalDate.of(2024, 3, 1));
            ReflectionTestUtils.setField(request, "checkInEndDate", LocalDate.of(2024, 11, 30));

            HttpEntity<RatePlanCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.postForEntity(baseUrl, entity, RatePlanResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("얼리버드 특가");
            assertThat(response.getBody().getRoomTypeId()).isEqualTo(defaultRoomType.getId());
            assertThat(response.getBody().isIncludesBreakfast()).isTrue();
            assertThat(response.getBody().isRefundable()).isFalse();
            assertThat(response.getBody().getMinNights()).isEqualTo(2);
            assertThat(response.getBody().getId()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 룸타입으로 요금제 생성 시 404 반환")
        void createRatePlan_RoomTypeNotFound() {
            // given
            RatePlanCreateRequest request = new RatePlanCreateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", 999L);
            ReflectionTestUtils.setField(request, "name", "테스트 요금제");
            ReflectionTestUtils.setField(request, "includesBreakfast", true);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 1);

            HttpEntity<RatePlanCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 데이터로 요금제 생성 시 400 반환")
        void createRatePlan_BadRequest() {
            // given
            RatePlanCreateRequest request = new RatePlanCreateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "includesBreakfast", true);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 1);

            HttpEntity<RatePlanCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("유효하지 않은 최소 숙박일로 요금제 생성 시 400 반환")
        void createRatePlan_InvalidMinNights() {
            // given
            RatePlanCreateRequest request = new RatePlanCreateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "name", "테스트 요금제");
            ReflectionTestUtils.setField(request, "includesBreakfast", true);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 0); // 유효하지 않은 최소 숙박일

            HttpEntity<RatePlanCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("요금제 수정 테스트")
    class UpdateRatePlanTests {

        @Test
        @DisplayName("요금제 정보 수정 성공")
        void updateRatePlan_Success() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId();

            RatePlanUpdateRequest request = new RatePlanUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 요금제");
            ReflectionTestUtils.setField(request, "includesBreakfast", false);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 3);
            ReflectionTestUtils.setField(request, "maxNights", 10);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);
            ReflectionTestUtils.setField(request, "bookingStartDate", LocalDate.of(2024, 2, 1));
            ReflectionTestUtils.setField(request, "bookingEndDate", LocalDate.of(2024, 11, 30));
            ReflectionTestUtils.setField(request, "checkInStartDate", LocalDate.of(2024, 3, 1));
            ReflectionTestUtils.setField(request, "checkInEndDate", LocalDate.of(2024, 10, 31));

            HttpEntity<RatePlanUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    RatePlanResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRatePlan.getId());
            assertThat(response.getBody().getName()).isEqualTo("수정된 요금제");
            assertThat(response.getBody().isIncludesBreakfast()).isFalse();
            assertThat(response.getBody().isRefundable()).isTrue();
            assertThat(response.getBody().getMinNights()).isEqualTo(3);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 수정 시 404 반환")
        void updateRatePlan_NotFound() {
            // given
            String url = baseUrl + "/999";

            RatePlanUpdateRequest request = new RatePlanUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 요금제");
            ReflectionTestUtils.setField(request, "includesBreakfast", false);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 3);
            ReflectionTestUtils.setField(request, "maxNights", 10);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<RatePlanUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("잘못된 데이터로 요금제 수정 시 400 반환")
        void updateRatePlan_BadRequest() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId();

            RatePlanUpdateRequest request = new RatePlanUpdateRequest();
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "includesBreakfast", false);
            ReflectionTestUtils.setField(request, "refundable", true);
            ReflectionTestUtils.setField(request, "minNights", 3);
            ReflectionTestUtils.setField(request, "maxNights", 10);
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<RatePlanUpdateRequest> entity = new HttpEntity<>(request);

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
    @DisplayName("요금제 삭제 테스트")
    class DeleteRatePlanTests {

        @Test
        @DisplayName("요금제 삭제 성공")
        void deleteRatePlan_Success() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            String url = baseUrl + "/" + ratePlanId;

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
            ResponseEntity<RatePlanResponse> finalGetResponse = restTemplate.getForEntity(
                    baseUrl + "/" + ratePlanId,
                    RatePlanResponse.class
            );

            assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalGetResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 요금제 삭제 시 404 반환")
        void deleteRatePlan_NotFound() {
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
    @DisplayName("요금제 상태 변경 테스트")
    class UpdateRatePlanStatusTests {

        @Test
        @DisplayName("요금제 상태를 비활성으로 변경 성공")
        void updateRatePlanStatus_ToInactive_Success() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId() + "/status";

            RatePlanStatusUpdateRequest request = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.INACTIVE);

            HttpEntity<RatePlanStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RatePlanResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRatePlan.getId());
        }

        @Test
        @DisplayName("요금제 상태를 유지보수로 변경 성공")
        void updateRatePlanStatus_ToUnderMaintenance_Success() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId() + "/status";

            RatePlanStatusUpdateRequest request = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.UNDER_MAINTENANCE);

            HttpEntity<RatePlanStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RatePlanResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRatePlan.getId());
        }

        @Test
        @DisplayName("요금제 상태를 활성으로 변경 성공")
        void updateRatePlanStatus_ToActive_Success() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId() + "/status";

            // 먼저 비활성 상태로 변경
            RatePlanStatusUpdateRequest inactiveRequest = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(inactiveRequest, "status", Status.INACTIVE);
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(inactiveRequest), RatePlanResponse.class);

            // 활성으로 다시 변경
            RatePlanStatusUpdateRequest activeRequest = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(activeRequest, "status", Status.ACTIVE);
            HttpEntity<RatePlanStatusUpdateRequest> entity = new HttpEntity<>(activeRequest);

            // when
            ResponseEntity<RatePlanResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    RatePlanResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultRatePlan.getId());
        }

        @Test
        @DisplayName("존재하지 않는 요금제 상태 변경 시 404 반환")
        void updateRatePlanStatus_NotFound() {
            // given
            String url = baseUrl + "/999/status";

            RatePlanStatusUpdateRequest request = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.INACTIVE);

            HttpEntity<RatePlanStatusUpdateRequest> entity = new HttpEntity<>(request);

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
        void updateRatePlanStatus_BadRequest() {
            // given
            String url = baseUrl + "/" + defaultRatePlan.getId() + "/status";

            // null 상태값
            RatePlanStatusUpdateRequest request = new RatePlanStatusUpdateRequest();
            // status를 null로 설정 (ReflectionTestUtils로 null 설정하지 않음)

            HttpEntity<RatePlanStatusUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("요금제 생성 -> 조회 -> 수정 -> 상태변경 -> 삭제 시나리오")
        void ratePlanLifecycleScenario() {
            // 1. 요금제 생성
            RatePlanCreateRequest createRequest = new RatePlanCreateRequest();
            ReflectionTestUtils.setField(createRequest, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(createRequest, "name", "시나리오 요금제");
            ReflectionTestUtils.setField(createRequest, "includesBreakfast", true);
            ReflectionTestUtils.setField(createRequest, "refundable", false);
            ReflectionTestUtils.setField(createRequest, "minNights", 2);
            ReflectionTestUtils.setField(createRequest, "maxNights", 5);
            ReflectionTestUtils.setField(createRequest, "bookingStartDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(createRequest, "bookingEndDate", LocalDate.of(2024, 12, 31));
            ReflectionTestUtils.setField(createRequest, "checkInStartDate", LocalDate.of(2024, 3, 1));
            ReflectionTestUtils.setField(createRequest, "checkInEndDate", LocalDate.of(2024, 11, 30));

            ResponseEntity<RatePlanResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    new HttpEntity<>(createRequest),
                    RatePlanResponse.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long ratePlanId = createResponse.getBody().getId();

            // 2. 생성된 요금제 조회
            ResponseEntity<RatePlanResponse> getResponse = restTemplate.getForEntity(
                    baseUrl + "/" + ratePlanId,
                    RatePlanResponse.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getName()).isEqualTo("시나리오 요금제");

            // 3. 요금제 정보 수정
            RatePlanUpdateRequest updateRequest = new RatePlanUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "name", "수정된 시나리오 요금제");
            ReflectionTestUtils.setField(updateRequest, "includesBreakfast", false);
            ReflectionTestUtils.setField(updateRequest, "refundable", true);
            ReflectionTestUtils.setField(updateRequest, "minNights", 3);
            ReflectionTestUtils.setField(updateRequest, "maxNights", 7);
            ReflectionTestUtils.setField(updateRequest, "status", Status.ACTIVE);
            ReflectionTestUtils.setField(updateRequest, "bookingStartDate", LocalDate.of(2024, 2, 1));
            ReflectionTestUtils.setField(updateRequest, "bookingEndDate", LocalDate.of(2024, 11, 30));
            ReflectionTestUtils.setField(updateRequest, "checkInStartDate", LocalDate.of(2024, 3, 1));
            ReflectionTestUtils.setField(updateRequest, "checkInEndDate", LocalDate.of(2024, 10, 31));

            ResponseEntity<RatePlanResponse> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + ratePlanId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    RatePlanResponse.class
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody().getName()).isEqualTo("수정된 시나리오 요금제");

            // 4. 요금제 상태를 유지보수로 변경
            RatePlanStatusUpdateRequest statusRequest = new RatePlanStatusUpdateRequest();
            ReflectionTestUtils.setField(statusRequest, "status", Status.UNDER_MAINTENANCE);

            ResponseEntity<RatePlanResponse> statusResponse = restTemplate.exchange(
                    baseUrl + "/" + ratePlanId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(statusRequest),
                    RatePlanResponse.class
            );

            assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 5. 요금제 삭제
            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    baseUrl + "/" + ratePlanId,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 6. 삭제 후 조회하여 INACTIVE 상태 확인
            ResponseEntity<RatePlanResponse> finalGetResponse = restTemplate.getForEntity(
                    baseUrl + "/" + ratePlanId,
                    RatePlanResponse.class
            );

            assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalGetResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }
    }
}

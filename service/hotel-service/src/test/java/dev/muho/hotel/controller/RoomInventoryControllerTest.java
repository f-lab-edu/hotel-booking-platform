package dev.muho.hotel.controller;

import dev.muho.hotel.domain.RoomInventory;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.dto.response.RoomInventoryResponse;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomInventoryControllerTest {

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
    private RoomInventory defaultRoomInventory;
    private String baseUrl;
    private String bulkUpdateUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        defaultRoomType = testDataSetupService.setupRoomType();
        defaultRoomInventory = testDataSetupService.setupRoomInventory(defaultRoomType);

        baseUrl = "http://localhost:" + port + "/api/v1/room-types";
        bulkUpdateUrl = "http://localhost:" + port + "/api/v1/inventories/bulk-update";
    }

    @Nested
    @DisplayName("객실 재고 조회 테스트")
    class GetRoomInventoryTests {

        @Test
        @DisplayName("객실 재고 조회 성공 - 기존 재고가 있는 경우")
        void getRoomInventories_Success_WithExistingInventory() {
            // given
            Long roomTypeId = defaultRoomType.getId();
            LocalDate startDate = defaultRoomInventory.getDate();
            LocalDate endDate = startDate.plusDays(2);

            String url = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, roomTypeId, startDate, endDate);

            // when
            ResponseEntity<List<RoomInventoryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3); // startDate부터 endDate까지 3일

            // 첫 번째 날짜는 기존 재고가 있어야 함
            RoomInventoryResponse firstDay = response.getBody().get(0);
            assertThat(firstDay.getDate()).isEqualTo(startDate);
            assertThat(firstDay.getTotalQuantity()).isEqualTo(defaultRoomInventory.getTotalQuantity());
            assertThat(firstDay.getReservedQuantity()).isEqualTo(defaultRoomInventory.getReservedQuantity());
            assertThat(firstDay.getAvailableQuantity()).isEqualTo(
                    defaultRoomInventory.getTotalQuantity() - defaultRoomInventory.getReservedQuantity());
        }

        @Test
        @DisplayName("객실 재고 조회 성공 - 기존 재고가 없는 경우")
        void getRoomInventories_Success_WithNoExistingInventory() {
            // given
            Long roomTypeId = defaultRoomType.getId();
            LocalDate startDate = LocalDate.of(2025, 1, 1); // 미래 날짜로 설정
            LocalDate endDate = startDate.plusDays(1);

            String url = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, roomTypeId, startDate, endDate);

            // when
            ResponseEntity<List<RoomInventoryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);

            // 모든 날짜가 기본값(0)이어야 함
            response.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(0);
                assertThat(inventory.getReservedQuantity()).isEqualTo(0);
                assertThat(inventory.getAvailableQuantity()).isEqualTo(0);
            });
        }

        @Test
        @DisplayName("존재하지 않는 객실 타입으로 재고 조회 시 404 반환")
        void getRoomInventories_RoomTypeNotFound() {
            // given
            Long roomTypeId = 999L;
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 2);

            String url = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, roomTypeId, startDate, endDate);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 날짜 파라미터로 조회 시 400 반환")
        void getRoomInventories_InvalidDateParameters() {
            // given
            Long roomTypeId = defaultRoomType.getId();
            String url = String.format("%s?roomTypeId=%d&startDate=invalid-date&endDate=2024-01-02",
                    baseUrl, roomTypeId);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("필수 파라미터 누락 시 400 반환")
        void getRoomInventories_MissingRequiredParameters() {
            // given
            String url = baseUrl; // 파라미터 없음

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("객실 재고 대량 업데이트 테스트")
    class BulkUpdateInventoryTests {

        @Test
        @DisplayName("객실 재고 대량 업데이트 성공 - 기존 재고 업데이트")
        void bulkUpdateInventory_Success_UpdateExisting() {
            // given
            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "startDate", defaultRoomInventory.getDate());
            ReflectionTestUtils.setField(request, "endDate", defaultRoomInventory.getDate());
            ReflectionTestUtils.setField(request, "totalQuantity", 20);

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 업데이트 후 조회하여 확인
            String getUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, defaultRoomType.getId(),
                    defaultRoomInventory.getDate(), defaultRoomInventory.getDate());

            ResponseEntity<List<RoomInventoryResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().get(0).getTotalQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("객실 재고 대량 업데이트 성공 - 새로운 재고 생성")
        void bulkUpdateInventory_Success_CreateNew() {
            // given
            LocalDate futureDate = LocalDate.of(2025, 6, 1);

            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "startDate", futureDate);
            ReflectionTestUtils.setField(request, "endDate", futureDate.plusDays(2));
            ReflectionTestUtils.setField(request, "totalQuantity", 15);

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 생성 후 조회하여 확인
            String getUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, defaultRoomType.getId(), futureDate, futureDate.plusDays(2));

            ResponseEntity<List<RoomInventoryResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(3);
            getResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(15);
                assertThat(inventory.getReservedQuantity()).isEqualTo(0);
                assertThat(inventory.getAvailableQuantity()).isEqualTo(15);
            });
        }

        @Test
        @DisplayName("존재하지 않는 객실 타입으로 대량 업데이트 시 404 반환")
        void bulkUpdateInventory_RoomTypeNotFound() {
            // given
            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", 999L);
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "totalQuantity", 10);

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("잘못된 데이터로 대량 업데이트 시 400 반환")
        void bulkUpdateInventory_BadRequest() {
            // given
            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "totalQuantity", -1); // 음수 재고

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("예약된 수량보다 적은 총 재고로 업데이트 시 400 반환")
        void bulkUpdateInventory_TotalQuantityLessThanReserved() {
            // given - 예약 수량이 있는 재고에 더 적은 총 재고로 업데이트 시도
            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "startDate", defaultRoomInventory.getDate());
            ReflectionTestUtils.setField(request, "endDate", defaultRoomInventory.getDate());
            ReflectionTestUtils.setField(request, "totalQuantity",
                    defaultRoomInventory.getReservedQuantity() - 1); // 예약 수량보다 적게 설정

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    ErrorResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("장기간 대량 업데이트 성공")
        void bulkUpdateInventory_Success_LongPeriod() {
            // given
            LocalDate startDate = LocalDate.of(2025, 7, 1);
            LocalDate endDate = LocalDate.of(2025, 7, 30); // 30일간

            RoomInventoryBulkUpdateRequest request = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(request, "startDate", startDate);
            ReflectionTestUtils.setField(request, "endDate", endDate);
            ReflectionTestUtils.setField(request, "totalQuantity", 25);

            HttpEntity<RoomInventoryBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<Void> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 일부 날짜만 확인
            String getUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, defaultRoomType.getId(), startDate, startDate.plusDays(2));

            ResponseEntity<List<RoomInventoryResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(3);
            getResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(25);
            });
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("재고 조회 -> 대량 업데이트 -> 재조회 시나리오")
        void roomInventoryLifecycleScenario() {
            // 1. 초기 재고 조회 (기존 재고 없는 미래 날짜)
            LocalDate startDate = LocalDate.of(2025, 8, 1);
            LocalDate endDate = LocalDate.of(2025, 8, 5);

            String getUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, defaultRoomType.getId(), startDate, endDate);

            ResponseEntity<List<RoomInventoryResponse>> initialResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(initialResponse.getBody()).hasSize(5);
            initialResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(0);
            });

            // 2. 재고 대량 생성
            RoomInventoryBulkUpdateRequest createRequest = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(createRequest, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(createRequest, "startDate", startDate);
            ReflectionTestUtils.setField(createRequest, "endDate", endDate);
            ReflectionTestUtils.setField(createRequest, "totalQuantity", 30);

            ResponseEntity<Void> createResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(createRequest),
                    Void.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 3. 생성 후 재고 조회
            ResponseEntity<List<RoomInventoryResponse>> afterCreateResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(afterCreateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            afterCreateResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(30);
                assertThat(inventory.getAvailableQuantity()).isEqualTo(30);
            });

            // 4. 재고 수정 (일부 기간만)
            RoomInventoryBulkUpdateRequest updateRequest = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(updateRequest, "startDate", startDate.plusDays(1));
            ReflectionTestUtils.setField(updateRequest, "endDate", startDate.plusDays(3));
            ReflectionTestUtils.setField(updateRequest, "totalQuantity", 50);

            ResponseEntity<Void> updateResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    Void.class
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 5. 최종 재고 조회 및 확인
            ResponseEntity<List<RoomInventoryResponse>> finalResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {}
            );

            assertThat(finalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<RoomInventoryResponse> finalInventories = finalResponse.getBody();

            // 첫째 날과 마지막 날은 30개
            assertThat(finalInventories.get(0).getTotalQuantity()).isEqualTo(30);
            assertThat(finalInventories.get(4).getTotalQuantity()).isEqualTo(30);

            // 중간 3일은 50개
            assertThat(finalInventories.get(1).getTotalQuantity()).isEqualTo(50);
            assertThat(finalInventories.get(2).getTotalQuantity()).isEqualTo(50);
            assertThat(finalInventories.get(3).getTotalQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("여러 객실 타입의 재고 관리 시나리오")
        void multipleRoomTypeInventoryScenario() {
            // 추가 룸타입 생성
            RoomType secondRoomType = testDataSetupService.setupSecondRoomType();

            LocalDate testDate = LocalDate.of(2025, 9, 1);

            // 첫 번째 룸타입 재고 설정
            RoomInventoryBulkUpdateRequest firstRequest = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(firstRequest, "roomTypeId", defaultRoomType.getId());
            ReflectionTestUtils.setField(firstRequest, "startDate", testDate);
            ReflectionTestUtils.setField(firstRequest, "endDate", testDate.plusDays(1));
            ReflectionTestUtils.setField(firstRequest, "totalQuantity", 20);

            restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(firstRequest),
                    Void.class
            );

            // 두 번째 룸타입 재고 설정
            RoomInventoryBulkUpdateRequest secondRequest = new RoomInventoryBulkUpdateRequest();
            ReflectionTestUtils.setField(secondRequest, "roomTypeId", secondRoomType.getId());
            ReflectionTestUtils.setField(secondRequest, "startDate", testDate);
            ReflectionTestUtils.setField(secondRequest, "endDate", testDate.plusDays(1));
            ReflectionTestUtils.setField(secondRequest, "totalQuantity", 35);

            restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(secondRequest),
                    Void.class
            );

            // 각각의 재고 조회 및 확인
            String firstUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, defaultRoomType.getId(), testDate, testDate.plusDays(1));
            String secondUrl = String.format("%s/%d/inventories?startDate=%s&endDate=%s",
                    baseUrl, secondRoomType.getId(), testDate, testDate.plusDays(1));

            ResponseEntity<List<RoomInventoryResponse>> firstResponse = restTemplate.exchange(
                    firstUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {});

            ResponseEntity<List<RoomInventoryResponse>> secondResponse = restTemplate.exchange(
                    secondUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<RoomInventoryResponse>>() {});

            assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            firstResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(20);
            });

            secondResponse.getBody().forEach(inventory -> {
                assertThat(inventory.getTotalQuantity()).isEqualTo(35);
            });
        }
    }
}

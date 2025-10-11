package dev.muho.hotel.controller;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.HotelCreateRequest;
import dev.muho.hotel.dto.request.HotelStatusUpdateRequest;
import dev.muho.hotel.dto.request.HotelUpdateRequest;
import dev.muho.hotel.dto.response.HotelResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HotelControllerTest {

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

    private List<Hotel> defaultHotels;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        defaultHotels = testDataSetupService.setupHotels();
        baseUrl = "http://localhost:" + port + "/api/v1/hotels";
    }

    @Nested
    @DisplayName("호텔 조회 테스트")
    class GetHotelTests {

        @Test
        @DisplayName("ID로 호텔 조회 성공")
        void getHotelById_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId();

            // when
            ResponseEntity<HotelResponse> response = restTemplate.getForEntity(url, HotelResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelId()).isEqualTo(hotel.getId());
            assertThat(response.getBody().getHotelName()).isEqualTo(hotel.getName());
            assertThat(response.getBody().getAddress()).isEqualTo(hotel.getAddress());
            assertThat(response.getBody().getRating()).isEqualTo(hotel.getRating());
            assertThat(response.getBody().getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 ID로 조회 시 404 반환")
        void getHotelById_NotFound() {
            // given
            String url = baseUrl + "/999";

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("모든 호텔 조회 성공")
        void getAllHotels_Success() {
            // given
            String url = baseUrl;

            // when
            ResponseEntity<List<HotelResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<HotelResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(defaultHotels.size());
        }
    }

    @Nested
    @DisplayName("호텔 생성 테스트")
    class CreateHotelTests {

        @Test
        @DisplayName("호텔 생성 성공")
        void createHotel_Success() {
            // given
            HotelCreateRequest request = new HotelCreateRequest();
            ReflectionTestUtils.setField(request, "name", "새로운 호텔");
            ReflectionTestUtils.setField(request, "address", "서울시 강남구");
            ReflectionTestUtils.setField(request, "rating", 4);

            HttpEntity<HotelCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<HotelResponse> response = restTemplate.postForEntity(baseUrl, entity, HotelResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelName()).isEqualTo("새로운 호텔");
            assertThat(response.getBody().getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getBody().getRating()).isEqualTo(4);
            assertThat(response.getBody().getStatus()).isEqualTo(Status.ACTIVE);
            assertThat(response.getBody().getHotelId()).isNotNull();
        }

        @Test
        @DisplayName("잘못된 데이터로 호텔 생성 시 400 반환")
        void createHotel_BadRequest() {
            // given
            HotelCreateRequest request = new HotelCreateRequest();
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "address", "서울시 강남구");
            ReflectionTestUtils.setField(request, "rating", 4);

            HttpEntity<HotelCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("등급 범위 초과로 호텔 생성 시 400 반환")
        void createHotel_InvalidRating() {
            // given
            HotelCreateRequest request = new HotelCreateRequest();
            ReflectionTestUtils.setField(request, "name", "테스트 호텔");
            ReflectionTestUtils.setField(request, "address", "서울시 강남구");
            ReflectionTestUtils.setField(request, "rating", 6); // 유효하지 않은 등급

            HttpEntity<HotelCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("호텔 수정 테스트")
    class UpdateHotelTests {

        @Test
        @DisplayName("호텔 정보 수정 성공")
        void updateHotel_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId();

            HotelUpdateRequest request = new HotelUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 호텔명");
            ReflectionTestUtils.setField(request, "address", "수정된 주소");
            ReflectionTestUtils.setField(request, "rating", 3);

            HttpEntity<HotelUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<HotelResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    HotelResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelId()).isEqualTo(hotel.getId());
            assertThat(response.getBody().getHotelName()).isEqualTo("수정된 호텔명");
            assertThat(response.getBody().getAddress()).isEqualTo("수정된 주소");
            assertThat(response.getBody().getRating()).isEqualTo(3);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 수정 시 404 반환")
        void updateHotel_NotFound() {
            // given
            String url = baseUrl + "/999";

            HotelUpdateRequest request = new HotelUpdateRequest();
            ReflectionTestUtils.setField(request, "name", "수정된 호텔명");
            ReflectionTestUtils.setField(request, "address", "수정된 주소");
            ReflectionTestUtils.setField(request, "rating", 3);

            HttpEntity<HotelUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("잘못된 데이터로 호텔 수정 시 400 반환")
        void updateHotel_BadRequest() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId();

            HotelUpdateRequest request = new HotelUpdateRequest();
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "address", "수정된 주소");
            ReflectionTestUtils.setField(request, "rating", 3);

            HttpEntity<HotelUpdateRequest> entity = new HttpEntity<>(request);

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
    @DisplayName("호텔 삭제 테스트")
    class DeleteHotelTests {

        @Test
        @DisplayName("호텔 삭제(논리적 삭제) 성공")
        void deleteHotel_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId();

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
            ResponseEntity<HotelResponse> getResponse = restTemplate.getForEntity(url, HotelResponse.class);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 삭제 시 404 반환")
        void deleteHotel_NotFound() {
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
    @DisplayName("호텔 상태 변경 테스트")
    class UpdateHotelStatusTests {

        @Test
        @DisplayName("호텔 상태를 중지로 변경 성공")
        void updateHotelStatus_ToSuspended_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId() + "/status";

            HotelStatusUpdateRequest request = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.UNDER_MAINTENANCE);

            HttpEntity<HotelStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<HotelResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    HotelResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelId()).isEqualTo(hotel.getId());
            assertThat(response.getBody().getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);
        }

        @Test
        @DisplayName("호텔 상태를 미운영으로 변경 성공")
        void updateHotelStatus_ToClosed_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId() + "/status";

            HotelStatusUpdateRequest request = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.INACTIVE);

            HttpEntity<HotelStatusUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<HotelResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    HotelResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelId()).isEqualTo(hotel.getId());
            assertThat(response.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }

        @Test
        @DisplayName("호텔 상태를 운영 중으로 변경 성공")
        void updateHotelStatus_ToOperating_Success() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId() + "/status";

            // 먼저 중지 상태로 변경
            HotelStatusUpdateRequest suspendRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(suspendRequest, "status", Status.UNDER_MAINTENANCE);
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(suspendRequest), HotelResponse.class);

            // 운영 중으로 다시 변경
            HotelStatusUpdateRequest operatingRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(operatingRequest, "status", Status.ACTIVE);
            HttpEntity<HotelStatusUpdateRequest> entity = new HttpEntity<>(operatingRequest);

            // when
            ResponseEntity<HotelResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    HotelResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getHotelId()).isEqualTo(hotel.getId());
            assertThat(response.getBody().getStatus()).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 호텔 상태 변경 시 404 반환")
        void updateHotelStatus_NotFound() {
            // given
            String url = baseUrl + "/999/status";

            HotelStatusUpdateRequest request = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(request, "status", Status.UNDER_MAINTENANCE);

            HttpEntity<HotelStatusUpdateRequest> entity = new HttpEntity<>(request);

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
        void updateHotelStatus_BadRequest() {
            // given
            Hotel hotel = defaultHotels.get(0);
            String url = baseUrl + "/" + hotel.getId() + "/status";

            // null 상태값
            HotelStatusUpdateRequest request = new HotelStatusUpdateRequest();
            // status를 null로 설정 (ReflectionTestUtils로 null 설정하지 않음)

            HttpEntity<HotelStatusUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("호텔 생성 -> 조회 -> 수정 -> 상태변경 -> 삭제 시나리오")
        void hotelLifecycleScenario() {
            // 1. 호텔 생성
            HotelCreateRequest createRequest = new HotelCreateRequest();
            ReflectionTestUtils.setField(createRequest, "name", "시나리오 호텔");
            ReflectionTestUtils.setField(createRequest, "address", "서울시 종로구");
            ReflectionTestUtils.setField(createRequest, "rating", 5);

            ResponseEntity<HotelResponse> createResponse = restTemplate.postForEntity(
                    baseUrl,
                    new HttpEntity<>(createRequest),
                    HotelResponse.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            Long hotelId = createResponse.getBody().getHotelId();

            // 2. 생성된 호텔 조회
            ResponseEntity<HotelResponse> getResponse = restTemplate.getForEntity(
                    baseUrl + "/" + hotelId,
                    HotelResponse.class
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody().getHotelName()).isEqualTo("시나리오 호텔");

            // 3. 호텔 정보 수정
            HotelUpdateRequest updateRequest = new HotelUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "name", "수정된 시나리오 호텔");
            ReflectionTestUtils.setField(updateRequest, "address", "서울시 마포구");
            ReflectionTestUtils.setField(updateRequest, "rating", 4);

            ResponseEntity<HotelResponse> updateResponse = restTemplate.exchange(
                    baseUrl + "/" + hotelId,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    HotelResponse.class
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody().getHotelName()).isEqualTo("수정된 시나리오 호텔");

            // 4. 호텔 상태를 중지로 변경
            HotelStatusUpdateRequest statusRequest = new HotelStatusUpdateRequest();
            ReflectionTestUtils.setField(statusRequest, "status", Status.UNDER_MAINTENANCE);

            ResponseEntity<HotelResponse> statusResponse = restTemplate.exchange(
                    baseUrl + "/" + hotelId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(statusRequest),
                    HotelResponse.class
            );

            assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(statusResponse.getBody().getStatus()).isEqualTo(Status.UNDER_MAINTENANCE);

            // 5. 호텔 삭제 (논리적 삭제)
            ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                    baseUrl + "/" + hotelId,
                    HttpMethod.DELETE,
                    null,
                    Void.class
            );

            assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // 6. 삭제 후 조회하여 상태 확인
            ResponseEntity<HotelResponse> finalGetResponse = restTemplate.getForEntity(
                    baseUrl + "/" + hotelId,
                    HotelResponse.class
            );

            assertThat(finalGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(finalGetResponse.getBody().getStatus()).isEqualTo(Status.INACTIVE);
        }
    }
}

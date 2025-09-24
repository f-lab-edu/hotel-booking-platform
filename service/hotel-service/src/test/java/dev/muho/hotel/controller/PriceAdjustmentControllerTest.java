package dev.muho.hotel.controller;

import dev.muho.hotel.domain.AdjustmentType;
import dev.muho.hotel.domain.CalculationType;
import dev.muho.hotel.domain.PriceAdjustment;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.PriceAdjustmentCreateRequest;
import dev.muho.hotel.dto.request.PriceAdjustmentUpdateRequest;
import dev.muho.hotel.dto.response.PriceAdjustmentResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriceAdjustmentControllerTest {

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

    private RatePlan defaultRatePlan;
    private PriceAdjustment defaultPriceAdjustment;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        // 기본 룸타입과 요금제 설정
        var roomType = testDataSetupService.setupRoomType();
        defaultRatePlan = testDataSetupService.setupRatePlan(roomType);

        // 기본 가격 조정 설정
        defaultPriceAdjustment = testDataSetupService.setupPriceAdjustment(defaultRatePlan);

        baseUrl = "http://localhost:" + port + "/api/v1/adjustments";
    }

    @Nested
    @DisplayName("가격 조정 조회 테스트")
    class GetPriceAdjustmentTests {

        @Test
        @DisplayName("ID로 가격 조정 조회 성공")
        void getPriceAdjustmentById_Success() {
            // given
            String url = baseUrl + "/" + defaultPriceAdjustment.getId();

            // when
            ResponseEntity<PriceAdjustmentResponse> response = restTemplate.getForEntity(url, PriceAdjustmentResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultPriceAdjustment.getId());
            assertThat(response.getBody().getName()).isEqualTo(defaultPriceAdjustment.getName());
            assertThat(response.getBody().getRatePlanId()).isEqualTo(defaultRatePlan.getId());
            assertThat(response.getBody().getAdjustmentType()).isEqualTo(defaultPriceAdjustment.getAdjustmentType());
            assertThat(response.getBody().getCalculationType()).isEqualTo(defaultPriceAdjustment.getCalculationType());
        }

        @Test
        @DisplayName("존재하지 않는 가격 조정 ID로 조회 시 404 반환")
        void getPriceAdjustmentById_NotFound() {
            // given
            String url = baseUrl + "/999";

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("요금제 ID로 가격 조정 목록 조회 성공")
        void getPriceAdjustmentsByRatePlanId_Success() {
            // given
            String url = baseUrl + "?hotelId=1&ratePlanId=" + defaultRatePlan.getId() + "&page=0&size=10";

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
            assertThat(response.getBody()).contains("\"ratePlanId\":" + defaultRatePlan.getId());
        }
    }

    @Nested
    @DisplayName("가격 조정 생성 테스트")
    class CreatePriceAdjustmentTests {

        @Test
        @DisplayName("가격 조정 생성 성공")
        void createPriceAdjustment_Success() {
            // given
            PriceAdjustmentCreateRequest request = new PriceAdjustmentCreateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "얼리버드 할인");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.FIXED_AMOUNT);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(50000));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 12, 31));
            ReflectionTestUtils.setField(request, "bookingDaysBeforeArrival", 30);

            HttpEntity<PriceAdjustmentCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<PriceAdjustmentResponse> response = restTemplate.postForEntity(baseUrl, entity, PriceAdjustmentResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getName()).isEqualTo("얼리버드 할인");
            assertThat(response.getBody().getRatePlanId()).isEqualTo(defaultRatePlan.getId());
            assertThat(response.getBody().getAdjustmentType()).isEqualTo(AdjustmentType.DISCOUNT);
            assertThat(response.getBody().getCalculationType()).isEqualTo(CalculationType.FIXED_AMOUNT);
            assertThat(response.getBody().getAmount()).isEqualTo(BigDecimal.valueOf(50000));
            assertThat(response.getBody().getId()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 요금제로 가격 조정 생성 시 404 반환")
        void createPriceAdjustment_RatePlanNotFound() {
            // given
            PriceAdjustmentCreateRequest request = new PriceAdjustmentCreateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", 999L);
            ReflectionTestUtils.setField(request, "name", "테스트 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(10.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 12, 31));

            HttpEntity<PriceAdjustmentCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("종료일이 시작일보다 이른 날짜로 생성 시 400 반환")
        void createPriceAdjustment_InvalidDateRange() {
            // given
            PriceAdjustmentCreateRequest request = new PriceAdjustmentCreateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "테스트 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(10.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 12, 31));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 1, 1)); // 종료일이 시작일보다 이름
            ReflectionTestUtils.setField(request, "bookingDaysBeforeArrival", 30);

            HttpEntity<PriceAdjustmentCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 데이터로 가격 조정 생성 시 400 반환")
        void createPriceAdjustment_BadRequest() {
            // given
            PriceAdjustmentCreateRequest request = new PriceAdjustmentCreateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(10.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 12, 31));

            HttpEntity<PriceAdjustmentCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("null 조정 금액으로 가격 조정 생성 시 400 반환")
        void createPriceAdjustment_NullAmount() {
            // given
            PriceAdjustmentCreateRequest request = new PriceAdjustmentCreateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "테스트 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", null); // null 금액
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 12, 31));

            HttpEntity<PriceAdjustmentCreateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(baseUrl, entity, ErrorResponse.class);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("가격 조정 수정 테스트")
    class UpdatePriceAdjustmentTests {

        @Test
        @DisplayName("가격 조정 정보 수정 성공")
        void updatePriceAdjustment_Success() {
            // given
            String url = baseUrl + "/" + defaultPriceAdjustment.getId();

            PriceAdjustmentUpdateRequest request = new PriceAdjustmentUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "수정된 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(15.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 6, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 9, 30));
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);
            ReflectionTestUtils.setField(request, "bookingDaysBeforeArrival", 14);

            HttpEntity<PriceAdjustmentUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<PriceAdjustmentResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    PriceAdjustmentResponse.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getId()).isEqualTo(defaultPriceAdjustment.getId());
            assertThat(response.getBody().getName()).isEqualTo("수정된 가격 조정");
            assertThat(response.getBody().getAdjustmentType()).isEqualTo(AdjustmentType.DISCOUNT);
            assertThat(response.getBody().getCalculationType()).isEqualTo(CalculationType.PERCENTAGE);
            assertThat(response.getBody().getAmount()).isEqualTo(BigDecimal.valueOf(15.0));
        }

        @Test
        @DisplayName("존재하지 않는 가격 조정 수정 시 404 반환")
        void updatePriceAdjustment_NotFound() {
            // given
            String url = baseUrl + "/999";

            PriceAdjustmentUpdateRequest request = new PriceAdjustmentUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "수정된 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(15.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 6, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 9, 30));
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<PriceAdjustmentUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("잘못된 데이터로 가격 조정 수정 시 400 반환")
        void updatePriceAdjustment_BadRequest() {
            // given
            String url = baseUrl + "/" + defaultPriceAdjustment.getId();

            PriceAdjustmentUpdateRequest request = new PriceAdjustmentUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", ""); // 빈 이름
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(15.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 6, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 9, 30));
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<PriceAdjustmentUpdateRequest> entity = new HttpEntity<>(request);

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

        @Test
        @DisplayName("종료일이 시작일보다 이른 날짜로 수정 시 400 반환")
        void updatePriceAdjustment_InvalidDateRange() {
            // given
            String url = baseUrl + "/" + defaultPriceAdjustment.getId();

            PriceAdjustmentUpdateRequest request = new PriceAdjustmentUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "name", "테스트 가격 조정");
            ReflectionTestUtils.setField(request, "adjustmentType", AdjustmentType.DISCOUNT);
            ReflectionTestUtils.setField(request, "calculationType", CalculationType.PERCENTAGE);
            ReflectionTestUtils.setField(request, "amount", BigDecimal.valueOf(15.0));
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2024, 9, 30));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2024, 6, 1)); // 종료일이 시작일보다 이름
            ReflectionTestUtils.setField(request, "status", Status.ACTIVE);

            HttpEntity<PriceAdjustmentUpdateRequest> entity = new HttpEntity<>(request);

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
}

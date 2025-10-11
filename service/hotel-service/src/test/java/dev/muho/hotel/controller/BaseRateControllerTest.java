package dev.muho.hotel.controller;

import dev.muho.hotel.domain.BaseRate;
import dev.muho.hotel.domain.RatePlan;
import dev.muho.hotel.domain.RoomType;
import dev.muho.hotel.dto.request.BaseRateBulkUpdateRequest;
import dev.muho.hotel.dto.response.BaseRateResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseRateControllerTest {

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
    private String bulkUpdateUrl;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();

        defaultRoomType = testDataSetupService.setupRoomType();
        defaultRatePlan = testDataSetupService.setupRatePlan(defaultRoomType);

        baseUrl = "http://localhost:" + port + "/api/v1/rate-plans";
        bulkUpdateUrl = "http://localhost:" + port + "/api/v1/base-rates/bulk-update";
    }

    @Nested
    @DisplayName("기본 요금 조회 테스트")
    class GetBaseRatesTests {

        @Test
        @DisplayName("기본 요금 조회 성공 - 기존 요금이 없는 경우")
        void getBaseRates_Success_WithNoExistingRates() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 3);

            String url = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, ratePlanId, startDate, endDate);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3); // startDate부터 endDate까지 3일

            // 모든 날짜의 가격이 null이어야 함 (기본 요금 없음)
            response.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isNull();
            });

            // 날짜 순서 확인
            List<BaseRateResponse> baseRates = response.getBody();
            assertThat(baseRates.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
            assertThat(baseRates.get(1).getDate()).isEqualTo(LocalDate.of(2025, 1, 2));
            assertThat(baseRates.get(2).getDate()).isEqualTo(LocalDate.of(2025, 1, 3));
        }

        @Test
        @DisplayName("기본 요금 조회 성공 - 기존 요금이 있는 경우")
        void getBaseRates_Success_WithExistingRates() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            LocalDate startDate = LocalDate.of(2025, 2, 1);
            LocalDate endDate = LocalDate.of(2025, 2, 3);

            // 먼저 기본 요금을 생성
            BaseRateBulkUpdateRequest createRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(createRequest, "ratePlanId", ratePlanId);
            ReflectionTestUtils.setField(createRequest, "startDate", startDate);
            ReflectionTestUtils.setField(createRequest, "endDate", startDate.plusDays(1)); // 2일만 생성
            ReflectionTestUtils.setField(createRequest, "price", BigDecimal.valueOf(150000));

            restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(createRequest),
                    Void.class
            );

            String url = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, ratePlanId, startDate, endDate);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);

            List<BaseRateResponse> baseRates = response.getBody();

            // 첫 번째와 두 번째 날짜는 기본 요금이 있어야 함
            assertThat(baseRates.get(0).getDate()).isEqualTo(LocalDate.of(2025, 2, 1));
            assertThat(baseRates.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150000));

            assertThat(baseRates.get(1).getDate()).isEqualTo(LocalDate.of(2025, 2, 2));
            assertThat(baseRates.get(1).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150000));

            // 세 번째 날짜는 기본 요금이 없어야 함
            assertThat(baseRates.get(2).getDate()).isEqualTo(LocalDate.of(2025, 2, 3));
            assertThat(baseRates.get(2).getPrice()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 요금제로 기본 요금 조회 시 404 반환")
        void getBaseRates_RatePlanNotFound() {
            // given
            Long ratePlanId = 999L;
            LocalDate startDate = LocalDate.of(2025, 1, 1);
            LocalDate endDate = LocalDate.of(2025, 1, 2);

            String url = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, ratePlanId, startDate, endDate);

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
        void getBaseRates_InvalidDateParameters() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            String url = String.format("%s/%d/base-rates?startDate=invalid-date&endDate=2025-01-02",
                    baseUrl, ratePlanId);

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
        void getBaseRates_MissingRequiredParameters() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            String url = String.format("%s/%d/base-rates", baseUrl, ratePlanId); // 파라미터 없음

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
        @DisplayName("시작 날짜가 종료 날짜보다 늦은 경우 400 반환")
        void getBaseRates_StartDateAfterEndDate() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            LocalDate startDate = LocalDate.of(2025, 1, 5);
            LocalDate endDate = LocalDate.of(2025, 1, 3);

            String url = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, ratePlanId, startDate, endDate);

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
        @DisplayName("단일 날짜 기본 요금 조회 성공")
        void getBaseRates_Success_SingleDate() {
            // given
            Long ratePlanId = defaultRatePlan.getId();
            LocalDate testDate = LocalDate.of(2025, 3, 15);

            String url = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, ratePlanId, testDate, testDate);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getDate()).isEqualTo(testDate);
            assertThat(response.getBody().get(0).getPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("기본 요금 대량 업데이트 테스트")
    class BulkUpdateBaseRatesTests {

        @Test
        @DisplayName("기본 요금 대량 업데이트 성공 - 새로운 요금 생성")
        void bulkUpdateBaseRates_Success_CreateNew() {
            // given
            LocalDate startDate = LocalDate.of(2025, 4, 1);
            LocalDate endDate = LocalDate.of(2025, 4, 3);

            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "startDate", startDate);
            ReflectionTestUtils.setField(request, "endDate", endDate);
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(200000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            response.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
            });

            // 추가 검증: 조회하여 확인
            String getUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), startDate, endDate);

            ResponseEntity<List<BaseRateResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(3);
            getResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
            });
        }

        @Test
        @DisplayName("기본 요금 대량 업데이트 성공 - 기존 요금 업데이트")
        void bulkUpdateBaseRates_Success_UpdateExisting() {
            // given
            LocalDate startDate = LocalDate.of(2025, 5, 1);
            LocalDate endDate = LocalDate.of(2025, 5, 2);

            // 먼저 기본 요금 생성
            BaseRateBulkUpdateRequest createRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(createRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(createRequest, "startDate", startDate);
            ReflectionTestUtils.setField(createRequest, "endDate", endDate);
            ReflectionTestUtils.setField(createRequest, "price", BigDecimal.valueOf(100000));

            restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(createRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            // 업데이트 요청
            BaseRateBulkUpdateRequest updateRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(updateRequest, "startDate", startDate);
            ReflectionTestUtils.setField(updateRequest, "endDate", endDate);
            ReflectionTestUtils.setField(updateRequest, "price", BigDecimal.valueOf(250000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(updateRequest);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            response.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250000));
            });

            // 추가 검증: 조회하여 확인
            String getUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), startDate, endDate);

            ResponseEntity<List<BaseRateResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(2);
            getResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250000));
            });
        }

        @Test
        @DisplayName("존재하지 않는 요금제로 대량 업데이트 시 404 반환")
        void bulkUpdateBaseRates_RatePlanNotFound() {
            // given
            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", 999L);
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2025, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2025, 1, 1));
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(100000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

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
        void bulkUpdateBaseRates_BadRequest() {
            // given
            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2025, 1, 1));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2025, 1, 1));
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(-1000)); // 음수 가격

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<String> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("시작 날짜가 종료 날짜보다 늦은 경우 400 반환")
        void bulkUpdateBaseRates_StartDateAfterEndDate() {
            // given
            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "startDate", LocalDate.of(2025, 1, 5));
            ReflectionTestUtils.setField(request, "endDate", LocalDate.of(2025, 1, 3));
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(100000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

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
        @DisplayName("단일 날짜 대량 업데이트 성공")
        void bulkUpdateBaseRates_Success_SingleDate() {
            // given
            LocalDate testDate = LocalDate.of(2025, 6, 15);

            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "startDate", testDate);
            ReflectionTestUtils.setField(request, "endDate", testDate);
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(300000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(300000));

            // 추가 검증: 조회하여 확인
            String getUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), testDate, testDate);

            ResponseEntity<List<BaseRateResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(1);
            assertThat(getResponse.getBody().get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        }

        @Test
        @DisplayName("장기간 대량 업데이트 성공")
        void bulkUpdateBaseRates_Success_LongPeriod() {
            // given
            LocalDate startDate = LocalDate.of(2025, 7, 1);
            LocalDate endDate = LocalDate.of(2025, 7, 30); // 30일간

            BaseRateBulkUpdateRequest request = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(request, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(request, "startDate", startDate);
            ReflectionTestUtils.setField(request, "endDate", endDate);
            ReflectionTestUtils.setField(request, "price", BigDecimal.valueOf(180000));

            HttpEntity<BaseRateBulkUpdateRequest> entity = new HttpEntity<>(request);

            // when
            ResponseEntity<List<BaseRateResponse>> response = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(30);
            response.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(180000));
            });

            // 일부 날짜만 추가 검증
            String getUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), startDate, startDate.plusDays(2));

            ResponseEntity<List<BaseRateResponse>> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResponse.getBody()).hasSize(3);
            getResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(180000));
            });
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("기본 요금 조회 -> 대량 업데이트 -> 재조회 시나리오")
        void baseRateLifecycleScenario() {
            // 1. 초기 기본 요금 조회 (기존 요금 없는 미래 날짜)
            LocalDate startDate = LocalDate.of(2025, 8, 1);
            LocalDate endDate = LocalDate.of(2025, 8, 5);

            String getUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), startDate, endDate);

            ResponseEntity<List<BaseRateResponse>> initialResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(initialResponse.getBody()).hasSize(5);
            initialResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isNull();
            });

            // 2. 기본 요금 대량 생성
            BaseRateBulkUpdateRequest createRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(createRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(createRequest, "startDate", startDate);
            ReflectionTestUtils.setField(createRequest, "endDate", endDate);
            ReflectionTestUtils.setField(createRequest, "price", BigDecimal.valueOf(120000));

            ResponseEntity<List<BaseRateResponse>> createResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(createRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(createResponse.getBody()).hasSize(5);
            createResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));
            });

            // 3. 생성 후 기본 요금 조회
            ResponseEntity<List<BaseRateResponse>> afterCreateResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(afterCreateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            afterCreateResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));
            });

            // 4. 기본 요금 수정 (일부 기간만)
            BaseRateBulkUpdateRequest updateRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(updateRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(updateRequest, "startDate", startDate.plusDays(1));
            ReflectionTestUtils.setField(updateRequest, "endDate", startDate.plusDays(3));
            ReflectionTestUtils.setField(updateRequest, "price", BigDecimal.valueOf(160000));

            ResponseEntity<List<BaseRateResponse>> updateResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(updateRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updateResponse.getBody()).hasSize(3); // 업데이트된 3일간의 요금
            updateResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(160000));
            });

            // 5. 최종 기본 요금 조회 및 확인
            ResponseEntity<List<BaseRateResponse>> finalResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    }
            );

            assertThat(finalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<BaseRateResponse> finalBaseRates = finalResponse.getBody();

            // 첫째 날과 마지막 날은 120,000원
            assertThat(finalBaseRates.get(0).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));
            assertThat(finalBaseRates.get(4).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(120000));

            // 중간 3일은 160,000원
            assertThat(finalBaseRates.get(1).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(160000));
            assertThat(finalBaseRates.get(2).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(160000));
            assertThat(finalBaseRates.get(3).getPrice()).isEqualByComparingTo(BigDecimal.valueOf(160000));
        }

        @Test
        @DisplayName("여러 요금제의 기본 요금 관리 시나리오")
        void multipleRatePlanBaseRateScenario() {
            // 추가 요금제 생성
            RatePlan secondRatePlan = testDataSetupService.setupRatePlan(defaultRoomType);
            ReflectionTestUtils.setField(secondRatePlan, "name", "프리미엄 요금제");

            LocalDate testDate = LocalDate.of(2025, 9, 1);

            // 첫 번째 요금제 기본 요금 설정
            BaseRateBulkUpdateRequest firstRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(firstRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(firstRequest, "startDate", testDate);
            ReflectionTestUtils.setField(firstRequest, "endDate", testDate.plusDays(1));
            ReflectionTestUtils.setField(firstRequest, "price", BigDecimal.valueOf(100000));

            ResponseEntity<List<BaseRateResponse>> firstUpdateResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(firstRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(firstUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(firstUpdateResponse.getBody()).hasSize(2);

            // 두 번째 요금제 기본 요금 설정
            BaseRateBulkUpdateRequest secondRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(secondRequest, "ratePlanId", secondRatePlan.getId());
            ReflectionTestUtils.setField(secondRequest, "startDate", testDate);
            ReflectionTestUtils.setField(secondRequest, "endDate", testDate.plusDays(1));
            ReflectionTestUtils.setField(secondRequest, "price", BigDecimal.valueOf(200000));

            ResponseEntity<List<BaseRateResponse>> secondUpdateResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(secondRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(secondUpdateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(secondUpdateResponse.getBody()).hasSize(2);

            // 각각의 기본 요금 조회 및 확인
            String firstUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), testDate, testDate.plusDays(1));
            String secondUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, secondRatePlan.getId(), testDate, testDate.plusDays(1));

            ResponseEntity<List<BaseRateResponse>> firstResponse = restTemplate.exchange(
                    firstUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    });

            ResponseEntity<List<BaseRateResponse>> secondResponse = restTemplate.exchange(
                    secondUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    });

            assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            firstResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100000));
            });

            secondResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
            });
        }

        @Test
        @DisplayName("계절별 기본 요금 설정 시나리오")
        void seasonalBaseRateScenario() {
            LocalDate springStart = LocalDate.of(2025, 3, 1);
            LocalDate springEnd = LocalDate.of(2025, 5, 31);
            LocalDate summerStart = LocalDate.of(2025, 6, 1);
            LocalDate summerEnd = LocalDate.of(2025, 8, 31);

            // 봄 시즌 기본 요금 설정
            BaseRateBulkUpdateRequest springRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(springRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(springRequest, "startDate", springStart);
            ReflectionTestUtils.setField(springRequest, "endDate", springEnd);
            ReflectionTestUtils.setField(springRequest, "price", BigDecimal.valueOf(130000));

            ResponseEntity<List<BaseRateResponse>> springResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(springRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(springResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(springResponse.getBody()).isNotNull();
            // 3월 1일부터 5월 31일까지: 92일 (3월 31일 + 4월 30일 + 5월 31일)
            assertThat(springResponse.getBody()).hasSize(92);

            // 여름 성수기 기본 요금 설정 (더 높은 가격)
            BaseRateBulkUpdateRequest summerRequest = new BaseRateBulkUpdateRequest();
            ReflectionTestUtils.setField(summerRequest, "ratePlanId", defaultRatePlan.getId());
            ReflectionTestUtils.setField(summerRequest, "startDate", summerStart);
            ReflectionTestUtils.setField(summerRequest, "endDate", summerEnd);
            ReflectionTestUtils.setField(summerRequest, "price", BigDecimal.valueOf(200000));

            ResponseEntity<List<BaseRateResponse>> summerResponse = restTemplate.exchange(
                    bulkUpdateUrl,
                    HttpMethod.PUT,
                    new HttpEntity<>(summerRequest),
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {}
            );

            assertThat(summerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(summerResponse.getBody()).isNotNull();
            // 6월 1일부터 8월 31일까지: 92일 (6월 30일 + 7월 31일 + 8월 31일)
            assertThat(summerResponse.getBody()).hasSize(92);

            // 봄 시즌 요금 확인 (일부 날짜만)
            String springUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), springStart, springStart.plusDays(2));

            ResponseEntity<List<BaseRateResponse>> springCheckResponse = restTemplate.exchange(
                    springUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    });

            assertThat(springCheckResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            springCheckResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(130000));
            });

            // 여름 시즌 요금 확인 (일부 날짜만)
            String summerUrl = String.format("%s/%d/base-rates?startDate=%s&endDate=%s",
                    baseUrl, defaultRatePlan.getId(), summerStart, summerStart.plusDays(2));

            ResponseEntity<List<BaseRateResponse>> summerCheckResponse = restTemplate.exchange(
                    summerUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<BaseRateResponse>>() {
                    });

            assertThat(summerCheckResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            summerCheckResponse.getBody().forEach(baseRate -> {
                assertThat(baseRate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
            });
        }
    }
}


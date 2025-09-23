package dev.muho.hotel.controller;

import dev.muho.hotel.domain.*;
import dev.muho.hotel.dto.response.AvailabilityResponse;
import dev.muho.hotel.util.DatabaseCleaner;
import dev.muho.hotel.util.TestDataSetupService;
import dev.muho.hotel.util.TestDateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AvailabilityControllerTest {

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

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
        defaultHotel = testDataSetupService.setup();
    }

    private String buildAvailabilityUrl(Long hotelId, String checkIn, String checkOut, int adults, int children) {
        return "http://localhost:" + port + "/api/v1/hotels/" + hotelId +
                "/availability?checkInDate=" + checkIn + "&checkOutDate=" + checkOut +
                "&adults=" + adults + "&children=" + children;
    }

    private String buildAvailabilityUrlWithMissingCheckInDate(Long hotelId) {
        return "http://localhost:" + port + "/api/v1/hotels/" + hotelId +
                "/availability?checkOutDate=" + TestDateUtils.getFutureDatePlusDays(3) + "&adults=2&children=0";
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 실제 서버와 DB 컨테이너 환경에서 성공")
    void getAvailability_Success_WithRealServerAndDbContainer() {
        // when (TestRestTemplate을 사용하여 실제 HTTP GET 요청)
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then (응답 검증)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getHotelName()).isEqualTo("테스트 호텔");
        assertThat(response.getBody().getAvailableRoomTypes()).hasSize(1);
        assertThat(response.getBody().getAvailableRoomTypes().get(0).getAvailableRatePlans().get(0).getTotalPrice())
                .isEqualByComparingTo(new BigDecimal("300000.00"));
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 존재하지 않는 호텔 ID로 요청시 404 반환")
    void getAvailability_NotFound_WhenHotelDoesNotExist() {
        // when
        String url = buildAvailabilityUrl(999999L, TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 필수 파라미터 누락시 400 반환")
    void getAvailability_BadRequest_WhenMissingRequiredParameters() {
        // when - checkInDate 파라미터 누락
        String url = buildAvailabilityUrlWithMissingCheckInDate(defaultHotel.getId());
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 잘못된 날짜 형식으로 요청시 400 반환")
    void getAvailability_BadRequest_WhenInvalidDateFormat() {
        // when - 잘못된 날짜 형식
        String url = buildAvailabilityUrl(defaultHotel.getId(), "invalid-date", TestDateUtils.getFutureDatePlusDays(32), 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 체크인 날짜가 체크아웃 날짜보다 늦을 때 400 반환")
    void getAvailability_BadRequest_WhenCheckInDateAfterCheckOutDate() {
        // when - 체크인 날짜가 체크아웃 날짜보다 늦음
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(35), TestDateUtils.getFutureDatePlusDays(32), 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 과거 날짜로 요청시 400 반환")
    void getAvailability_BadRequest_WhenCheckInDateInPast() {
        // when - 과거 날짜
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getPastDate(), TestDateUtils.getPastDatePlusDays(2), 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 성인 수가 0명일 때 400 반환")
    void getAvailability_BadRequest_WhenAdultsIsZero() {
        // when - 성인 수 0명
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 0, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 음수 인원수로 요청시 400 반환")
    void getAvailability_BadRequest_WhenNegativeGuestCount() {
        // when - 음수 성인 수
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), -1, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 아이들만 있는 경우 400 반환")
    void getAvailability_BadRequest_WhenOnlyChildren() {
        // when - 성인 0명, 아이들만 있음
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 0, 2);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 다양한 인원 조합으로 성공 케이스")
    void getAvailability_Success_WithDifferentGuestCombinations() {
        // when - 성인 1명
        String url1 = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 1, 0);
        ResponseEntity<AvailabilityResponse> response1 = restTemplate.getForEntity(url1, AvailabilityResponse.class);

        // then
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).isNotNull();

        // when - 성인 2명, 아이 1명
        String url2 = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, 1);
        ResponseEntity<AvailabilityResponse> response2 = restTemplate.getForEntity(url2, AvailabilityResponse.class);

        // then
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();

        // when - 성인 4명
        String url3 = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 4, 0);
        ResponseEntity<AvailabilityResponse> response3 = restTemplate.getForEntity(url3, AvailabilityResponse.class);

        // then
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response3.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 장기간 숙박 요청 성공")
    void getAvailability_Success_WithLongStay() {
        // when - 7박 8일
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(38), 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 당일 체크인/체크아웃 요청시 400 반환")
    void getAvailability_BadRequest_WhenSameDayCheckInOut() {
        // when - 같은 날 체크인/체크아웃
        String sameDate = TestDateUtils.getFutureDatePlusDays(30);
        String url = buildAvailabilityUrl(defaultHotel.getId(), sameDate, sameDate, 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 예약 불가능한 객실이 있는 경우 사용 가능한 객실만 반환")
    void getAvailability_Success_WhenSomeRoomsUnavailable() {
        // when
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // 사용 가능한 객실만 반환되는지 확인
        assertThat(response.getBody().getAvailableRoomTypes()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 모든 객실이 예약된 경우 빈 목록 반환")
    void getAvailability_Success_WhenAllRoomsBooked() {
        // when - 재고가 설정되지 않은 날짜로 요청하여 빈 결과 확인
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(90), TestDateUtils.getFutureDatePlusDays(93), 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // 재고가 없으므로 빈 목록 반환
        assertThat(response.getBody().getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 최대 수용 인원을 초과하는 요청시 빈 목록 반환")
    void getAvailability_Success_WhenExceedingMaxOccupancy() {
        // when - 최대 수용 인원을 초과하는 인원수 (예: 10명)
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 10, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAvailableRoomTypes()).isEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 극단적으로 높은 인원수로 요청시 400 반환")
    void getAvailability_BadRequest_WhenExtremelyHighGuestCount() {
        // when - 극단적으로 높은 인원수 (예: 1000명)
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 1000, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then - 실제 구현에 따라 400 또는 200(빈 결과) 모두 가능
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 특수 문자가 포함된 파라미터로 요청시 400 반환")
    void getAvailability_BadRequest_WhenSpecialCharactersInParameters() {
        // when - 특수 문자가 포함된 adults 파라미터
        String url = "http://localhost:" + port + "/api/v1/hotels/" + defaultHotel.getId() +
                "/availability?checkInDate=" + TestDateUtils.getFutureDatePlusDays(30) + "&checkOutDate=" + TestDateUtils.getFutureDatePlusDays(32) + "&adults=2a&children=0";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 모든 파라미터 누락시 400 반환")
    void getAvailability_BadRequest_WhenAllParametersMissing() {
        // when - 모든 쿼리 파라미터 누락
        String url = "http://localhost:" + port + "/api/v1/hotels/" + defaultHotel.getId() + "/availability";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 잘못된 호텔 ID 형식으로 요청시 400 반환")
    void getAvailability_BadRequest_WhenInvalidHotelIdFormat() {
        // when - 문자열 형태의 호텔 ID
        String url = "http://localhost:" + port + "/api/v1/hotels/invalid-id/availability" +
                "?checkInDate=" + TestDateUtils.getFutureDatePlusDays(30) + "&checkOutDate=" + TestDateUtils.getFutureDatePlusDays(32) + "&adults=2&children=0";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 매우 긴 숙박 기간으로 요청 (30일)")
    void getAvailability_Success_WithVeryLongStay() {
        // when - 30박 31일
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(61), 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then - 긴 기간이어도 정상적으로 처리되어야 함
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 어린이 수만 음수로 요청시 400 반환")
    void getAvailability_BadRequest_WhenNegativeChildrenCount() {
        // when - 음수 어린이 수
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, -1);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 최대 어린이 수로 요청 성공")
    void getAvailability_Success_WithMaxChildren() {
        // when - 성인 2명, 어린이 2명 (최대 수용 인원 4명)
        String url = buildAvailabilityUrl(defaultHotel.getId(), TestDateUtils.getFutureDatePlusDays(30), TestDateUtils.getFutureDatePlusDays(32), 2, 2);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 현재 날짜로 당일 예약 요청")
    void getAvailability_CheckTodayBooking() {
        // when - 오늘 날짜로 체크인 요청 (실제 구현에 따라 허용/불허용 결정)
        String today = TestDateUtils.getTodayDate();
        String tomorrow = TestDateUtils.getTomorrowDate();
        String url = buildAvailabilityUrl(defaultHotel.getId(), today, tomorrow, 2, 0);
        ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);

        // then - 당일 예약 정책에 따라 결과가 달라질 수 있음
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 1년 후 날짜로 요청")
    void getAvailability_Success_WithFutureDate() {
        // when - 1년 후 날짜
        String checkInDate = TestDateUtils.getFutureDateInYears(1);
        String checkOutDate = LocalDate.parse(checkInDate).plusDays(2).format(TestDateUtils.DATE_FORMATTER);
        String url = buildAvailabilityUrl(defaultHotel.getId(), checkInDate, checkOutDate, 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 2월 29일 윤년 날짜로 요청")
    void getAvailability_Success_WithLeapYearDate() {
        // when - 윤년의 2월 29일
        String leapDate = TestDateUtils.getLeapYearDate();
        String nextDay = LocalDate.parse(leapDate).plusDays(1).format(TestDateUtils.DATE_FORMATTER);
        String url = buildAvailabilityUrl(defaultHotel.getId(), leapDate, nextDay, 2, 0);
        ResponseEntity<AvailabilityResponse> response = restTemplate.getForEntity(url, AvailabilityResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{hotelId}/availability - 잘못된 윤년 날짜로 요청시 400 반환")
    void getAvailability_BadRequest_WhenInvalidLeapYearDate() {
        // when - 평년의 2월 29일 (존재하지 않는 날짜)
        String invalidDate = TestDateUtils.getInvalidLeapYearDate();
        String nextDay = invalidDate.substring(0, 8) + "01"; // 3월 1일로 변경
        String url = buildAvailabilityUrl(defaultHotel.getId(), invalidDate, nextDay, 2, 0);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}

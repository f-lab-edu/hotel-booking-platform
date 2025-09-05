package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.rateplan.FakeRatePlanRepository;
import dev.muho.hotel.domain.rateplan.RatePlanController;
import dev.muho.hotel.domain.rateplan.dto.RatePlanCreateRequest;
import dev.muho.hotel.domain.rateplan.dto.RatePlanUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatePlanController.class)
@Import(FakeDBConfiguration.class)
public class RatePlanE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeRatePlanRepository ratePlanRepository;

    @BeforeEach
    void setUp() {
        ratePlanRepository.clear();
    }

    @Test
    void 플랜_목록_조회_성공() throws Exception {
        // 여러 플랜 생성
        RatePlanCreateRequest ratePlan1 = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        RatePlanCreateRequest ratePlan2 = RatePlanCreateRequest.builder()
                .name("Deluxe Plan")
                .description("A deluxe room with premium amenities.")
                .basePrice(BigDecimal.valueOf(20))
                .breakfastIncluded(true)
                .refundable(false)
                .minNights(2)
                .maxNights(15)
                .build();
        ratePlanRepository.save(1L, ratePlan1);
        ratePlanRepository.save(1L, ratePlan2);

        // 플랜 목록 조회 API 호출 및 검증
        mockMvc.perform(get("/v1/room-types/1/rate-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content.[0].name").value("Standard Plan"))
                .andExpect(jsonPath("$.content.[1].name").value("Deluxe Plan"));
    }

    @Test
    void 플랜_빈_목록_조회_성공() throws Exception {
        // 플랜 목록 조회 API 호출 및 검증
        mockMvc.perform(get("/v1/room-types/1/rate-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void 플랜_조회_성공() throws Exception {
        // 플랜 생성
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        var savedRatePlan = ratePlanRepository.save(1L, ratePlan);

        // 플랜 단건 조회 API 호출 및 검증
        mockMvc.perform(get("/v1/room-types/1/rate-plans/" + savedRatePlan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard Plan"))
                .andExpect(jsonPath("$.description").value("A standard room with basic amenities."))
                .andExpect(jsonPath("$.basePrice").value(10))
                .andExpect(jsonPath("$.breakfastIncluded").value(false))
                .andExpect(jsonPath("$.refundable").value(true))
                .andExpect(jsonPath("$.minNights").value(1))
                .andExpect(jsonPath("$.maxNights").value(10));
    }

    @Test
    void 플랜_조회_실패_존재하지_않는_플랜() throws Exception {
        // 플랜 단건 조회 API 호출 및 검증
        mockMvc.perform(get("/v1/room-types/1/rate-plans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 플랜_생성_성공() throws Exception {
        // 플랜 생성 요청 객체
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        String createJson = objectMapper.writeValueAsString(ratePlan);

        // 플랜 생성 API 호출 및 검증
        mockMvc.perform(post("/v1/room-types/1/rate-plans")
                        .contentType("application/json")
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Standard Plan"))
                .andExpect(jsonPath("$.description").value("A standard room with basic amenities."))
                .andExpect(jsonPath("$.basePrice").value(10))
                .andExpect(jsonPath("$.breakfastIncluded").value(false))
                .andExpect(jsonPath("$.refundable").value(true))
                .andExpect(jsonPath("$.minNights").value(1))
                .andExpect(jsonPath("$.maxNights").value(10));
    }

    @Test
    void 플랜_생성_실패_잘못된_입력값() throws Exception {
        // 플랜 생성 요청 객체 (잘못된 입력값)
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("") // 이름이 빈 문자열
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.valueOf(-10)) // 음수 가격
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(0) // 0 이하
                .maxNights(-5) // 음수
                .build();
        String createJson = objectMapper.writeValueAsString(ratePlan);

        // 플랜 생성 API 호출 및 검증
        mockMvc.perform(post("/v1/room-types/1/rate-plans")
                        .contentType("application/json")
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("요금제 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.basePrice").value("기본 가격은 0 이상이어야 합니다."))
                .andExpect(jsonPath("$.validation.minNights").value("최소 숙박일은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.validation.maxNights").value("최대 숙박일은 1 이상이어야 합니다."));
    }

    @Test
    void 플랜_수정_성공() throws Exception {
        // 플랜 생성
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        var savedRatePlan = ratePlanRepository.save(1L, ratePlan);

        // 플랜 수정 요청 객체
        var ratePlanUpdateRequest = RatePlanUpdateRequest.builder()
                .name("Updated Standard Plan")
                .description("An updated standard room with better amenities.")
                .basePrice(BigDecimal.valueOf(15))
                .breakfastIncluded(true)
                .refundable(false)
                .minNights(2)
                .maxNights(12)
                .build();
        String updateJson = objectMapper.writeValueAsString(ratePlanUpdateRequest);

        // 플랜 수정 API 호출 및 검증
        mockMvc.perform(put("/v1/room-types/1/rate-plans/" + savedRatePlan.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRatePlan.getId()))
                .andExpect(jsonPath("$.name").value("Updated Standard Plan"))
                .andExpect(jsonPath("$.description").value("An updated standard room with better amenities."))
                .andExpect(jsonPath("$.basePrice").value(15))
                .andExpect(jsonPath("$.breakfastIncluded").value(true))
                .andExpect(jsonPath("$.refundable").value(false))
                .andExpect(jsonPath("$.minNights").value(2))
                .andExpect(jsonPath("$.maxNights").value(12));
    }

    @Test
    void 플랜_수정_실패_존재하지_않는_플랜() throws Exception {
        // 플랜 수정 요청 객체
        var ratePlanUpdateRequest = RatePlanUpdateRequest.builder()
                .name("Updated Standard Plan")
                .description("An updated standard room with better amenities.")
                .basePrice(BigDecimal.valueOf(15))
                .breakfastIncluded(true)
                .refundable(false)
                .minNights(2)
                .maxNights(12)
                .build();
        String updateJson = objectMapper.writeValueAsString(ratePlanUpdateRequest);

        // 플랜 수정 API 호출 및 검증
        mockMvc.perform(put("/v1/room-types/1/rate-plans/999")
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void 플랜_수정_실패_잘못된_입력값() throws Exception {
        // 플랜 생성
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        var savedRatePlan = ratePlanRepository.save(1L, ratePlan);

        // 플랜 수정 요청 객체 (잘못된 입력값)
        var ratePlanUpdateRequest = RatePlanUpdateRequest.builder()
                .name("") // 이름이 빈 문자열
                .description("An updated standard room with better amenities.")
                .basePrice(BigDecimal.valueOf(-15)) // 음수 가격
                .breakfastIncluded(true)
                .refundable(false)
                .minNights(0) // 0 이하
                .maxNights(-3) // 음수
                .build();
        String updateJson = objectMapper.writeValueAsString(ratePlanUpdateRequest);

        // 플랜 수정 API 호출 및 검증
        mockMvc.perform(put("/v1/room-types/1/rate-plans/" + savedRatePlan.getId())
                        .contentType("application/json")
                        .content(updateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("요금제 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.basePrice").value("기본 가격은 0 이상이어야 합니다."))
                .andExpect(jsonPath("$.validation.minNights").value("최소 숙박일은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.validation.maxNights").value("최대 숙박일은 1 이상이어야 합니다."));
    }

    @Test
    void 플랜_삭제_성공() throws Exception {
        // 플랜 생성
        RatePlanCreateRequest ratePlan = RatePlanCreateRequest.builder()
                .name("Standard Plan")
                .description("A standard room with basic amenities.")
                .basePrice(BigDecimal.TEN)
                .breakfastIncluded(false)
                .refundable(true)
                .minNights(1)
                .maxNights(10)
                .build();
        var savedRatePlan = ratePlanRepository.save(1L, ratePlan);

        // 플랜 삭제 API 호출 및 검증
        mockMvc.perform(delete("/v1/room-types/1/rate-plans/" + savedRatePlan.getId()))
                .andExpect(status().isNoContent());

        // 삭제된 플랜 조회 시도 및 검증
        mockMvc.perform(get("/v1/room-types/1/rate-plans/" + savedRatePlan.getId()))
                .andExpect(status().isNotFound());
    }
}

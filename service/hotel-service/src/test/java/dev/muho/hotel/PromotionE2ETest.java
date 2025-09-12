package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.promotion.DiscountType;
import dev.muho.hotel.domain.promotion.FakePromotionRepository;
import dev.muho.hotel.domain.promotion.PromotionController;
import dev.muho.hotel.domain.promotion.dto.PromotionCreateRequest;
import dev.muho.hotel.domain.promotion.dto.PromotionResponse;
import dev.muho.hotel.domain.promotion.dto.PromotionUpdateRequest;
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

@WebMvcTest(PromotionController.class)
@Import(FakeDBConfiguration.class)
public class PromotionE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakePromotionRepository promotionRepository;

    @BeforeEach
    void setUp() {
        promotionRepository.clear();
    }

    @Test
    void 프로모션_목록_조회_성공() throws Exception {
        // 여러 프로모션 생성
        PromotionCreateRequest request1 = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        PromotionCreateRequest request2 = PromotionCreateRequest.builder()
                .name("Winter Sale")
                .description("15% off for winter bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(-10))
                .minNights(2)
                .build();
        promotionRepository.save(request1);
        promotionRepository.save(request2);

        mockMvc.perform(get("/v1/promotions"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content.[0].name").value("Summer Sale"))
                .andExpect(jsonPath("$.content.[1].name").value("Winter Sale"));
    }

    @Test
    void 프로모션_조회_성공() throws Exception {
        PromotionCreateRequest request = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        PromotionResponse createdPromotion = promotionRepository.save(request);

        mockMvc.perform(get("/v1/promotions/1"))
                .andExpect(jsonPath("$.name").value("Summer Sale"))
                .andExpect(jsonPath("$.description").value("20% off for summer bookings"));
    }

    @Test
    void 프로모션_조회_실패_존재하지_않는_프로모션() throws Exception {
        mockMvc.perform(get("/v1/promotions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 프로모션_생성_성공() throws Exception {
        PromotionCreateRequest request = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/promotions")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Summer Sale"))
                .andExpect(jsonPath("$.applicableHotelId").value(1L))
                .andExpect(jsonPath("$.discountType").value("FIXED_AMOUNT"))
                .andExpect(jsonPath("$.description").value("20% off for summer bookings"));
    }

    @Test
    void 프로모션_생성_실패_잘못된_입력값() throws Exception {
        PromotionCreateRequest request = PromotionCreateRequest.builder()
                .name("") // 빈 이름
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountValue(BigDecimal.valueOf(-1))
                .minNights(1)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/promotions")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("프로모션 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.discountType").value("할인 유형은 필수입니다."))
                .andExpect(jsonPath("$.validation.discountValue").value("할인 값은 양수여야 합니다."));
    }

    @Test
    void 프로모션_수정_성공() throws Exception {
        PromotionCreateRequest createRequest = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        PromotionResponse createdPromotion = promotionRepository.save(createRequest);

        PromotionUpdateRequest updateRequest = PromotionUpdateRequest.builder()
                .name("Updated Summer Sale")
                .description("25% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .minNights(2)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/promotions/1")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Summer Sale"))
                .andExpect(jsonPath("$.description").value("25% off for summer bookings"))
                .andExpect(jsonPath("$.discountType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.discountValue").value(20));
    }

    @Test
    void 프로모션_수정_실패_존재하지_않는_플랜() throws Exception {
        PromotionUpdateRequest updateRequest = PromotionUpdateRequest.builder()
                .name("Updated Summer Sale")
                .description("25% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .minNights(2)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/promotions/999")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void 프로모션_수정_실패_잘못된_입력값() throws Exception {
        PromotionCreateRequest createRequest = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        PromotionResponse createdPromotion = promotionRepository.save(createRequest);

        PromotionUpdateRequest updateRequest = PromotionUpdateRequest.builder()
                .name("") // 빈 이름
                .description("25% off for summer bookings")
                .applicableHotelId(1L)
                .discountValue(BigDecimal.valueOf(-20)) // 음수 할인 값
                .minNights(2)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/promotions/1")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("프로모션 이름은 필수입니다."))
                .andExpect(jsonPath("$.validation.discountType").value("할인 유형은 필수입니다."))
                .andExpect(jsonPath("$.validation.discountValue").value("할인 값은 양수여야 합니다."));
    }

    @Test
    void 프로모션_삭제_성공() throws Exception {
        PromotionCreateRequest createRequest = PromotionCreateRequest.builder()
                .name("Summer Sale")
                .description("20% off for summer bookings")
                .applicableHotelId(1L)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.ONE)
                .minNights(1)
                .build();
        PromotionResponse createdPromotion = promotionRepository.save(createRequest);

        mockMvc.perform(delete("/v1/promotions/1"))
                .andExpect(status().isNoContent());

        // 삭제 후 조회 시도
        mockMvc.perform(get("/v1/promotions/1"))
                .andExpect(status().isNotFound());
    }
}

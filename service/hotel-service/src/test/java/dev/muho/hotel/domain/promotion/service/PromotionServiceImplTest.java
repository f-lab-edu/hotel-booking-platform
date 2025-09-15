package dev.muho.hotel.domain.promotion.service;

import dev.muho.hotel.domain.promotion.dto.command.PromotionCreateCommand;
import dev.muho.hotel.domain.promotion.dto.command.PromotionInfoResult;
import dev.muho.hotel.domain.promotion.dto.command.PromotionUpdateCommand;
import dev.muho.hotel.domain.promotion.entity.Promotion;
import dev.muho.hotel.domain.promotion.entity.DiscountType;
import dev.muho.hotel.domain.promotion.error.PromotionNotFoundException;
import dev.muho.hotel.domain.promotion.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    PromotionRepository promotionRepository;

    @InjectMocks
    PromotionServiceImpl service;

    @Test
    @DisplayName("create: 저장 및 결과 매핑")
    void create_success() {
        PromotionCreateCommand cmd = new PromotionCreateCommand(
                1L,
                2L,
                3L,
                "P1",
                "d",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10),
                1,
                0,
                30
        );
        given(promotionRepository.save(any(Promotion.class))).willAnswer(inv -> inv.getArgument(0));

        PromotionInfoResult res = service.create(cmd);

        assertThat(res.name()).isEqualTo("P1");
    }

    @Test
    @DisplayName("findById: 존재하지 않음 예외")
    void findById_notFound() {
        given(promotionRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(PromotionNotFoundException.class);
    }

    @Test
    @DisplayName("findById: 성공 매핑")
    void findById_success() {
        Promotion p = Promotion.builder().id(5L).applicableHotelId(1L).name("Promo").build();
        given(promotionRepository.findById(5L)).willReturn(Optional.of(p));

        PromotionInfoResult res = service.findById(5L);
        assertThat(res.id()).isEqualTo(5L);
        assertThat(res.name()).isEqualTo("Promo");
    }

    @Test
    @DisplayName("update: repo.update 위임 및 결과 매핑")
    void update_success() {
        PromotionUpdateCommand cmd = new PromotionUpdateCommand(
                "U",
                "d",
                DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(5000),
                2,
                0,
                10
        );
        Promotion updated = Promotion.builder().id(10L).name("U").build();
        given(promotionRepository.update(eq(10L), any(Promotion.class))).willReturn(updated);

        PromotionInfoResult res = service.update(10L, cmd);
        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.name()).isEqualTo("U");
    }

    @Test
    @DisplayName("deleteById: repo 호출 위임")
    void deleteById() {
        service.deleteById(5L);
        verify(promotionRepository).deleteById(5L);
    }

    @Test
    @DisplayName("search: 페이지 매핑")
    void search_mapping() {
        Promotion p1 = Promotion.builder().id(1L).applicableHotelId(10L).name("A").build();
        Promotion p2 = Promotion.builder().id(2L).applicableHotelId(10L).name("B").build();
        Pageable pageable = PageRequest.of(0, 10);
        given(promotionRepository.search(10L, pageable)).willReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));

        var page = service.search(10L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(PromotionInfoResult::name)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("search: 빈 결과")
    void search_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        given(promotionRepository.search(99L, pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        var page = service.search(99L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(0);
    }
}

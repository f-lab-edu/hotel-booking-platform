package dev.muho.hotel.domain.rateplan.service;

import dev.muho.hotel.domain.rateplan.dto.command.RatePlanCreateCommand;
import dev.muho.hotel.domain.rateplan.dto.command.RatePlanInfoResult;
import dev.muho.hotel.domain.rateplan.entity.RatePlan;
import dev.muho.hotel.domain.rateplan.error.RatePlanNotFoundException;
import dev.muho.hotel.domain.rateplan.repository.RatePlanRepository;
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
class RatePlanServiceImplTest {

    @Mock
    RatePlanRepository repository;

    @InjectMocks
    RatePlanServiceImpl service;

    @Test
    @DisplayName("create: 저장 및 결과 매핑")
    void create_success() {
        RatePlanCreateCommand cmd = new RatePlanCreateCommand("RP", "d", BigDecimal.valueOf(100), true, false, 1, 5);
        given(repository.save(any(RatePlan.class))).willAnswer(inv -> inv.getArgument(0));

        RatePlanInfoResult res = service.create(200L, cmd);

        assertThat(res.name()).isEqualTo("RP");
    }

    @Test
    @DisplayName("create: minNights > maxNights 입력 시에도 저장 호출됨")
    void create_minGreaterThanMax_callsSave() {
        RatePlanCreateCommand cmd = new RatePlanCreateCommand("Bad", "d", BigDecimal.valueOf(50), false, true, 10, 2);
        given(repository.save(any(RatePlan.class))).willAnswer(inv -> inv.getArgument(0));

        RatePlanInfoResult res = service.create(201L, cmd);

        // 비즈니스 레이어가 별도 검증을 하지 않는 경우 저장은 호출되어 매핑 결과를 반환함
        assertThat(res.name()).isEqualTo("Bad");
        verify(repository).save(any(RatePlan.class));
    }

    @Test
    @DisplayName("findById: 존재하지 않음 예외")
    void findById_notFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(1L, 999L)).isInstanceOf(RatePlanNotFoundException.class);
    }

    @Test
    @DisplayName("findById: 존재하고 roomTypeId 불일치 시 예외")
    void findById_roomTypeMismatch() {
        RatePlan r = RatePlan.builder().id(3L).roomTypeId(20L).name("RP").build();
        given(repository.findById(3L)).willReturn(Optional.of(r));

        assertThatThrownBy(() -> service.findById(999L, 3L)).isInstanceOf(RatePlanNotFoundException.class);
    }

    @Test
    @DisplayName("update: repo.update 위임 및 결과 매핑")
    void update_success() {
        RatePlanCreateCommand cmd = new RatePlanCreateCommand("U", "d", BigDecimal.valueOf(150), false, true, 1, 3);
        RatePlan updated = RatePlan.builder().id(9L).roomTypeId(50L).name("U").build();
        given(repository.update(eq(9L), any(RatePlan.class))).willReturn(updated);

        RatePlanInfoResult res = service.update(50L, 9L, cmd);
        assertThat(res.id()).isEqualTo(9L);
        assertThat(res.name()).isEqualTo("U");
    }

    @Test
    @DisplayName("deleteById: repo 호출 위임")
    void deleteById() {
        service.deleteById(1L, 2L);
        verify(repository).deleteById(2L);
    }

    @Test
    @DisplayName("search: 페이지 매핑")
    void search_mapping() {
        RatePlan r1 = RatePlan.builder().id(1L).roomTypeId(10L).name("A").build();
        RatePlan r2 = RatePlan.builder().id(2L).roomTypeId(10L).name("B").build();
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.findByRoomTypeId(10L, pageable)).willReturn(new PageImpl<>(List.of(r1, r2), pageable, 2));

        var page = service.search(10L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(RatePlanInfoResult::name)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("search: 빈 결과 처리")
    void search_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.findByRoomTypeId(999L, pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        var page = service.search(999L, pageable);
        assertThat(page.getTotalElements()).isZero();
    }
}

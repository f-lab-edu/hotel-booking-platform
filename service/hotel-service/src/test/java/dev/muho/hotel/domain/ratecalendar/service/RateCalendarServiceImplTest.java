package dev.muho.hotel.domain.ratecalendar.service;

import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarCreateCommand;
import dev.muho.hotel.domain.ratecalendar.dto.command.RateCalendarInfoResult;
import dev.muho.hotel.domain.ratecalendar.entity.RateCalendar;
import dev.muho.hotel.domain.ratecalendar.entity.AdjustmentType;
import dev.muho.hotel.domain.ratecalendar.error.RateCalendarNotFoundException;
import dev.muho.hotel.domain.ratecalendar.repository.RateCalendarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateCalendarServiceImplTest {

    @Mock
    RateCalendarRepository repository;

    @InjectMocks
    RateCalendarServiceImpl service;

    @Test
    @DisplayName("create: 저장 및 결과 매핑")
    void create_success() {
        RateCalendarCreateCommand cmd = new RateCalendarCreateCommand(
                "RC",
                "description",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                AdjustmentType.PERCENTAGE,
                BigDecimal.valueOf(10)
        );
        given(repository.save(any(RateCalendar.class))).willAnswer(inv -> inv.getArgument(0));

        RateCalendarInfoResult res = service.create(11L, cmd);

        assertThat(res.name()).isEqualTo("RC");
    }

    @Test
    @DisplayName("findById: 존재하지 않음 예외")
    void findById_notFound() {
        given(repository.findById(777L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(1L, 777L)).isInstanceOf(RateCalendarNotFoundException.class);
    }

    @Test
    @DisplayName("findById: 성공과 호텔 권한 검증")
    void findById_success_and_invalidHotel() {
        RateCalendar r = RateCalendar.builder().id(5L).hotelId(20L).name("RC").build();
        given(repository.findById(5L)).willReturn(Optional.of(r));

        RateCalendarInfoResult res = service.findById(20L, 5L);
        assertThat(res.id()).isEqualTo(5L);

        assertThatThrownBy(() -> service.findById(999L, 5L)).isInstanceOf(RateCalendarNotFoundException.class);
    }

    @Test
    @DisplayName("update: repo.update 위임 및 결과 매핑")
    void update_success() {
        RateCalendarCreateCommand cmd = new RateCalendarCreateCommand(
                "U",
                "updated",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                Set.of(DayOfWeek.TUESDAY),
                AdjustmentType.FIXED_AMOUNT,
                BigDecimal.valueOf(5000)
        );
        RateCalendar updated = RateCalendar.builder().id(9L).hotelId(50L).name("U").build();
        given(repository.update(eq(9L), any(RateCalendar.class))).willReturn(updated);

        RateCalendarInfoResult res = service.update(50L, 9L, cmd);
        assertThat(res.id()).isEqualTo(9L);
        assertThat(res.name()).isEqualTo("U");
    }

    @Test
    @DisplayName("update: applicableDays 변경이 반영되는지")
    void update_applicableDays_changed() {
        RateCalendarCreateCommand cmd = new RateCalendarCreateCommand(
                "U2",
                "updated",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                AdjustmentType.FIXED_AMOUNT,
                BigDecimal.valueOf(1000)
        );
        RateCalendar updated = RateCalendar.builder().id(12L).hotelId(60L).name("U2").applicableDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)).build();
        given(repository.update(eq(12L), any(RateCalendar.class))).willReturn(updated);

        RateCalendarInfoResult res = service.update(60L, 12L, cmd);
        assertThat(res.id()).isEqualTo(12L);
        assertThat(res.applicableDays()).contains(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
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
        RateCalendar r1 = RateCalendar.builder().id(1L).hotelId(10L).name("A").build();
        RateCalendar r2 = RateCalendar.builder().id(2L).hotelId(10L).name("B").build();
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.searchByHotelId(10L, pageable)).willReturn(new PageImpl<>(List.of(r1, r2), pageable, 2));

        var page = service.search(10L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(RateCalendarInfoResult::name)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("search: 빈 결과 처리")
    void search_empty() {
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.searchByHotelId(999L, pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        var page = service.search(999L, pageable);
        assertThat(page.getTotalElements()).isZero();
    }
}

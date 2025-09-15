package dev.muho.hotel.domain.hotel.service;

import dev.muho.hotel.domain.hotel.dto.command.HotelCreateCommand;
import dev.muho.hotel.domain.hotel.dto.command.HotelInfoResult;
import dev.muho.hotel.domain.hotel.dto.command.HotelSearchCondition;
import dev.muho.hotel.domain.hotel.dto.command.HotelUpdateCommand;
import dev.muho.hotel.domain.hotel.entity.Hotel;
import dev.muho.hotel.domain.hotel.error.HotelNotFoundException;
import dev.muho.hotel.domain.hotel.repository.HotelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    HotelRepository hotelRepository;

    @InjectMocks
    HotelServiceImpl hotelService;

    @Test
    @DisplayName("create: 저장 및 결과 매핑")
    void create_success() {
        HotelCreateCommand cmd = new HotelCreateCommand("H1", "addr", "KR", "Seoul", 5, "desc", "010-1111-2222");
        ArgumentCaptor<Hotel> captor = ArgumentCaptor.forClass(Hotel.class);
        given(hotelRepository.save(any(Hotel.class))).willAnswer(inv -> inv.getArgument(0));

        HotelInfoResult res = hotelService.create(cmd);

        verify(hotelRepository).save(captor.capture());
        Hotel saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("H1");
        assertThat(res.name()).isEqualTo("H1");
    }

    @Test
    @DisplayName("create: 이름이 비어있으면 예외")
    void create_invalidName() {
        HotelCreateCommand cmd = new HotelCreateCommand("", "addr", "KR", "Seoul", 4, "d", "010");
        assertThatThrownBy(() -> hotelService.create(cmd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create: 등급 범위 초과 시 예외")
    void create_invalidRating() {
        HotelCreateCommand cmd = new HotelCreateCommand("Hbad", "addr", "KR", "Seoul", 6, "d", "010");
        assertThatThrownBy(() -> hotelService.create(cmd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("findById: 성공")
    void findById_success() {
        Hotel h = Hotel.createNew("Found", "addr", "KR", "Seoul", 4, "d", "010");
        h.setId(2L);
        given(hotelRepository.findById(2L)).willReturn(Optional.of(h));

        HotelInfoResult res = hotelService.findById(2L);
        assertThat(res.id()).isEqualTo(2L);
        assertThat(res.name()).isEqualTo("Found");
    }

    @Test
    @DisplayName("findById: 존재하지 않음 예외")
    void findById_notFound() {
        given(hotelRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> hotelService.findById(99L)).isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    @DisplayName("update: 정상 업데이트 매핑")
    void update_success() {
        HotelUpdateCommand cmd = new HotelUpdateCommand("H2", "addr2", "KR", "Busan", 4, "d2", "010-2222-3333");
        Hotel updated = Hotel.builder().id(7L).name("H2")
                .address("addr2").country("KR").city("Busan").rating(4)
                .description("d2").contactNumber("010-2222-3333")
                .build();
        given(hotelRepository.update(eq(7L), any(Hotel.class))).willReturn(updated);

        HotelInfoResult res = hotelService.update(7L, cmd);
        assertThat(res.id()).isEqualTo(7L);
        assertThat(res.name()).isEqualTo("H2");
    }

    @Test
    @DisplayName("update: 잘못된 등급 입력 시 예외")
    void update_invalidRating() {
        HotelUpdateCommand cmd = new HotelUpdateCommand("H2", "addr2", "KR", "Busan", 10, "d2", "010-2222-3333");
        assertThatThrownBy(() -> hotelService.update(7L, cmd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteById: repo 호출 위임")
    void deleteById() {
        hotelService.deleteById(5L);
        verify(hotelRepository).deleteById(5L);
    }

    @Test
    @DisplayName("search: 페이지 매핑 - 전체")
    void search_mapping_all() {
        Hotel h1 = Hotel.createNew("A", "addr", "KR", "Seoul", 5, "d", "010");
        Hotel h2 = Hotel.createNew("B", "addr", "KR", "Seoul", 4, "d", "010");
        Pageable pageable = PageRequest.of(0, 10);
        given(hotelRepository.search("", pageable)).willReturn(new PageImpl<>(List.of(h1, h2), pageable, 2));

        var page = hotelService.search(HotelSearchCondition.of(""), pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(HotelInfoResult::name)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("search: 이름 필터링")
    void search_withNameFilter() {
        Hotel h1 = Hotel.createNew("Alpha Hotel", "addr", "KR", "Seoul", 5, "d", "010");
        Pageable pageable = PageRequest.of(0, 10);
        given(hotelRepository.search("Alpha", pageable)).willReturn(new PageImpl<>(List.of(h1), pageable, 1));

        var page = hotelService.search(HotelSearchCondition.of("Alpha"), pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(HotelInfoResult::name)
                .containsExactly("Alpha Hotel");
    }
}

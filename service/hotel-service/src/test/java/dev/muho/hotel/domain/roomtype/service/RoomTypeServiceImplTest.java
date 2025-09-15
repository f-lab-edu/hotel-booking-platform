package dev.muho.hotel.domain.roomtype.service;

import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeCreateCommand;
import dev.muho.hotel.domain.roomtype.dto.command.RoomTypeInfoResult;
import dev.muho.hotel.domain.roomtype.entity.RoomType;
import dev.muho.hotel.domain.roomtype.error.RoomTypeNotFoundException;
import dev.muho.hotel.domain.roomtype.repository.RoomTypeRepository;
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
class RoomTypeServiceImplTest {

    @Mock
    RoomTypeRepository repository;

    @InjectMocks
    RoomTypeServiceImpl service;

    @Test
    @DisplayName("create: 저장 및 결과 매핑")
    void create_success() {
        RoomTypeCreateCommand cmd = new RoomTypeCreateCommand("Deluxe", "desc", 4, 2, "SEA", "KING");
        ArgumentCaptor<RoomType> captor = ArgumentCaptor.forClass(RoomType.class);
        given(repository.save(any(RoomType.class))).willAnswer(inv -> inv.getArgument(0));

        RoomTypeInfoResult res = service.create(10L, cmd);

        verify(repository).save(captor.capture());
        RoomType saved = captor.getValue();
        assertThat(saved.getHotelId()).isEqualTo(10L);
        assertThat(saved.getName()).isEqualTo("Deluxe");
        assertThat(res.name()).isEqualTo("Deluxe");
    }

    @Test
    @DisplayName("findById: 성공과 권한 검증")
    void findById_success_and_invalidHotel() {
        RoomType r = RoomType.builder().id(5L).hotelId(20L).name("RT").build();
        given(repository.findById(5L)).willReturn(Optional.of(r));

        RoomTypeInfoResult res = service.findById(20L, 5L);
        assertThat(res.id()).isEqualTo(5L);

        // 잘못된 hotelId로 조회 시 예외 발생
        assertThatThrownBy(() -> service.findById(999L, 5L)).isInstanceOf(RoomTypeNotFoundException.class);
    }

    @Test
    @DisplayName("update: repo.update 위임 및 결과 매핑")
    void update_success() {
        RoomTypeCreateCommand cmd = new RoomTypeCreateCommand("Updated", "d", 3, 2, "CITY", "TWIN");
        RoomType updated = RoomType.builder().id(7L).hotelId(30L).name("Updated").build();
        given(repository.update(eq(7L), any(RoomType.class))).willReturn(updated);

        RoomTypeInfoResult res = service.update(30L, 7L, cmd);
        assertThat(res.id()).isEqualTo(7L);
        assertThat(res.name()).isEqualTo("Updated");
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
        RoomType r1 = RoomType.builder().id(1L).hotelId(10L).name("A").build();
        RoomType r2 = RoomType.builder().id(2L).hotelId(10L).name("B").build();
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.findByHotelId(10L, pageable)).willReturn(new PageImpl<>(List.of(r1, r2), pageable, 2));

        var page = service.search(10L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(RoomTypeInfoResult::name)
                .containsExactlyInAnyOrder("A", "B");
    }
}

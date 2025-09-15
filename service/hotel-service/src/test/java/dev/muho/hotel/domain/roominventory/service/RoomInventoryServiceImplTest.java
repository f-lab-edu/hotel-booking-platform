package dev.muho.hotel.domain.roominventory.service;

import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryBulkUpdateCommand;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryUpdateCommand;
import dev.muho.hotel.domain.roominventory.dto.command.RoomInventoryInfoResult;
import dev.muho.hotel.domain.roominventory.entity.RoomInventory;
import dev.muho.hotel.domain.roominventory.repository.RoomInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RoomInventoryServiceImplTest {

    @Mock
    RoomInventoryRepository repository;

    @InjectMocks
    RoomInventoryServiceImpl service;

    @Test
    @DisplayName("search: 페이지 매핑")
    void search_mapping() {
        RoomInventory r1 = RoomInventory.of(100L, LocalDate.now(), 10, 5);
        RoomInventory r2 = RoomInventory.of(100L, LocalDate.now().plusDays(1), 10, 6);
        Pageable pageable = PageRequest.of(0, 10);
        given(repository.findByRoomTypeId(100L, pageable)).willReturn(new PageImpl<>(List.of(r1, r2), pageable, 2));

        var page = service.search(100L, pageable);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(RoomInventoryInfoResult::date)
                .containsExactlyInAnyOrder(r1.getDate(), r2.getDate());
    }

    @Test
    @DisplayName("bulkUpdate: startDate after endDate 예외")
    void bulkUpdate_invalidDates() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now();
        RoomInventoryBulkUpdateCommand cmd = new RoomInventoryBulkUpdateCommand(start, end, 10, 5);
        assertThatThrownBy(() -> service.bulkUpdate(100L, cmd)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("update: 기존 있으면 update 호출, 없으면 save 호출")
    void update_existing_and_create() {
        LocalDate date = LocalDate.now();
        RoomInventory existing = RoomInventory.of(200L, date, 8, 4);
        given(repository.findByRoomTypeIdAndDate(200L, date)).willReturn(Optional.of(existing));

        RoomInventoryUpdateCommand cmd = new RoomInventoryUpdateCommand(12, 6);
        service.update(200L, date, cmd);

        then(repository).should().update(existing.getId(), existing);

        // when not present -> save
        LocalDate d2 = date.plusDays(1);
        given(repository.findByRoomTypeIdAndDate(200L, d2)).willReturn(Optional.empty());
        service.update(200L, d2, cmd);
        then(repository).should().save(any(RoomInventory.class));
    }

    @Test
    @DisplayName("bulkUpdate: 모든 날짜 신규 생성")
    void bulkUpdate_allNew() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(2);
        // 모든 날짜에 대해 Optional.empty() 반환
        given(repository.findByRoomTypeIdAndDate(eq(300L), any(LocalDate.class))).willReturn(Optional.empty());

        RoomInventoryBulkUpdateCommand cmd = new RoomInventoryBulkUpdateCommand(start, end, 20, 18);
        service.bulkUpdate(300L, cmd);

        // 3일치 save 호출
        then(repository).should(times(3)).save(any(RoomInventory.class));
        then(repository).should(never()).update(anyLong(), any(RoomInventory.class));
    }

    @Test
    @DisplayName("bulkUpdate: 모든 날짜 기존 업데이트")
    void bulkUpdate_allExisting() {
        LocalDate start = LocalDate.now();
        LocalDate d1 = start;
        LocalDate d2 = start.plusDays(1);
        LocalDate d3 = start.plusDays(2);

        RoomInventory e1 = RoomInventory.of(400L, d1, 10, 5);
        RoomInventory e2 = RoomInventory.of(400L, d2, 10, 5);
        RoomInventory e3 = RoomInventory.of(400L, d3, 10, 5);

        // assign ids to simulate persisted entities
        e1.setId(401L);
        e2.setId(402L);
        e3.setId(403L);

        given(repository.findByRoomTypeIdAndDate(400L, d1)).willReturn(Optional.of(e1));
        given(repository.findByRoomTypeIdAndDate(400L, d2)).willReturn(Optional.of(e2));
        given(repository.findByRoomTypeIdAndDate(400L, d3)).willReturn(Optional.of(e3));

        RoomInventoryBulkUpdateCommand cmd = new RoomInventoryBulkUpdateCommand(d1, d3, 50, 45);
        service.bulkUpdate(400L, cmd);

        then(repository).should(times(3)).update(anyLong(), any(RoomInventory.class));
        then(repository).should(never()).save(any(RoomInventory.class));
    }

    @Test
    @DisplayName("bulkUpdate: 일부는 기존, 일부는 신규 생성")
    void bulkUpdate_mixedExistingAndNew() {
        LocalDate start = LocalDate.now();
        LocalDate d0 = start;
        LocalDate d1 = start.plusDays(1);
        LocalDate d2 = start.plusDays(2);

        RoomInventory exist = RoomInventory.of(500L, d0, 5, 2);
        exist.setId(501L);
        given(repository.findByRoomTypeIdAndDate(500L, d0)).willReturn(Optional.of(exist));
        given(repository.findByRoomTypeIdAndDate(500L, d1)).willReturn(Optional.empty());
        given(repository.findByRoomTypeIdAndDate(500L, d2)).willReturn(Optional.empty());

        RoomInventoryBulkUpdateCommand cmd = new RoomInventoryBulkUpdateCommand(d0, d2, 30, 25);
        service.bulkUpdate(500L, cmd);

        then(repository).should(times(1)).update(eq(exist.getId()), eq(exist));
        then(repository).should(times(2)).save(any(RoomInventory.class));
    }

    @Test
    @DisplayName("update: 기존 객체의 값이 실제로 변경되는지 검증")
    void update_mutatesExisting() {
        LocalDate date = LocalDate.now();
        RoomInventory existing = RoomInventory.of(600L, date, 7, 3);
        existing.setId(601L);
        given(repository.findByRoomTypeIdAndDate(600L, date)).willReturn(Optional.of(existing));

        RoomInventoryUpdateCommand cmd = new RoomInventoryUpdateCommand(15, 10);
        service.update(600L, date, cmd);

        // 기존 객체의 값이 변경되었고, update가 호출되었는지 확인
        assertThat(existing.getTotalRooms()).isEqualTo(15);
        assertThat(existing.getAvailableRooms()).isEqualTo(10);
        then(repository).should().update(existing.getId(), existing);
    }
}

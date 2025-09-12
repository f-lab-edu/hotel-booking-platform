package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.roominventory.FakeRoomInventoryRepository;
import dev.muho.hotel.domain.roominventory.RoomInventoryController;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomInventoryController.class)
@Import(FakeDBConfiguration.class)
public class RoomInventoryE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeRoomInventoryRepository roomInventoryRepository;

    @BeforeEach
    void setUp() {
        roomInventoryRepository.clear();
    }

    @Test
    void 인벤토리_목록_조회_성공() throws Exception {
        // 여러 인벤토리 생성
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 10, 1))
                .endDate(java.time.LocalDate.of(2026, 10, 5))
                .totalRooms(10)
                .availableRooms(8)
                .build();

        roomInventoryRepository.bulkUpdate(1L, request);

        // 인벤토리 목록 조회
        mockMvc.perform(get("/v1/room-types/1/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    void 인벤토리_일괄_생성_성공() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 3))
                .totalRooms(15)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // 변경된 인벤토리 확인
        mockMvc.perform(get("/v1/room-types/1/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].totalRooms").value(15))
                .andExpect(jsonPath("$.content[0].availableRooms").value(10));
    }

    @Test
    void 인벤토리_일괄_생성_실패_시작_날짜_없음() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .endDate(java.time.LocalDate.of(2026, 7, 1))
                .totalRooms(15)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.startDate").value("시작일은 필수입니다."));
    }

    @Test
    void 인벤토리_일괄_생성_실패_시작_날짜_과거() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2022, 7, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 3))
                .totalRooms(15)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.startDate").value("시작일은 오늘이거나 오늘 이후여야 합니다."));
    }

    @Test
    void 인벤토리_일괄_생성_실패_종료_날짜_없음() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .totalRooms(15)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.endDate").value("종료일은 필수입니다."));
    }

    @Test
    void 인벤토리_일괄_생성_실패_종료_날짜_과거() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .endDate(java.time.LocalDate.of(2022, 7, 3))
                .totalRooms(15)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.endDate").value("종료일은 오늘이거나 오늘 이후여야 합니다."));
    }

    @Test
    void 인벤토리_일괄_생성_실패_전체_객실수_음수() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 3))
                .totalRooms(-5)
                .availableRooms(10)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.totalRooms").value("전체 객실 수는 0 이상이어야 합니다."));
    }

    @Test
    void 인벤토리_일괄_생성_실패_예약_가능_객실수_음수() throws Exception {
        RoomInventoryBulkUpdateRequest request = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 7, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 3))
                .totalRooms(15)
                .availableRooms(-3)
                .build();

        mockMvc.perform(put("/v1/room-types/1/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.availableRooms").value("예약 가능 객실 수는 0 이상이어야 합니다."));
    }

    @Test
    void 인벤토리_단건_수정_성공() throws Exception {
        // 먼저 인벤토리 생성
        RoomInventoryBulkUpdateRequest createRequest = RoomInventoryBulkUpdateRequest.builder()
                .startDate(java.time.LocalDate.of(2026, 8, 1))
                .endDate(java.time.LocalDate.of(2026, 8, 1))
                .totalRooms(20)
                .availableRooms(15)
                .build();
        roomInventoryRepository.bulkUpdate(1L, createRequest);

        // 단건 인벤토리 수정
        RoomInventoryUpdateRequest updateRequest = RoomInventoryUpdateRequest.builder()
                .date(java.time.LocalDate.of(2026, 8, 1))
                .totalRooms(25)
                .availableRooms(20)
                .build();
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/room-types/1/inventories/2026-08-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNoContent());

        // 변경된 인벤토리 확인
        mockMvc.perform(get("/v1/room-types/1/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].totalRooms").value(25))
                .andExpect(jsonPath("$.content[0].availableRooms").value(20));
    }
}

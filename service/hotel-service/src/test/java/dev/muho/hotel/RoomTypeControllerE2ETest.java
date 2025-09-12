package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.roomtype.FakeRoomTypeRepository;
import dev.muho.hotel.domain.roomtype.RoomTypeController;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeCreateRequest;
import dev.muho.hotel.domain.roomtype.dto.RoomTypeUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomTypeController.class)
@Import(FakeDBConfiguration.class)
public class RoomTypeControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeRoomTypeRepository roomTypeRepository;

    @BeforeEach
    void setUp() {
        roomTypeRepository.clear();
    }

    @Test
    void 룸타입_목록_조회_성공() throws Exception {
        // 여러 룸타입 생성
        RoomTypeCreateRequest roomType1 = RoomTypeCreateRequest.builder()
                .name("Room Type One")
                .description("Description One")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();
        RoomTypeCreateRequest roomType2 = RoomTypeCreateRequest.builder()
                .name("Room Type Two")
                .description("Description Two")
                .maxOccupancy(4)
                .standardOccupancy(3)
                .viewType("Sea View")
                .bedType("King")
                .build();
        roomTypeRepository.save(1L, roomType1);
        roomTypeRepository.save(1L, roomType2);

        // 룸타입 목록 조회
        mockMvc.perform(get("/v1/hotels/1/room-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content.[0].name").value("Room Type One"))
                .andExpect(jsonPath("$.content.[1].name").value("Room Type Two"));
    }

    @Test
    void 룸타입_목록_조회_빈_목록_성공() throws Exception {
        // 룸타입 목록 조회
        mockMvc.perform(get("/v1/hotels/1/room-types").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void 룸타입_조회_성공() throws Exception {
        // 먼저 룸타입을 생성
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Test Room Type")
                .description("A room type for testing")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("Garden View")
                .bedType("Double")
                .build();
        var createdRoomType = roomTypeRepository.save(1L, createRequest);

        // 생성된 룸타입 조회
        mockMvc.perform(get("/v1/hotels/1/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdRoomType.getId()))
                .andExpect(jsonPath("$.hotelId").value(1L))
                .andExpect(jsonPath("$.name").value(createRequest.getName()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$.maxOccupancy").value(createRequest.getMaxOccupancy()))
                .andExpect(jsonPath("$.standardOccupancy").value(createRequest.getStandardOccupancy()))
                .andExpect(jsonPath("$.viewType").value(createRequest.getViewType()))
                .andExpect(jsonPath("$.bedType").value(createRequest.getBedType()));
    }

    @Test
    void 룸타입_조회_실패_존재하지_않는_룸타입() throws Exception {
        mockMvc.perform(get("/v1/hotels/1/room-types/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void 룸타입_생성_성공() throws Exception {
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("New Room Type")
                .description("A newly created room type")
                .maxOccupancy(3)
                .standardOccupancy(2)
                .viewType("Mountain View")
                .bedType("Twin")
                .build();

        mockMvc.perform(post("/v1/hotels/1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hotelId").value(1L))
                .andExpect(jsonPath("$.name").value(createRequest.getName()))
                .andExpect(jsonPath("$.description").value(createRequest.getDescription()))
                .andExpect(jsonPath("$.maxOccupancy").value(createRequest.getMaxOccupancy()))
                .andExpect(jsonPath("$.standardOccupancy").value(createRequest.getStandardOccupancy()))
                .andExpect(jsonPath("$.viewType").value(createRequest.getViewType()))
                .andExpect(jsonPath("$.bedType").value(createRequest.getBedType()));
    }

    @Test
    void 룸타입_생성_실패_이름_없음() throws Exception {
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .description("A room type without a name")
                .maxOccupancy(3)
                .standardOccupancy(2)
                .viewType("Mountain View")
                .bedType("Twin")
                .build();

        mockMvc.perform(post("/v1/hotels/1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("객실 타입 이름은 필수입니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 룸타입_생성_실패_최대_인원_음수() throws Exception {
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Invalid Room Type")
                .description("A room type with invalid max occupancy")
                .maxOccupancy(-1)
                .standardOccupancy(2)
                .viewType("Mountain View")
                .bedType("Twin")
                .build();

        mockMvc.perform(post("/v1/hotels/1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.maxOccupancy").value("최대 인원은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 룸타입_생성_실패_기준_인원_0() throws Exception {
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Invalid Room Type")
                .description("A room type with invalid standard occupancy")
                .maxOccupancy(3)
                .standardOccupancy(0)
                .viewType("Mountain View")
                .bedType("Twin")
                .build();

        mockMvc.perform(post("/v1/hotels/1/room-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.standardOccupancy").value("기준 인원은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 룸타입_수정_성공() throws Exception {
        // 먼저 룸타입을 생성
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Old Room Type")
                .description("Old description")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();
        var createdRoomType = roomTypeRepository.save(1L, createRequest);

        // 룸타입 수정 요청
        RoomTypeUpdateRequest updateRequest = RoomTypeUpdateRequest.builder()
                .name("Updated Room Type")
                .description("Updated description")
                .maxOccupancy(4)
                .standardOccupancy(3)
                .viewType("Sea View")
                .bedType("King")
                .build();

        mockMvc.perform(put("/v1/hotels/1/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdRoomType.getId()))
                .andExpect(jsonPath("$.hotelId").value(1L))
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.description").value(updateRequest.getDescription()))
                .andExpect(jsonPath("$.maxOccupancy").value(updateRequest.getMaxOccupancy()))
                .andExpect(jsonPath("$.standardOccupancy").value(updateRequest.getStandardOccupancy()))
                .andExpect(jsonPath("$.viewType").value(updateRequest.getViewType()))
                .andExpect(jsonPath("$.bedType").value(updateRequest.getBedType()));
    }

    @Test
    void 룸타입_수정_실패_존재하지_않는_룸타입() throws Exception {
        RoomTypeUpdateRequest updateRequest = RoomTypeUpdateRequest.builder()
                .name("Non-existent Room Type")
                .description("Trying to update a non-existent room type")
                .maxOccupancy(3)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();

        mockMvc.perform(put("/v1/hotels/1/room-types/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 룸타입_수정_실패_최대_인원_음수() throws Exception {
        // 먼저 룸타입을 생성
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Old Room Type")
                .description("Old description")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();
        roomTypeRepository.save(1L, createRequest);

        // 룸타입 수정 요청 - 최대 인원 음수
        RoomTypeUpdateRequest updateRequest = RoomTypeUpdateRequest.builder()
                .name("Updated Room Type")
                .description("Updated description")
                .maxOccupancy(-1) // Invalid max occupancy
                .standardOccupancy(3)
                .viewType("Sea View")
                .bedType("King")
                .build();

        mockMvc.perform(put("/v1/hotels/1/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.maxOccupancy").value("최대 인원은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 룸타입_수정_실패_기준_인원_0() throws Exception {
        // 먼저 룸타입을 생성
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Old Room Type")
                .description("Old description")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();
        roomTypeRepository.save(1L, createRequest);

        // 룸타입 수정 요청 - 기준 인원 0
        RoomTypeUpdateRequest updateRequest = RoomTypeUpdateRequest.builder()
                .name("Updated Room Type")
                .description("Updated description")
                .maxOccupancy(3)
                .standardOccupancy(0) // Invalid standard occupancy
                .viewType("Sea View")
                .bedType("King")
                .build();

        mockMvc.perform(put("/v1/hotels/1/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.standardOccupancy").value("기준 인원은 1 이상이어야 합니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 룸타입_삭제_성공() throws Exception {
        // 먼저 룸타입을 생성
        RoomTypeCreateRequest createRequest = RoomTypeCreateRequest.builder()
                .name("Room Type to be deleted")
                .description("This room type will be deleted")
                .maxOccupancy(2)
                .standardOccupancy(2)
                .viewType("City View")
                .bedType("Queen")
                .build();
        roomTypeRepository.save(1L, createRequest);

        // 룸타입 삭제 요청
        mockMvc.perform(delete("/v1/hotels/1/room-types/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}

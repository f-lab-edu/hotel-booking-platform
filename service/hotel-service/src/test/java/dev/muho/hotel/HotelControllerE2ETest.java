package dev.muho.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.muho.hotel.domain.hotel.FakeHotelRepository;
import dev.muho.hotel.domain.hotel.HotelController;
import dev.muho.hotel.domain.hotel.dto.HotelCreateRequest;
import dev.muho.hotel.domain.hotel.dto.HotelUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@Import(FakeDBConfiguration.class)
public class HotelControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FakeHotelRepository hotelRepository;

    @BeforeEach
    void setUp() {
        hotelRepository.clear();
    }

    @Test
    void 호텔_목록_조회_성공() throws Exception {
        // 여러 호텔 생성
        HotelCreateRequest hotel1 = HotelCreateRequest.builder()
                .name("Hotel One")
                .address("Address One")
                .country("Country One")
                .city("City One")
                .rating(4)
                .description("First hotel")
                .contactNumber("111-111-1111")
                .build();
        HotelCreateRequest hotel2 = HotelCreateRequest.builder()
                .name("Hotel Two")
                .address("Address Two")
                .country("Country Two")
                .city("City Two")
                .rating(5)
                .description("Second hotel")
                .contactNumber("222-222-2222")
                .build();
        hotelRepository.save(hotel1);
        hotelRepository.save(hotel2);

        // 호텔 목록 조회
        mockMvc.perform(get("/v1/hotels").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content.[0].name").value("Hotel One"))
                .andExpect(jsonPath("$.content.[1].name").value("Hotel Two"));
    }

    @Test
    void 호텔_조회_성공() throws Exception {
        // 먼저 호텔을 생성
        HotelCreateRequest createRequest = HotelCreateRequest.builder()
                .name("Test Hotel")
                .address("123 Test St")
                .country("Test Country")
                .city("Test City")
                .rating(5)
                .description("A test hotel")
                .contactNumber("123-456-7890")
                .build();
        hotelRepository.save(createRequest);

        // 생성된 호텔 조회
        mockMvc.perform(get("/v1/hotels/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void 호텔_조회_실패_존재하지_않는_호텔() throws Exception {
        mockMvc.perform(get("/v1/hotels/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void 호텔_생성_성공() throws Exception {
        HotelCreateRequest request = HotelCreateRequest.builder()
                .name("Test Hotel")
                .address("123 Test St")
                .country("Test Country")
                .city("Test City")
                .rating(5)
                .description("A test hotel")
                .contactNumber("123-456-7890")
                .build();
        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/hotels").contentType(MediaType.APPLICATION_JSON).content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Hotel"));
    }

    @Test
    void 호텔_생성_실패_이름_없음() throws Exception {
        HotelCreateRequest request = HotelCreateRequest.builder()
                .address("123 Test St")
                .country("Test Country")
                .city("Test City")
                .rating(5)
                .description("A test hotel")
                .contactNumber("123-456-7890")
                .build();
        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/hotels").contentType(MediaType.APPLICATION_JSON).content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.name").value("호텔 이름은 필수입니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 호텔_생성_실패_등급_범위_초과() throws Exception {
        HotelCreateRequest request = HotelCreateRequest.builder()
                .name("Test Hotel")
                .address("123 Test St")
                .country("Test Country")
                .city("Test City")
                .rating(6) // 5성을 초과하는 등급
                .description("A test hotel")
                .contactNumber("123-456-7890")
                .build();
        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/hotels").contentType(MediaType.APPLICATION_JSON).content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation.rating").value("호텔 등급은 5성을 초과할 수 없습니다."))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void 호텔_수정_성공() throws Exception {
        // 먼저 호텔을 생성
        HotelCreateRequest createRequest = HotelCreateRequest.builder()
                .name("Old Hotel")
                .address("123 Old St")
                .country("Old Country")
                .city("Old City")
                .rating(3)
                .description("An old hotel")
                .contactNumber("111-222-3333")
                .build();
        hotelRepository.save(createRequest);

        // 호텔 수정 요청
        HotelUpdateRequest updateRequest = HotelUpdateRequest.builder()
                .name("Updated Hotel")
                .address("456 New St")
                .country("New Country")
                .city("New City")
                .rating(4)
                .description("An updated hotel")
                .contactNumber("444-555-6666")
                .build();
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/hotels/1").contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Hotel"))
                .andExpect(jsonPath("$.address").value("456 New St"))
                .andExpect(jsonPath("$.country").value("New Country"))
                .andExpect(jsonPath("$.city").value("New City"))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.description").value("An updated hotel"))
                .andExpect(jsonPath("$.contactNumber").value("444-555-6666"));
    }

    @Test
    void 호텔_수정_실패_존재하지_않는_호텔() throws Exception {
        HotelUpdateRequest updateRequest = HotelUpdateRequest.builder()
                .name("Non-existent Hotel")
                .address("No Address")
                .country("No Country")
                .city("No City")
                .rating(3)
                .description("This hotel does not exist")
                .contactNumber("000-000-0000")
                .build();
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/v1/hotels/999").contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void 호텔_삭제_성공() throws Exception {
        // 먼저 호텔을 생성
        HotelCreateRequest createRequest = HotelCreateRequest.builder()
                .name("Delete Hotel")
                .address("123 Delete St")
                .country("Delete Country")
                .city("Delete City")
                .rating(2)
                .description("A hotel to be deleted")
                .contactNumber("999-888-7777")
                .build();
        hotelRepository.save(createRequest);

        // 호텔 삭제 요청
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/v1/hotels/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}

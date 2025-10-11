package dev.muho.hotel.dto.request;

import dev.muho.hotel.domain.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomTypeSearchRequest {

    @NotNull(message = "호텔 ID는 필수입니다.")
    private Long hotelId;

    private String name; // 선택적 - 룸타입 이름으로 검색

    private Status status; // 선택적 - 상태로 필터링
}

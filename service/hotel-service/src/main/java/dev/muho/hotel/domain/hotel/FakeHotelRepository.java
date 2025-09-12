package dev.muho.hotel.domain.hotel;

import dev.muho.hotel.domain.hotel.dto.HotelCreateRequest;
import dev.muho.hotel.domain.hotel.dto.HotelResponse;
import dev.muho.hotel.domain.hotel.dto.HotelUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FakeHotelRepository {

    private final Map<Long, HotelResponse> db = new HashMap<>();
    private long currentId = 1L;

    public Page<HotelResponse> findAll(Pageable pageable) {
        // 1. HashMap은 순서가 없으므로, 값들을 리스트로 변환합니다.
        List<HotelResponse> sortedHotels = new ArrayList<>(db.values());

        // 2. 일관된 순서를 보장하기 위해 ID를 기준으로 정렬합니다. (매우 중요)
        // 실제 구현에서는 Pageable 객체의 Sort 정보를 사용해 동적으로 정렬할 수 있습니다.
        sortedHotels.sort(Comparator.comparing(HotelResponse::getId));

        // 3. 정렬된 리스트를 기준으로 페이징 처리를 합니다.
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sortedHotels.size());

        // 만약 요청한 페이지가 데이터 범위를 벗어나는 경우 빈 리스트를 반환
        if (start > sortedHotels.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, sortedHotels.size());
        }

        List<HotelResponse> content = sortedHotels.subList(start, end);

        // 4. PageImpl 객체를 생성하여 반환합니다.
        return new PageImpl<>(content, pageable, sortedHotels.size());
    }

    public HotelResponse findById(Long id) {
        return db.get(id);
    }

    public HotelResponse save(HotelCreateRequest request) {
        HotelResponse hotel = new HotelResponse(currentId++, request.getName(), request.getAddress(), request.getCountry(), request.getCity(), request.getRating(), request.getDescription(), request.getContactNumber());
        db.put(hotel.getId(), hotel);
        return hotel;
    }

    public HotelResponse update(Long id, HotelUpdateRequest request) {
        HotelResponse hotel = new HotelResponse(id, request.getName(), request.getAddress(), request.getCountry(), request.getCity(), request.getRating(), request.getDescription(), request.getContactNumber());
        db.put(hotel.getId(), hotel);
        return hotel;
    }

    public void deleteById(Long id) {
        db.remove(id);
    }

    public void clear() {
        db.clear();
        currentId = 1L;
    }
}

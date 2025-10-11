package dev.muho.hotel.service;

import dev.muho.hotel.domain.Hotel;
import dev.muho.hotel.domain.Status;
import dev.muho.hotel.dto.request.HotelCreateRequest;
import dev.muho.hotel.dto.request.HotelStatusUpdateRequest;
import dev.muho.hotel.dto.request.HotelUpdateRequest;
import dev.muho.hotel.dto.response.HotelResponse;
import dev.muho.hotel.global.exception.HotelNotFoundException;
import dev.muho.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelResponse findHotelById(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        return HotelResponse.from(hotel);
    }

    public List<HotelResponse> findAllHotels() {
        return hotelRepository.findAll().stream()
                .map(HotelResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelResponse createHotel(HotelCreateRequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .address(request.getAddress())
                .rating(request.getRating())
                .build();

        Hotel savedHotel = hotelRepository.save(hotel);

        return HotelResponse.from(savedHotel);
    }

    @Transactional
    public HotelResponse updateHotel(Long hotelId, HotelUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        hotel.updateHotelInfo(request.getName(), request.getAddress(), request.getRating());

        return HotelResponse.from(hotel);
    }

    @Transactional
    public void deleteHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        hotel.changeStatus(Status.INACTIVE);
    }

    @Transactional
    public HotelResponse updateHotelStatus(Long hotelId, HotelStatusUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(HotelNotFoundException::new);

        hotel.changeStatus(request.getStatus());

        return HotelResponse.from(hotel);
    }
}

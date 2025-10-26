package dev.muho.hoteleventconsumer.event;

import dev.muho.hoteleventconsumer.domain.RoomInventory;
import dev.muho.hoteleventconsumer.repository.RoomInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final RoomInventoryRepository roomInventoryRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;

    @Transactional
    @KafkaListener(topics = "booking-created", groupId = "hotel-event-consumer-group")
    public void consumeBookingCreatedEvent(BookingCreatedEvent event) {
        List<LocalDate> stayDates = event.getCheckInDate().datesUntil(event.getCheckOutDate()).toList();
        List<RoomInventory> inventories = roomInventoryRepository.findByRoomTypeIdAndDateIn(event.getRoomTypeId(), stayDates);

        try {
            for (RoomInventory inventory : inventories) {
                inventory.reserveRoom();
            }
        } catch (IllegalStateException ex) {
            kafkaTemplate.send("booking-failed", event.getBookingLongId());
            throw ex;
        }
    }
}

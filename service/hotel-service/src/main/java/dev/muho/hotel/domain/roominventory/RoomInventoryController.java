package dev.muho.hotel.domain.roominventory;

import dev.muho.hotel.domain.roominventory.dto.RoomInventoryBulkUpdateRequest;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryResponse;
import dev.muho.hotel.domain.roominventory.dto.RoomInventoryUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/room-types/{roomTypeId}/inventories")
public class RoomInventoryController {

    @GetMapping
    public List<RoomInventoryResponse> getInventories(@PathVariable Long roomTypeId, @RequestParam String startDate, @RequestParam String endDate) {
        return List.of(
                new RoomInventoryResponse(1L, "2025-11-01", 10, 5),
                new RoomInventoryResponse(2L, "2025-11-02", 10, 3)
        );
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bulkUpdateInventory(@PathVariable Long roomTypeId, @Valid @RequestBody RoomInventoryBulkUpdateRequest roomInventoryBulkUpdateRequest) {
    }

    @PutMapping("/{date}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInventory(@PathVariable Long roomTypeId, @PathVariable LocalDate date, @Valid @RequestBody RoomInventoryUpdateRequest roomInventoryUpdateRequest) {
    }
}

package neyas.itmo.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDto(
        UUID id,
        String type,
        String status,
        UUID clientId,
        UUID managerId,
        UUID carId,
        Long price,
        LocalDateTime startsAt,
        String sparePartCode,
        Integer quantity,
        Instant createdAt

) {
}

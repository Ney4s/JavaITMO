package neyas.itmo.dto;

import java.util.UUID;

public record CarDto(
        UUID id,
        String modelCode,
        String color,
        String vin,
        String status,
        long price,
        SpecificationDto specification
) {
}

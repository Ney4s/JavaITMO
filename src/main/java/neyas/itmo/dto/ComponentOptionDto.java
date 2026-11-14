package neyas.itmo.dto;

import java.util.UUID;

public record ComponentOptionDto(
        UUID id,
        String code,
        String type,
        String name,
        Long price,
        String fuelType,
        Long hpPower,
        Double displacement,
        String transmissionType,
        String bodyType,
        String driveType)
{
}

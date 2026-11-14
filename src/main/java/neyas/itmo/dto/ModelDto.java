package neyas.itmo.dto;

import neyas.itmo.entity.configuration.ComponentOption;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ModelDto(
        UUID id,
        String code,
        String manufacturer,
        String name,
        long basePrice,
        Map<String, ComponentOptionDto> baseConfiguration,
        List<ComponentOptionDto> availableOption
) {
}

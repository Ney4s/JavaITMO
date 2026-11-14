package neyas.itmo.dto;

import java.util.Map;

public record SpecificationDto(
        Map<String, ComponentOptionDto> components,
        long price
) {
}

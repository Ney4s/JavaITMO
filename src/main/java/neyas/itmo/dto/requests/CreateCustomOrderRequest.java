package neyas.itmo.dto.requests;

import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;

import java.util.Map;

public record CreateCustomOrderRequest(
        @NotBlank String modelCode,
        @NotBlank String color,
        @NotNull Map<ComponentType, String> options
) {
}

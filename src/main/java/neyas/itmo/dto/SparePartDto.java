package neyas.itmo.dto;

import java.util.List;
import java.util.UUID;

public record SparePartDto(
        UUID id,
        String code,
        String name,
        Long price,
        int quantityInStock,
        List<String> capableModels
) {
}

package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.Positive;

public record CreateSparePartOrderRequest(
        @NotBlank String sparePartCode,
        @Positive int quantity
) {
}

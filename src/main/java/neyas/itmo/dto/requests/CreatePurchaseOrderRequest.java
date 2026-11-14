package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotNull;

import java.util.UUID;

public record CreatePurchaseOrderRequest(
        @NotNull UUID carId
) {
}

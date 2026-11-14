package neyas.itmo.dto.requests;

import neyas.itmo.framework.PositiveOrZero;

public record ChangeQuantityRequest(
        @PositiveOrZero int quantity
) {
}

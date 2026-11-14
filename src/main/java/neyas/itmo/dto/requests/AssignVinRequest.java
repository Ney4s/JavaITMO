package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotBlank;

public record AssignVinRequest(
        @NotBlank String vin
) {
}

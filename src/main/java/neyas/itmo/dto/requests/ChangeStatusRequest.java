package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotBlank;

public record ChangeStatusRequest(
        @NotBlank String status
) {

}

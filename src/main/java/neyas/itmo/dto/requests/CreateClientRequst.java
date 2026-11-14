package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotBlank;

public record CreateClientRequst(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String phone
) {
}

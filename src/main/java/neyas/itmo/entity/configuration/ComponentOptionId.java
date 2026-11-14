package neyas.itmo.entity.configuration;

import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;

public record ComponentOptionId(
        @NotBlank String code
) {
    @Override
    public String toString() {
        return code;
    }
}

package neyas.itmo.entity.model;

import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;

public record ModelId(
        @NotBlank String code
) {
    @Override
    public String toString() {
        return code;
    }

}

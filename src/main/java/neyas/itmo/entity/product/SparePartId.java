package neyas.itmo.entity.product;

import neyas.itmo.framework.NotBlank;

import java.util.UUID;

public record SparePartId(
        @NotBlank String code
) {
    public SparePartId {
        code = code.strip().toUpperCase();
    }


    @Override
    public String toString() {
        return code;
    }


}

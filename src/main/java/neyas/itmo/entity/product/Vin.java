package neyas.itmo.entity.product;

import neyas.itmo.Validation;
import neyas.itmo.framework.NotBlank;

public record Vin(
        @NotBlank String value
) {
    public Vin {
        Validation.requireNotBlank(value, "VIN");
        value = value.strip().toUpperCase();
        Validation.require(value.matches("[A-HJ-NPR-Z0-9]{17}"),
                "VIN имеет неверный формат: %s".formatted(value));
    }


    @Override
    public String toString() {
        return value;
    }
}

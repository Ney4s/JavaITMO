package neyas.itmo.entity.type;

import lombok.Getter;

public enum FuelType {
    GASOLINE("Бензин"),
    DIESEL("Дизель"),
    ELECTRICITY("Электричество");

    @Getter private final String displayName;

    FuelType(String displayName) {
        this.displayName = displayName;
    }
}

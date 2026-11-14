package neyas.itmo.entity.type;

import lombok.Getter;

public enum ComponentType {
    ENGINE("Двигатель"),
    TRANSMISSION("Трансмиссия"),
    BODY("Кузов"),
    DRIVE("Привод"),
    INTERIOR("Иртерьер Салона"),
    STEERING_WHEEL("Руль");

    @Getter private final String displayName;

    ComponentType(String displayName) {
        this.displayName = displayName;
    }

}

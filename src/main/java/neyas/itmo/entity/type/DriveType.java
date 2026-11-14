package neyas.itmo.entity.type;

import lombok.Getter;

public enum DriveType {
    FRONT("Передний"),
    REAR("Задний"),
    ALL("Полный");

    @Getter private final String displayName;

    DriveType(String displayName) {
        this.displayName = displayName;
    }
}

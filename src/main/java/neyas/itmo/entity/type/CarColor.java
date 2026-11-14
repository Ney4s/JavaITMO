package neyas.itmo.entity.type;

import lombok.Getter;

public enum CarColor {
    WHITE("Белый"),
    BLACK("Черный"),
    SILVER("Серебристый"),
    GRAY("Серый"),
    BLUE("Синий"),
    RED("Красный"),
    GREEN("Зеленый");

    @Getter private final String displayName;

    CarColor(String displayName) {
        this.displayName = displayName;
    }
}

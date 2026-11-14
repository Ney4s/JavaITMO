package neyas.itmo.entity.type;

import lombok.Getter;

public enum BodyType {
    SEDAN("Седан"),
    COUPE("Купе"),
    CROSSOVER("Кроссовер"),
    CABRIOLET("Кабриолет");

    @Getter private final String displayName;


    BodyType(String displayName) {
        this.displayName = displayName;
    }



}

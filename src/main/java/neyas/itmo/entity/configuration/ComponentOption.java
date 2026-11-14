package neyas.itmo.entity.configuration;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.Identifiable;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.framework.NotBlank;
import neyas.itmo.framework.NotNull;
import neyas.itmo.framework.PositiveOrZero;

import java.awt.*;


@Entity
@Table(name = "component_options")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "component_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class ComponentOption extends BaseEntity {

    @Column(nullable = false, unique = true)
    private ComponentOptionId code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long price;

    protected ComponentOption(
            @NotNull ComponentOptionId code,
            @NotBlank String name,
            @PositiveOrZero long price) {
        this.code = code;
        this.name = name;
        this.price = price;
    }

    public abstract ComponentType type();

    public ComponentOptionId id() {
        return code;
    }

    public String name() {
        return name;
    }

    public long price() {
        return price;
    }
}

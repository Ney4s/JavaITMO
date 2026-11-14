package neyas.itmo.entity.configuration;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import neyas.itmo.entity.type.ComponentType;


@Entity
@NoArgsConstructor
@DiscriminatorValue("STEERING_WHEEL")
public class SteeringWheelOption extends ComponentOption {

    public SteeringWheelOption(
            ComponentOptionId code,
            String name,
            long price
    ) {
        super(code, name, price);
    }

    @Override
    public ComponentType type(){
        return ComponentType.STEERING_WHEEL;
    }

}

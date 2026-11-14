package neyas.itmo.entity.configuration;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.FuelType;


@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("ENGINE")
public class EngineOption  extends ComponentOption {

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    @Column(name = "hp_power")
    private long hpPower;

    @Column(name = "displacement")
    private double displacement;

    public EngineOption(ComponentOptionId code, String name, long price, FuelType fuelType, long hpPower, double displacement) {
        super(code, name, price);
        this.fuelType = fuelType;
        this.displacement = displacement;
        this.hpPower = hpPower;
    }

    @Override
    public ComponentType type(){
        return ComponentType.ENGINE;
    }

    public FuelType fuelType(){
        return fuelType;
    }

    public long hpPower(){
        return hpPower;
    }

    public double displacement(){
        return displacement;
    }

}

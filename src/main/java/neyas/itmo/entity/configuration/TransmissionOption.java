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
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.framework.NotNull;


@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("TRANSMISSION")
public class TransmissionOption extends ComponentOption {

    @Column(name = "transmission_type")
    @Enumerated(EnumType.STRING)
    private TransmissionType transmissionType;

    public TransmissionOption(
            ComponentOptionId code,
            String name,
            long price,
            @NotNull TransmissionType transmissionType) {
        super(code, name, price);
        this.transmissionType = transmissionType;

    }

    @Override
    public ComponentType type() {
        return ComponentType.TRANSMISSION;
    }

    public TransmissionType transmissionType(){
        return transmissionType;
    }

}

package neyas.itmo.entity.configuration;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neyas.itmo.entity.type.BodyType;
import neyas.itmo.entity.type.ComponentType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("BODY")
public class BodyOption extends ComponentOption {

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type")
    private BodyType bodyType;

    public BodyOption(
            ComponentOptionId componentOptionId,
            String name,
            long price,
            BodyType bodyOption){
        super(componentOptionId, name, price);
        this.bodyType = bodyOption;
    }

    @Override
    public ComponentType type(){
        return ComponentType.BODY;
    }

    public BodyType bodyType(){
        return bodyType;
    }
}

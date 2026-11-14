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
import neyas.itmo.entity.type.DriveType;


@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("DRIVE")
public class DriveOption extends ComponentOption {
    @Enumerated(EnumType.STRING)
    @Column(name = "drive_type")
    private DriveType driveType;

    public DriveOption(
        ComponentOptionId code,
        String name,
        long price,
        DriveType driveType) {
        super(code, name, price);
        this.driveType = driveType;
    }

    @Override
    public ComponentType type() {
        return ComponentType.DRIVE;
    }

    public DriveType driveType(){
        return driveType;
    }



}

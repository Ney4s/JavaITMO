package neyas.itmo.entity.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import neyas.itmo.Validation;
import neyas.itmo.entity.BaseEntity;
import neyas.itmo.entity.configuration.*;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.type.*;

import java.util.LinkedHashMap;
import java.util.Map;


@Entity
@Table(name = "cars")
@Getter
@NoArgsConstructor
public class Car extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id")
    private Model model;

    @Column(unique = true)
    private Vin vin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarColor color;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "car_components",
            joinColumns = @JoinColumn(name = "car_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "component_type")
    private Map<ComponentType, ComponentOption> components = new LinkedHashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarStatus carStatus;


    public Car(Model model,
               Specification specification,
               Vin vin,
               CarColor color,
               CarStatus carStatus) {
        Validation.require(vin != null || !carStatus.requiresVin(),
                "автомобиль должен иметь VIN номер");
        this.components = new LinkedHashMap<>(specification.components());
        this.model = model;
        this.vin = vin;
        this.color = color;
        this.carStatus = carStatus;
    }

    public static Car ordered(Model model, Specification specification, CarColor color) {
        return new Car(model, specification, null, color, CarStatus.ORDERED);
    }

    public void changeStatus(CarStatus newStatus){
        Validation.require(carStatus.canChangeTo(newStatus),
                "Недопустимый переход статуса автомобиля: %s -> %s".formatted(carStatus, newStatus));
        this.carStatus = newStatus;
    }

    public void assignVin(Vin vin){
        Validation.require(this.vin == null, "VIN автомобиля уже присвоен");
        this.vin = vin;
    }

    public Specification specification() {
        return new Specification(components);
    }

    public ModelId modelId() {
        return model.getCode();
    }

    public long price(){
        return specification().price();
    }

    public FuelType fuelType() {
        return specification().fuelType();
    }

    public long hpPower() {
        return specification().engine().getHpPower();
    }
    public double displacement(){
        return specification().engine().getDisplacement();
    }
    public BodyOption body(){
        return specification().body();
    }
    public TransmissionOption transmission(){
        return specification().transmission();
    }

    public TransmissionType transmissionType(){
        return transmission().getTransmissionType();
    }
    public DriveOption drive(){
        return specification().drive();
    }
    public EngineOption engineType(){
        return specification().engine();
    }

    public DriveType driveType(){
        return drive().getDriveType();
    }
    public BodyType bodyType(){
        return body().getBodyType();
    }

    public CarStatus carStatus(){
        return carStatus;
    }

    public boolean isAvailable(){
        return carStatus == CarStatus.IN_STOCK;
    }
    public boolean isOnTestDrive(){
        return carStatus == CarStatus.ON_TEST_DRIVE;
    }


}

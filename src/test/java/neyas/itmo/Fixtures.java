package neyas.itmo;

import neyas.itmo.entity.configuration.*;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.product.SparePartId;
import neyas.itmo.entity.product.Vin;
import neyas.itmo.entity.type.*;
import neyas.itmo.entity.user.AdminUser;
import neyas.itmo.entity.user.Client;
import neyas.itmo.entity.user.Manager;
import neyas.itmo.entity.user.WarehouseAdmin;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class Fixtures {

    public static final ModelId BMW_320I = new ModelId("BMW-320I");
    public static final ModelId BMW_330I = new ModelId("BMW-330I");
    public static final ModelId BMW_M340I = new ModelId("BMW-M340I");

    public static final Vin VIN_BLACK = new Vin("WBA3A5C50DF123456");
    public static final Vin VIN_WHITE = new Vin("WBA3A5C50DF654321");
    public static final Vin VIN_FREE = new Vin("WBA8E9C50KG111222");

    private Fixtures() {}

    public static EngineOption engine() {
        return new EngineOption(
                new ComponentOptionId("ENG-320I"),
                "2.0 бензин 184 л.с.",
                1_800_000,
                FuelType.GASOLINE,
                184,
                2.0);
    }

    public static EngineOption dieselEngine() {
        return new EngineOption(
                new ComponentOptionId("ENG-330I"),
                "3.0 дизель 258 л.с.",
                2_200_000,
                FuelType.DIESEL,
                258,
                3.0);
    }

    public static BodyOption sedan() {
        return new BodyOption(
                new ComponentOptionId("BODY-SEDAN"),
                "Седан",
                900_000,
                BodyType.SEDAN);
    }

    public static DriveOption rearDrive() {
        return new DriveOption(
                new ComponentOptionId("DRIVE-REAR"),
                "Задний привод",
                200_000,
                DriveType.REAR);
    }

    public static TransmissionOption automatic() {
        return new TransmissionOption(
                new ComponentOptionId("TRANS-8AT"),
                "Автоматическая 8AT",
                350_000,
                TransmissionType.AUTOMATIC);
    }

    public static TransmissionOption manual() {
        return new TransmissionOption(
                new ComponentOptionId("TRANS-6MT"),
                "Механическая 6MT",
                320_000,
                TransmissionType.MANUAL);
    }

    public static SteeringWheelOption steeringStandard() {
        return new SteeringWheelOption(
                new ComponentOptionId("STEER-STD"),
                "Спортивный кожаный (Standard)",
                60_000);
    }

    public static SteeringWheelOption steeringMSport() {
        return new SteeringWheelOption(
                new ComponentOptionId("STEER-MSPORT"),
                "M-Sport с подогревом",
                85_000);
    }

    public static InteriorOption interiorGraphite() {
        return new InteriorOption(
                new ComponentOptionId("INT-GRAPHITE"),
                "Тканевый Graphite",
                70_000);
    }

    public static InteriorOption interiorDakota() {
        return new InteriorOption(
                new ComponentOptionId("INT-DAKOTA"),
                "Кожаный Dakota",
                180_000);
    }

    public static InteriorOption interiorPerformance() {
        return new InteriorOption(
                new ComponentOptionId("INT-PERFORMANCE"),
                "Спортивный Performance",
                230_000);
    }

    public static Map<ComponentType, ComponentOption> baseConfiguration() {
        Map<ComponentType, ComponentOption> base = new HashMap<>();
        base.put(ComponentType.ENGINE, engine());
        base.put(ComponentType.BODY, sedan());
        base.put(ComponentType.DRIVE, rearDrive());
        base.put(ComponentType.TRANSMISSION, automatic());
        base.put(ComponentType.STEERING_WHEEL, steeringStandard());
        base.put(ComponentType.INTERIOR, interiorGraphite());
        return base;
    }

    public static Model bmw320i() {
        Map<ComponentType, ComponentOption> base = baseConfiguration();
        Set<ComponentOption> available = new LinkedHashSet<>(base.values());
        available.add(steeringMSport());
        available.add(interiorDakota());
        available.add(manual());
        return new Model(BMW_320I, "BMW", "320i", base, available);
    }

    public static Model bmw330i() {
        Map<ComponentType, ComponentOption> base = baseConfiguration();
        base.put(ComponentType.ENGINE, dieselEngine());
        Set<ComponentOption> available = new LinkedHashSet<>(base.values());
        available.add(interiorPerformance());
        return new Model(BMW_330I, "BMW", "330i", base, available);
    }

    public static Car carInStock(Vin vin) {
        return new Car(bmw320i(), Specification.base(bmw320i()), vin, CarColor.BLACK, CarStatus.IN_STOCK);
    }

    public static Car carInStock() {
        return carInStock(VIN_BLACK);
    }

    public static Car orderedCar() {
        return Car.ordered(bmw320i(), Specification.base(bmw320i()), CarColor.WHITE);
    }

    public static SparePart brakePads() {
        return new SparePart(
                new SparePartId("SP-BRAKE-PADS"),
                "Тормозные колодки передние",
                12_000,
                10,
                Set.of(bmw320i()));
    }

    public static Client client() {
        return new Client("Анна Клиентова", "client@mail.ru", "+79990000001");
    }

    public static Manager manager() {
        return new Manager("Иван Менеджер", "manager@autosalon.ru", "+79990000002");
    }

    public static WarehouseAdmin warehouseAdmin() {
        return new WarehouseAdmin("Петр Складов", "warehouse@autosalon.ru", "+79990000003");
    }

    public static AdminUser admin() {
        return new AdminUser("root", "root@autosalon.ru", "+79990000004");
    }
}

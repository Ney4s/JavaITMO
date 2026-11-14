package neyas.itmo.mapper;

import neyas.itmo.dto.*;
import neyas.itmo.entity.configuration.*;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.order.BasicOrder;
import neyas.itmo.entity.order.ManagedOrder;
import neyas.itmo.entity.order.SparePartOrder;
import neyas.itmo.entity.order.TestDriveOrder;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.product.SparePart;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.user.User;
import org.mapstruct.Mapper;

import java.util.*;

@Mapper(componentModel = "spring")
public interface DomainMapper {

    default UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.role().toString()
        );
    }


    default ComponentOptionDto toDto(ComponentOption option){
        if(option==null) return null;

        return new ComponentOptionDto(
                option.getId(),
                option.getCode().code(),
                option.type().name(),
                option.getName(),
                option.getPrice(),
                option instanceof EngineOption engine ? engine.getFuelType().name() : null,
                option instanceof EngineOption engine ? engine.getHpPower() : null,
                option instanceof EngineOption engine ? engine.getDisplacement() : null,
                option instanceof TransmissionOption transmision ? transmision.getTransmissionType().name() : null,
                option instanceof BodyOption body ? body.getBodyType().name() : null,
                option instanceof DriveOption drive ? drive.getDriveType().name() : null);
    }

    default SpecificationDto toDto(Specification specification){
        if(specification==null) return null;

        return new SpecificationDto(toDto(specification.components()), specification.price());
    }

    default Map<String, ComponentOptionDto> toDto(Map<ComponentType, ComponentOption> components){
        Map<String, ComponentOptionDto> result = new LinkedHashMap<>();
        components.forEach((k,v)->{
            result.put(k.name(), toDto(v));
        });
        return result;
    }

    default ModelDto toDto(Model model){
        if(model==null) return null;

        return new ModelDto(
                model.getId(),
                model.getCode().code(),
                model.getManufacturer(),
                model.getName(),
                model.basePrice(),
                toDto(model.getBaseConfiguration()),
                model.getAvailableOptions()
                        .stream()
                        .map(this::toDto)
                        .toList()
        );
    }

    default CarDto toDto(Car car){
        if(car==null) return null;

        return new CarDto(
                car.getId(),
                car.modelId().code(),
                car.getColor().name(),
                car.getVin() == null ? null : car.getVin().value(),
                car.getCarStatus().name(),
                car.price(),
                toDto(car.specification())
        );
    }

    default SparePartDto toDto(SparePart sparePart){
        if(sparePart==null) return null;

        return new SparePartDto(
                sparePart.getId(),
                sparePart.getCode().code(),
                sparePart.getName(),
                sparePart.getPrice(),
                sparePart.getQuantityInStock(),
                sparePart.getCompatibleModels()
                        .stream()
                        .map(model -> model.getCode().code())
                        .toList());
    }

    default OrderDto toDto(BasicOrder order){
        if(order==null) return null;

        ManagedOrder managedOrder = order instanceof ManagedOrder value ? value : null;
        TestDriveOrder testDriveOrder = order instanceof TestDriveOrder value ? value : null;
        SparePartOrder sparePartOrder = order instanceof SparePartOrder value ? value : null;

        return new OrderDto(
                order.getId(),
                order.getOrderType().name(),
                order.getOrderStatus().name(),
                order.getClient().getId(),
                managedOrder == null ? null : managedOrder.getManager().getId(),
                getCarId(order),
                getPriceOf(order),
                testDriveOrder == null ? null : testDriveOrder.getStartsAt(),
                sparePartOrder == null ? null : sparePartOrder.getSparePart().getCode().code(),
                sparePartOrder == null ? null : sparePartOrder.getQuantity(),
                order.getCreatedAt());
    }

    private static UUID getCarId(BasicOrder order){
        if(order instanceof TestDriveOrder testDriveOrder){
            return testDriveOrder.getCar().getId();
        }
        if(order instanceof ManagedOrder managedOrder){
            return managedOrder.getCar().getId();
        }
        return null;
    }

    private static Long getPriceOf(BasicOrder order){
        if(order instanceof SparePartOrder sparePartOrder){
            return sparePartOrder.getPrice();
        }
        if(order instanceof ManagedOrder managedOrder){
            return managedOrder.getPrice();
        }
        return null;
    }

    default List<CarDto> toCarDtos(List<Car> cars){
        return cars.stream()
                .map(this::toDto)
                .toList();
    }

    default List<ModelDto> toModelDtos(List<Model> models){
        return models.stream()
                .map(this::toDto)
                .toList();
    }

    default List<OrderDto> toOrderDtos(List<? extends BasicOrder> orders){
        return orders.stream()
                .map(this::toDto)
                .toList();
    }

    default List<SparePartDto> toSparePartDtos(List<SparePart> spareParts){
        return spareParts.stream()
                .map(this::toDto)
                .toList();
    }

}

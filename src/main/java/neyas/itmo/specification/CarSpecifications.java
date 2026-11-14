package neyas.itmo.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import neyas.itmo.entity.CarFilter;
import neyas.itmo.entity.configuration.BodyOption;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.DriveOption;
import neyas.itmo.entity.configuration.EngineOption;
import neyas.itmo.entity.configuration.TransmissionOption;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.product.Car;
import neyas.itmo.entity.type.ComponentType;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CarSpecifications {

    private CarSpecifications(){}

    public static Specification<Car> matching(CarFilter filter){
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("removed")));

            if(filter == null){
                return builder.and(predicates.toArray(Predicate[]::new));
            }

            if(filter.carStatus() != null){
                predicates.add(builder.equal(root.get("carStatus"), filter.carStatus()));
            }

            if(filter.manufacturer() != null || filter.model() != null){
                Join<Car, Model> model = root.join("model");
                if(filter.manufacturer() != null){
                    predicates.add(builder.equal(
                            builder.lower(model.get("manufacturer")), filter.manufacturer().toLowerCase()));
                }
                if(filter.model() != null){
                    predicates.add(builder.equal(
                            builder.lower(model.get("name")), filter.model().toLowerCase()));
                }
            }

            if(filter.bodyType() != null){
                predicates.add(hasComponent(root, query, builder, ComponentType.BODY, BodyOption.class,
                        body -> List.of(builder.equal(body.get("bodyType"), filter.bodyType()))));
            }

            if(filter.driveType() != null){
                predicates.add(hasComponent(root, query, builder, ComponentType.DRIVE, DriveOption.class,
                        drive -> List.of(builder.equal(drive.get("driveType"), filter.driveType()))));
            }

            if(filter.transmissionType() != null){
                predicates.add(hasComponent(root, query, builder, ComponentType.TRANSMISSION, TransmissionOption.class,
                        transmission -> List.of(
                                builder.equal(transmission.get("transmissionType"), filter.transmissionType()))));
            }

            if(filter.fuelType() != null || filter.minPowerHp() != null || filter.maxPowerHp() != null
                    || filter.minDisplacement() != null || filter.maxDisplacement() != null){
                predicates.add(hasComponent(root, query, builder, ComponentType.ENGINE, EngineOption.class,
                        engine -> engineConditions(builder, engine, filter)));
            }

            if(filter.minPrice() != null){
                predicates.add(builder.ge(priceOf(root, query, builder), filter.minPrice()));
            }

            if(filter.maxPrice() != null){
                predicates.add(builder.le(priceOf(root, query, builder), filter.maxPrice()));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static List<Predicate> engineConditions(CriteriaBuilder builder,
                                                    Root<EngineOption> engine,
                                                    CarFilter filter){
        List<Predicate> conditions = new ArrayList<>();

        if(filter.fuelType() != null){
            conditions.add(builder.equal(engine.get("fuelType"), filter.fuelType()));
        }
        if(filter.minPowerHp() != null){
            conditions.add(builder.ge(engine.get("hpPower"), filter.minPowerHp()));
        }
        if(filter.maxPowerHp() != null){
            conditions.add(builder.le(engine.get("hpPower"), filter.maxPowerHp()));
        }
        if(filter.minDisplacement() != null){
            conditions.add(builder.ge(engine.get("displacement"), filter.minDisplacement()));
        }
        if(filter.maxDisplacement() != null){
            conditions.add(builder.le(engine.get("displacement"), filter.maxDisplacement()));
        }
        return conditions;
    }

    private static <T extends ComponentOption> Predicate hasComponent(Root<Car> root,
                                                                      CriteriaQuery<?> query,
                                                                      CriteriaBuilder builder,
                                                                      ComponentType componentType,
                                                                      Class<T> optionType,
                                                                      Function<Root<T>, List<Predicate>> conditions){
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Car> car = subquery.correlate(root);
        MapJoin<Car, ComponentType, ComponentOption> components = car.joinMap("components");
        Root<T> option = subquery.from(optionType);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(components.key(), componentType));
        predicates.add(builder.equal(option.get("id"), components.value().get("id")));
        predicates.addAll(conditions.apply(option));

        return builder.exists(subquery.select(builder.literal(1L))
                .where(predicates.toArray(Predicate[]::new)));
    }

    private static Subquery<Long> priceOf(Root<Car> root,
                                          CriteriaQuery<?> query,
                                          CriteriaBuilder builder){
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Car> priced = subquery.from(Car.class);
        MapJoin<Car, ComponentType, ComponentOption> components = priced.joinMap("components");
        return subquery.select(builder.sum(components.value().get("price")))
                .where(builder.equal(priced.get("id"), root.get("id")));
    }
}

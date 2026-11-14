package neyas.itmo.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import neyas.itmo.entity.ModelFilter;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.model.Model;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ModelSpecifications {

    private ModelSpecifications(){}

    public static Specification<Model> matching(ModelFilter filter){
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.isFalse(root.get("removed")));

            if(filter == null){
                return builder.and(predicates.toArray(Predicate[]::new));
            }

            if(filter.manufacturer() != null){
                predicates.add(builder.equal(
                        builder.lower(root.get("manufacturer")), filter.manufacturer().toLowerCase()));
            }

            if(filter.name() != null){
                predicates.add(builder.equal(
                        builder.lower(root.get("name")), filter.name().toLowerCase()));
            }

            if(filter.restrictsComponents()){
                query.distinct(true);
                for(ComponentOptionId componentOptionId : filter.components()){
                    Join<Model, ComponentOption> option = root.join("availableOptions");
                    predicates.add(builder.equal(option.get("code"), componentOptionId));
                }
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}

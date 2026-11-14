package neyas.itmo.entity;

import lombok.Builder;
import neyas.itmo.entity.configuration.ComponentOptionId;

import java.util.List;
import java.util.Set;

@Builder
public record ModelFilter(
        String manufacturer,
        String name,
        Set<ComponentOptionId> components) {

    public ModelFilter {
        components = components == null ? Set.of() : Set.copyOf(components);
    }

    public static ModelFilter empty(){
        return ModelFilter.builder().build();
    }

    public static ModelFilter of(CarFilter filter){
        return ModelFilter.builder()
                .manufacturer(filter.manufacturer())
                .name(filter.model())
                .build();
    }

    public boolean restrictsComponents(){
        return !components.isEmpty();
    }
}

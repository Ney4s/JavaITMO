package neyas.itmo.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import neyas.itmo.entity.configuration.ComponentOptionId;

@Converter(autoApply = true)
public class ComponentOptionIdConverter implements AttributeConverter<ComponentOptionId, String> {

    @Override
    public String convertToDatabaseColumn(ComponentOptionId attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public ComponentOptionId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new ComponentOptionId(dbData);
    }
}

package neyas.itmo.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import neyas.itmo.entity.model.ModelId;

@Converter(autoApply = true)
public class ModelIdConverter implements AttributeConverter<ModelId, String> {

    @Override
    public String convertToDatabaseColumn(ModelId attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public ModelId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new ModelId(dbData);
    }
}

package neyas.itmo.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import neyas.itmo.entity.product.SparePartId;

@Converter(autoApply = true)
public class SparePartIdConverter implements AttributeConverter<SparePartId, String> {

    @Override
    public String convertToDatabaseColumn(SparePartId attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public SparePartId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new SparePartId(dbData);
    }
}

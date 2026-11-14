package neyas.itmo.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import neyas.itmo.entity.product.Vin;

@Converter(autoApply = true)
public class VinConverter implements AttributeConverter<Vin, String> {

    @Override
    public String convertToDatabaseColumn(Vin attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Vin convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Vin(dbData);
    }
}

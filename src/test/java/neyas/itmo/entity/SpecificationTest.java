package neyas.itmo.entity;

import neyas.itmo.Fixtures;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.configuration.Specification;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.entity.type.FuelType;
import neyas.itmo.entity.type.TransmissionType;
import neyas.itmo.exception.DomainValidationException;
import neyas.itmo.exception.IncompatibleComponentException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationTest {

    private final Model model = Fixtures.bmw320i();

    private Map<ComponentType, ComponentOptionId> baseSelection() {
        Map<ComponentType, ComponentOptionId> selection = new LinkedHashMap<>();
        model.baseConfiguration().forEach((type, option) -> selection.put(type, option.id()));
        return selection;
    }

    @Test
    void baseSpecificationCostsAsModel() {
        Specification specification = Specification.base(model);

        assertEquals(3_380_000, specification.price());
        assertEquals(0, specification.surchargeOver(model));
        assertFalse(specification.isCustomFor(model));
    }

    @Test
    void correctConfigurationSumsSurcharges() {
        Map<ComponentType, ComponentOptionId> selection = baseSelection();
        selection.put(ComponentType.STEERING_WHEEL, new ComponentOptionId("STEER-MSPORT"));
        selection.put(ComponentType.INTERIOR, new ComponentOptionId("INT-DAKOTA"));

        Specification specification = Specification.of(model, selection);

        assertEquals(135_000, specification.surchargeOver(model));
        assertEquals(3_515_000, specification.price());
        assertTrue(specification.isCustomFor(model));
    }

    @Test
    void cheaperComponentLowersPrice() {
        Map<ComponentType, ComponentOptionId> selection = baseSelection();
        selection.put(ComponentType.TRANSMISSION, new ComponentOptionId("TRANS-6MT"));

        assertEquals(-30_000, Specification.of(model, selection).surchargeOver(model));
    }

    @Test
    void rejectsComponentUnavailableForModel() {
        Map<ComponentType, ComponentOptionId> selection = baseSelection();
        selection.put(ComponentType.INTERIOR, new ComponentOptionId("INT-PERFORMANCE"));

        IncompatibleComponentException exception = assertThrows(IncompatibleComponentException.class,
                () -> Specification.of(model, selection));
        assertTrue(exception.getMessage().contains("BMW 320i"));
    }

    @Test
    void acceptsSameComponentForModelThatOffersIt() {
        Model bmw330i = Fixtures.bmw330i();
        Map<ComponentType, ComponentOptionId> selection = new LinkedHashMap<>();
        bmw330i.baseConfiguration().forEach((type, option) -> selection.put(type, option.id()));
        selection.put(ComponentType.INTERIOR, new ComponentOptionId("INT-PERFORMANCE"));

        assertEquals(160_000, Specification.of(bmw330i, selection).surchargeOver(bmw330i));
    }

    @Test
    void rejectsSelectionWithoutRequiredNode() {
        Map<ComponentType, ComponentOptionId> selection = baseSelection();
        selection.remove(ComponentType.INTERIOR);

        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> Specification.of(model, selection));
        assertTrue(exception.getMessage().contains("Иртерьер Салона"));
    }

    @Test
    void rejectsComponentPlacedInWrongNode() {
        Map<ComponentType, ComponentOption> components = new HashMap<>(model.baseConfiguration());
        components.put(ComponentType.INTERIOR, Fixtures.steeringMSport());

        assertThrows(DomainValidationException.class, () -> new Specification(components));
    }

    @Test
    void exposesCharacteristicsOfChosenComponents() {
        Specification specification = Specification.base(model);

        assertEquals(FuelType.GASOLINE, specification.fuelType());
        assertEquals(TransmissionType.AUTOMATIC, specification.transmission().transmissionType());
        assertEquals(184, specification.engine().hpPower());
        assertEquals(2.0, specification.engine().displacement());
    }

    @Test
    void followsSelectedTransmission() {
        Map<ComponentType, ComponentOptionId> selection = baseSelection();
        selection.put(ComponentType.TRANSMISSION, new ComponentOptionId("TRANS-6MT"));

        assertEquals(TransmissionType.MANUAL,
                Specification.of(model, selection).transmission().transmissionType());
    }
}

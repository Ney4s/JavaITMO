package neyas.itmo.entity;

import neyas.itmo.Fixtures;
import neyas.itmo.entity.configuration.ComponentOption;
import neyas.itmo.entity.configuration.ComponentOptionId;
import neyas.itmo.entity.model.Model;
import neyas.itmo.entity.model.ModelId;
import neyas.itmo.entity.type.ComponentType;
import neyas.itmo.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private final Model model = Fixtures.bmw320i();

    @Test
    void basePriceIsSumOfBaseComponents() {
        assertEquals(3_380_000, model.basePrice());
    }

    @Test
    void fullNameJoinsManufacturerAndName() {
        assertEquals("BMW 320i", model.fullName());
    }

    @Test
    void findsOptionByIdentifier() {
        assertTrue(model.findOption(new ComponentOptionId("INT-DAKOTA")).isPresent());
        assertTrue(model.findOption(new ComponentOptionId("INT-PERFORMANCE")).isEmpty());
    }

    @Test
    void returnsBaseComponentOfNode() {
        assertEquals("Тканевый Graphite", model.baseComponent(ComponentType.INTERIOR).name());
    }

    @Test
    void optionsOfNodeAreSortedByPrice() {
        assertEquals(2, model.optionOf(ComponentType.INTERIOR).size());
        assertEquals(70_000, model.optionOf(ComponentType.INTERIOR).getFirst().price());
        assertEquals(180_000, model.optionOf(ComponentType.INTERIOR).getLast().price());
    }

    @Test
    void surchargeIsDifferenceWithBaseComponent() {
        assertEquals(110_000, model.surchargeOf(Fixtures.interiorDakota()));
        assertEquals(25_000, model.surchargeOf(Fixtures.steeringMSport()));
        assertEquals(0, model.surchargeOf(Fixtures.interiorGraphite()));
    }

    @Test
    void rejectsBaseConfigurationWithoutRequiredNode() {
        Map<ComponentType, ComponentOption> incomplete = Fixtures.baseConfiguration();
        incomplete.remove(ComponentType.INTERIOR);

        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> new Model(Fixtures.BMW_M340I, "BMW", "M340i",
                        incomplete, new LinkedHashSet<>(incomplete.values())));
        assertTrue(exception.getMessage().contains("Иртерьер Салона"));
    }

    @Test
    void rejectsBaseComponentOutsideAvailableOptions() {
        Map<ComponentType, ComponentOption> base = Fixtures.baseConfiguration();
        Set<ComponentOption> available = new LinkedHashSet<>(base.values());
        available.remove(base.get(ComponentType.INTERIOR));

        assertThrows(DomainValidationException.class,
                () -> new Model(Fixtures.BMW_M340I, "BMW", "M340i", base, available));
    }

    @Test
    void rejectsComponentPlacedInWrongNode() {
        Map<ComponentType, ComponentOption> base = Fixtures.baseConfiguration();
        base.put(ComponentType.INTERIOR, Fixtures.steeringMSport());

        assertThrows(DomainValidationException.class,
                () -> new Model(Fixtures.BMW_M340I, "BMW", "M340i",
                        base, new LinkedHashSet<>(base.values())));
    }

    @Test
    void rejectsBlankManufacturer() {
        Map<ComponentType, ComponentOption> base = Fixtures.baseConfiguration();

        assertThrows(DomainValidationException.class,
                () -> new Model(new ModelId("BMW-M340I"), " ", "M340i",
                        base, new LinkedHashSet<>(base.values())));
    }
}

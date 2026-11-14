package neyas.itmo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CatalogApiIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAvailableCars() throws Exception {
        mockMvc.perform(get("/api/cars").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void filtersCarsByStatusOnDatabaseLevel() throws Exception {
        mockMvc.perform(get("/api/cars")
                        .param("carStatus", "IN_STOCK")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filtersCarsByManufacturerAndModel() throws Exception {
        mockMvc.perform(get("/api/cars")
                        .param("manufacturer", "BMW")
                        .param("model", "330i")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].modelCode").value("BMW-330I"));
    }

    @Test
    void rejectsModelFilterWithoutManufacturer() throws Exception {
        mockMvc.perform(get("/api/cars")
                        .param("model", "330i")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsCarById() throws Exception {
        mockMvc.perform(get("/api/cars/44444444-4444-4444-4444-444444444401")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("WBA8E9G50GNT12345"))
                .andExpect(jsonPath("$.price").value(3380000))
                .andExpect(jsonPath("$.specification.components.ENGINE.code").value("ENG-320I"));
    }

    @Test
    void returnsNotFoundForUnknownCar() throws Exception {
        mockMvc.perform(get("/api/cars/00000000-0000-0000-0000-000000000000")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deniesAccessWithoutAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsTestDriveCars() throws Exception {
        mockMvc.perform(get("/api/cars/testDriveCars").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void returnsModelsWithBaseConfiguration() throws Exception {
        mockMvc.perform(get("/api/models").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filtersModelsByComponent() throws Exception {
        mockMvc.perform(get("/api/models")
                        .param("components", "INT-PERFORMANCE")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("BMW-330I"));
    }

    @Test
    void returnsModelByCode() throws Exception {
        mockMvc.perform(get("/api/models/BMW-320I").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice").value(3380000))
                .andExpect(jsonPath("$.baseConfiguration.INTERIOR.code").value("INT-GRAPHITE"));
    }

    @Test
    void returnsSparePartsFittingToModel() throws Exception {
        mockMvc.perform(get("/api/spare-parts")
                        .param("model", "BMW-330I")
                        .with(as("WAREHOUSE_ADMIN", WAREHOUSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("SP-OIL-FILTER"));
    }

    @Test
    void exposesSwaggerUi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/cars']").exists());
    }
}

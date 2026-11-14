package neyas.itmo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class OrderApiIT extends IntegrationTestBase {

    private static final String CAR_IN_STOCK = "44444444-4444-4444-4444-444444444401";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientCreatesPurchaseOrderAndCarBecomesReserved() throws Exception {
        String id = createPurchaseOrder();

        mockMvc.perform(get("/api/orders/" + id).with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CAR_PURCHASING_ORDER"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.price").value(3380000))
                .andExpect(jsonPath("$.managerId").exists());

        mockMvc.perform(get("/api/cars/" + CAR_IN_STOCK).with(as("USER", CLIENT)))
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void purchaseOrderGoesThroughWholePipeline() throws Exception {
        String id = createPurchaseOrder();

        changeStatus(id, "APPROVED_BY_MANAGER", as("MANAGER", MANAGER));
        changeStatus(id, "AWAITING_PAYMENT", as("MANAGER", MANAGER));
        changeStatus(id, "PAID", as("MANAGER", MANAGER));
        changeStatus(id, "READY_FOR_PICKUP", as("MANAGER", MANAGER));
        changeStatus(id, "COMPLETED", as("MANAGER", MANAGER));

        mockMvc.perform(get("/api/cars/" + CAR_IN_STOCK).with(as("USER", CLIENT)))
                .andExpect(jsonPath("$.status").value("SOLD"));
    }

    @Test
    void rejectsForbiddenStatusTransition() throws Exception {
        String id = createPurchaseOrder();

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}")
                        .with(as("MANAGER", MANAGER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void canceledPurchaseOrderReturnsCarToStock() throws Exception {
        String id = createPurchaseOrder();

        mockMvc.perform(delete("/api/orders/" + id).with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/api/cars/" + CAR_IN_STOCK).with(as("USER", CLIENT)))
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    void clientCannotChangeOrderStatus() throws Exception {
        String id = createPurchaseOrder();

        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED_BY_MANAGER\"}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientSeesOwnOrders() throws Exception {
        createPurchaseOrder();

        mockMvc.perform(get("/api/orders/my").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void managerSeesOrdersByType() throws Exception {
        createPurchaseOrder();

        mockMvc.perform(get("/api/orders")
                        .param("type", "CAR_PURCHASING_ORDER")
                        .with(as("MANAGER", MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createsCustomOrderThroughConfigurator() throws Exception {
        String body = """
                {
                  "modelCode": "BMW-320I",
                  "color": "RED",
                  "options": {
                    "ENGINE": "ENG-320I",
                    "BODY": "BODY-SEDAN",
                    "DRIVE": "DRIVE-RWD",
                    "TRANSMISSION": "TR-8AT",
                    "STEERING_WHEEL": "SW-MSPORT",
                    "INTERIOR": "INT-DAKOTA"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CUSTOM_CAR_ORDER"))
                .andExpect(jsonPath("$.price").value(3515000));
    }

    @Test
    void rejectsIncompatibleComponent() throws Exception {
        String body = """
                {
                  "modelCode": "BMW-320I",
                  "color": "RED",
                  "options": {
                    "ENGINE": "ENG-320I",
                    "BODY": "BODY-SEDAN",
                    "DRIVE": "DRIVE-RWD",
                    "TRANSMISSION": "TR-8AT",
                    "STEERING_WHEEL": "SW-MSPORT",
                    "INTERIOR": "INT-PERFORMANCE"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(as("USER", CLIENT)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsConfigurationWithoutRequiredComponent() throws Exception {
        String body = """
                {
                  "modelCode": "BMW-320I",
                  "color": "RED",
                  "options": {
                    "ENGINE": "ENG-320I",
                    "BODY": "BODY-SEDAN",
                    "DRIVE": "DRIVE-RWD",
                    "TRANSMISSION": "TR-8AT",
                    "STEERING_WHEEL": "SW-MSPORT"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(as("USER", CLIENT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsSparePartOrderAndDecreasesStock() throws Exception {
        mockMvc.perform(post("/api/orders/spare-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sparePartCode\":\"SP-OIL-FILTER\",\"quantity\":5}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SPARE_PART_ORDER"))
                .andExpect(jsonPath("$.price").value(12500));

        mockMvc.perform(get("/api/spare-parts/SP-OIL-FILTER").with(as("WAREHOUSE_ADMIN", WAREHOUSE)))
                .andExpect(jsonPath("$.quantityInStock").value(115));
    }

    @Test
    void rejectsSparePartOrderOverStock() throws Exception {
        mockMvc.perform(post("/api/orders/spare-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sparePartCode\":\"SP-OIL-FILTER\",\"quantity\":500}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void configuratorReturnsBaseSpecificationAndPrice() throws Exception {
        mockMvc.perform(get("/api/models/BMW-320I/configuration").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(3380000));
    }

    @Test
    void configuratorReturnsAvailableOptionsForEveryComponentType() throws Exception {
        mockMvc.perform(get("/api/models/BMW-320I/configuration/options")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.INTERIOR.length()").value(2))
                .andExpect(jsonPath("$.TRANSMISSION.length()").value(2));
    }

    private String createPurchaseOrder() throws Exception {
        String response = mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carId\":\"" + CAR_IN_STOCK + "\"}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private void changeStatus(String orderId, String status, JwtRequestPostProcessor actor) throws Exception {
        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}")
                        .with(actor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }
}

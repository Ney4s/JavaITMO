package neyas.itmo;

import com.jayway.jsonpath.JsonPath;
import neyas.itmo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class SecurityIT extends IntegrationTestBase {

    private static final String CAR_IN_STOCK = "44444444-4444-4444-4444-444444444401";
    private static final String OTHER_CAR = "44444444-4444-4444-4444-444444444403";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @ParameterizedTest
    @ValueSource(strings = {"/api/cars", "/api/models", "/api/orders/my", "/api/users", "/api/spare-parts"})
    void requestWithoutTokenIsUnauthorized(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerIsAvailableWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void registrationIsAvailableWithoutToken() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRequest("Гость", "guest@autosalon.ru", "+79990000010")))
                .andExpect(status().isOk());
    }

    @Test
    void clientCannotChangeOrderStatus() throws Exception {
        String orderId = createPurchaseOrder(CAR_IN_STOCK, CLIENT);

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED_BY_MANAGER\"}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotReadUserList() throws Exception {
        mockMvc.perform(get("/api/users").with(as("USER", CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carId\":\"" + CAR_IN_STOCK + "\"}")
                        .with(as("MANAGER", MANAGER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotAssignVin() throws Exception {
        mockMvc.perform(post("/api/cars/" + CAR_IN_STOCK + "/vin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"WBA8E9C50KG111222\"}")
                        .with(as("USER", CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void warehouseAdminManagesTestDriveList() throws Exception {
        mockMvc.perform(post("/api/cars/" + CAR_IN_STOCK + "/test-drive")
                        .with(as("WAREHOUSE_ADMIN", WAREHOUSE)))
                .andExpect(status().isOk());
    }

    @Test
    void clientSeesOnlyOwnOrders() throws Exception {
        createPurchaseOrder(CAR_IN_STOCK, CLIENT);
        registerSecondClient();
        createPurchaseOrder(OTHER_CAR, SECOND_CLIENT);

        mockMvc.perform(get("/api/orders/my").with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/orders/my").with(as("USER", SECOND_CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void clientCannotReadForeignOrder() throws Exception {
        String orderId = createPurchaseOrder(CAR_IN_STOCK, CLIENT);
        registerSecondClient();

        mockMvc.perform(get("/api/orders/" + orderId).with(as("USER", SECOND_CLIENT)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders/" + orderId).with(as("USER", CLIENT)))
                .andExpect(status().isOk());
    }

    @Test
    void managerReadsAnyOrder() throws Exception {
        String orderId = createPurchaseOrder(CAR_IN_STOCK, CLIENT);

        mockMvc.perform(get("/api/orders/" + orderId).with(as("MANAGER", MANAGER)))
                .andExpect(status().isOk());
    }

    @Test
    void clientCannotCancelForeignOrder() throws Exception {
        String orderId = createPurchaseOrder(CAR_IN_STOCK, CLIENT);
        registerSecondClient();

        mockMvc.perform(delete("/api/orders/" + orderId).with(as("USER", SECOND_CLIENT)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/orders/" + orderId).with(as("USER", CLIENT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void adminPassesChecksOfEveryRole() throws Exception {
        mockMvc.perform(get("/api/users").with(as("ADMIN", ADMIN)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cars/testDriveCars").with(as("ADMIN", ADMIN)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cars/" + CAR_IN_STOCK + "/test-drive").with(as("ADMIN", ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void unknownRoleGetsForbidden() throws Exception {
        mockMvc.perform(get("/api/users").with(as("GUEST", CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentUserIsLinkedByKeycloakSubject() throws Exception {
        mockMvc.perform(get("/api/orders/my").with(as("USER", CLIENT)))
                .andExpect(status().isOk());

        assertEquals(UUID.nameUUIDFromBytes(CLIENT.getBytes()).toString(),
                userRepository.findByEmail(CLIENT).orElseThrow().getKeycloakId());
    }

    @Test
    void tokenOfUnknownUserIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders/my").with(as("USER", "nobody@autosalon.ru")))
                .andExpect(status().isForbidden());
    }

    private String createPurchaseOrder(String carId, String email) throws Exception {
        String response = mockMvc.perform(post("/api/orders/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"carId\":\"" + carId + "\"}")
                        .with(as("USER", email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private void registerSecondClient() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientRequest("Мария Вторая", SECOND_CLIENT, "+79990000011")))
                .andExpect(status().isOk());
    }

    private static String clientRequest(String username, String email, String phone) {
        return "{\"username\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}".formatted(username, email, phone);
    }
}

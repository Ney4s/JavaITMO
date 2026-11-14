package neyas.itmo;

import neyas.itmo.entity.type.UserRole;
import neyas.itmo.entity.user.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class IntegrationTestBase {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("autosalon")
            .withUsername("autosalon")
            .withPassword("autosalon");

    static {
        POSTGRES.start();
    }

    protected static final String CLIENT = "client@autosalon.ru";
    protected static final String SECOND_CLIENT = "client2@autosalon.ru";
    protected static final String MANAGER = "manager@autosalon.ru";
    protected static final String WAREHOUSE = "warehouse@autosalon.ru";
    protected static final String ADMIN = "admin@autosalon.ru";

    private static final Map<UserRole, String> KEYCLOAK_ROLES = Map.of(
            UserRole.CLIENT, "USER",
            UserRole.MANAGER, "MANAGER",
            UserRole.WAREHOUSE_MANAGER, "WAREHOUSE_ADMIN",
            UserRole.ADMIN, "ADMIN");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    protected static JwtRequestPostProcessor as(String role, String email) {
        return jwt()
                .jwt(builder -> builder
                        .subject(subjectOf(email))
                        .claim("email", email)
                        .claim("preferred_username", email))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    protected static void loginAs(String role, String email) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subjectOf(email))
                .claim("email", email)
                .claim("preferred_username", email)
                .build();

        AbstractAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role)), email);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected static void loginAs(User user) {
        loginAs(KEYCLOAK_ROLES.get(user.role()), user.getEmail());
    }

    protected static void logout() {
        SecurityContextHolder.clearContext();
    }

    protected static String roleOf(User user) {
        return KEYCLOAK_ROLES.get(user.role());
    }

    private static String subjectOf(String email) {
        return UUID.nameUUIDFromBytes(email.getBytes()).toString();
    }
}

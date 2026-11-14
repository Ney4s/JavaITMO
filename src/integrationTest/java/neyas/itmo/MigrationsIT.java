package neyas.itmo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationsIT extends IntegrationTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest
    @ValueSource(strings = {"users", "models", "component_options", "model_base_configuration",
            "model_available_options", "cars", "car_components", "spare_parts", "spare_part_models", "orders"})
    void migrationsCreateTable(String table) {
        Boolean exists = jdbcTemplate.queryForObject(
                "select exists (select 1 from information_schema.tables where table_name = ?)",
                Boolean.class, table);

        assertTrue(exists);
    }

    @Test
    void liquibaseRecordsAppliedChangeSets() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from databasechangelog", Integer.class);

        assertTrue(applied > 0);
    }

    @Test
    void seedFillsCatalog() {
        assertEquals(2, jdbcTemplate.queryForObject("select count(*) from models", Integer.class));
        assertEquals(13, jdbcTemplate.queryForObject("select count(*) from component_options", Integer.class));
        assertEquals(3, jdbcTemplate.queryForObject("select count(*) from cars", Integer.class));
        assertEquals(4, jdbcTemplate.queryForObject("select count(*) from users", Integer.class));
        assertEquals(2, jdbcTemplate.queryForObject("select count(*) from spare_parts", Integer.class));
    }

    @Test
    void seedFillsBaseConfigurationForEveryModel() {
        Integer rows = jdbcTemplate.queryForObject(
                "select count(*) from model_base_configuration", Integer.class);

        assertEquals(12, rows);
    }
}

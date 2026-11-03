
package longcare_api;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstitutionController {

    private final JdbcTemplate jdbcTemplate;

    public InstitutionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/institutions")
    public List<Map<String, Object>> getInstitutions() {

        String sql = """
            SELECT id, institution_name, institution_type,
                   city_code, district_code, address,
                   phone, latitude, longitude
            FROM care_institutions
            ORDER BY id
            LIMIT 20
            """;

        return jdbcTemplate.queryForList(sql);
    }
}

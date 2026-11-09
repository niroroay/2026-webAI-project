
package longcare_api;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {
        "http://localhost:5501",
        "http://127.0.0.1:5501"
})
@RestController
public class InstitutionController {

    private final JdbcTemplate jdbcTemplate;

    public InstitutionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/institutions")
    public List<Map<String, Object>> getInstitutions() {

        String sql = """
                SELECT
                    id,
                    institution_name,
                    institution_type,
                    city_code,
                    district_code,
                
                    CONCAT(SUBSTRING_INDEX(address, '市', 1), '市') AS city_name,
                    CONCAT(
                        SUBSTRING_INDEX(
                            SUBSTRING_INDEX(address, '區', 1),
                            '市', -1
                        ),
                        '區'
                    ) AS district_name,
                
                    address,
                    phone,
                    latitude,
                    longitude
                FROM care_institutions
                ORDER BY id
                LIMIT 20
                """;

        return jdbcTemplate.queryForList(sql);
    }
}

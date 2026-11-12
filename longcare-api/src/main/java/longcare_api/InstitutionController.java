
package longcare_api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public Map<String, Object> getInstitutions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String districtCode,
            @RequestParam(required = false) List<String> needs,
            @RequestParam(required = false) String keyword) {

        page = Math.max(page, 1);
        size = Math.clamp(size, 1, 10);

        int offset = (page - 1) * size;

        // 建立查詢條件
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (cityCode != null && !cityCode.isBlank()) {
            where.append("""
                    AND LPAD(CAST(i.city_code AS CHAR), 5, '0') = ?
                    """);
            params.add(cityCode);
        }

        if (districtCode != null && !districtCode.isBlank()) {
            where.append("""
                    AND LPAD(CAST(i.district_code AS CHAR), 8, '0') = ?
                    """);
            params.add(districtCode);
        }

        // 依照使用者勾選的需求篩選
        if (needs != null) {

            for (String need : needs) {

                switch (need) {

                    case "居家服務" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name = '居家服務'
                            )
                            """);

                    case "日間照顧" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name = '日間照顧服務(BB)'
                            )
                            """);

                    case "住宿機構" -> where.append("""
                            AND i.institution_type IN ('3', 'A3')
                            """);

                    case "喘息服務" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name = '喘息服務'
                            )
                            """);

                    case "交通接送" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name IN (
                                    '交通接送服務',
                                    '交通接送服務(DA01)',
                                    '社區式交通接送服務(BD03)'
                                )
                            )
                            """);

                    case "輔具服務" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name = '輔具及居家無障礙環境改善服務'
                            )
                            """);

                    case "餐飲服務" -> where.append("""
                            AND EXISTS (
                                SELECT 1 FROM care_services cs
                                WHERE cs.institution_id = i.id
                                AND cs.service_name = '營養餐飲服務'
                            )
                            """);
                }
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            where.append("""
                    AND (
                        i.institution_name LIKE ?
                        OR i.address LIKE ?
                        OR i.phone LIKE ?
                    )
                    """);

            String searchKeyword = "%" + keyword.trim() + "%";

            params.add(searchKeyword);
            params.add(searchKeyword);
            params.add(searchKeyword);
        }

        // 計算符合條件的總筆數
        String countSql = """
                SELECT COUNT(*)
                FROM care_institutions i
                """ + where;

        Long totalResult = jdbcTemplate.queryForObject(
                countSql,
                Long.class,
                params.toArray()
        );

        long total = totalResult != null ? totalResult : 0L;

        // 查詢當頁機構資料
        String sql = """
                SELECT
                    i.id,
                    i.institution_name,
                    i.institution_type,
                
                    LPAD(CAST(i.city_code AS CHAR), 5, '0') AS city_code,
                    LPAD(CAST(i.district_code AS CHAR), 8, '0') AS district_code,
                
                    CASE LPAD(CAST(i.city_code AS CHAR), 5, '0')
                        WHEN '09007' THEN '連江縣'
                        WHEN '09020' THEN '金門縣'
                        WHEN '10002' THEN '宜蘭縣'
                        WHEN '10004' THEN '新竹縣'
                        WHEN '10005' THEN '苗栗縣'
                        WHEN '10007' THEN '彰化縣'
                        WHEN '10008' THEN '南投縣'
                        WHEN '10009' THEN '雲林縣'
                        WHEN '10010' THEN '嘉義縣'
                        WHEN '10013' THEN '屏東縣'
                        WHEN '10014' THEN '臺東縣'
                        WHEN '10015' THEN '花蓮縣'
                        WHEN '10016' THEN '澎湖縣'
                        WHEN '10017' THEN '基隆市'
                        WHEN '10018' THEN '新竹市'
                        WHEN '10020' THEN '嘉義市'
                        WHEN '63000' THEN '臺北市'
                        WHEN '64000' THEN '高雄市'
                        WHEN '65000' THEN '新北市'
                        WHEN '66000' THEN '臺中市'
                        WHEN '67000' THEN '臺南市'
                        WHEN '68000' THEN '桃園市'
                        ELSE '其他縣市'
                    END AS city_name,
                
                    d.district_name,
                    i.address,
                    i.phone,
                    i.latitude,
                    i.longitude
                
                FROM care_institutions i
                
                LEFT JOIN care_districts d
                    ON LPAD(CAST(i.district_code AS CHAR), 8, '0')
                       COLLATE utf8mb4_0900_ai_ci
                       = d.district_code COLLATE utf8mb4_0900_ai_ci
                
                """ + where + """
                
                ORDER BY i.id
                LIMIT ? OFFSET ?
                """;

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add(offset);

        List<Map<String, Object>> institutions =
                jdbcTemplate.queryForList(sql, queryParams.toArray());

        // 取得每間機構的服務項目
        for (Map<String, Object> institution : institutions) {

            Long institutionId =
                    ((Number) institution.get("id")).longValue();

            String serviceSql = """
                    SELECT service_name
                    FROM care_services
                    WHERE institution_id = ?
                    ORDER BY service_name
                    """;

            List<String> serviceList = jdbcTemplate.queryForList(
                    serviceSql,
                    String.class,
                    institutionId
            );

            institution.put("services", serviceList);
        }

        // 回傳分頁資料
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("data", institutions);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        int totalPages = (int) ((total + size - 1) / size);

        result.put("totalPages", totalPages);
        return result;
    }

    @GetMapping("/api/institutions/districts")
    public List<Map<String, Object>> getDistricts(
            @RequestParam String cityCode) {

        String sql = """
                SELECT
                    district_code,
                    city_code,
                    district_name
                FROM care_districts
                WHERE city_code = ?
                ORDER BY district_code
                """;

        return jdbcTemplate.queryForList(sql, cityCode);
    }
}

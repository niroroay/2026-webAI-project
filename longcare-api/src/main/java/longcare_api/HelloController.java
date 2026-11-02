package longcare_api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final JdbcTemplate jdbcTemplate;
    private final CsvDownloadService csvDownloadService;

    // 建構子：讓 Spring Boot 自動提供需要的物件
    public HelloController(
            JdbcTemplate jdbcTemplate,
            CsvDownloadService csvDownloadService) {

        this.jdbcTemplate = jdbcTemplate;
        this.csvDownloadService = csvDownloadService;
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "長照資訊網後端啟動成功！";
    }

    @GetMapping("/api/db-test")
    public String dbTest() {
        return jdbcTemplate.queryForObject(
                "SELECT DATABASE()", String.class);
    }

    @GetMapping("/api/csv-test")
    public String csvTest() throws Exception {
        return csvDownloadService.downloadAndPreview();
    }
}

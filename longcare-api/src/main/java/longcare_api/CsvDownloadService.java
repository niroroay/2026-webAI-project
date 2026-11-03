
package longcare_api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CsvDownloadService {

    private static final String CSV_URL =
            "https://ltcpap.mohw.gov.tw/publish/abc.csv";

    private final JdbcTemplate jdbcTemplate;

    public CsvDownloadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String downloadAndPreview() throws Exception {

        int institutionCount = 0;
        int serviceCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        URI.create(CSV_URL).toURL().openStream(),
                        StandardCharsets.UTF_8))) {

            // 移除 CSV 開頭可能存在的 BOM
            reader.mark(1);
            if (reader.read() != '\uFEFF') {
                reader.reset();
            }

            // 解析 CSV，第一列作為欄位名稱
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreSurroundingSpaces(true)
                    .get();

            try (CSVParser parser = format.parse(reader)) {

                // 確認必要欄位存在
                if (!parser.getHeaderNames().contains("機構代碼")
                        || !parser.getHeaderNames().contains("機構名稱")) {
                    throw new IllegalArgumentException(
                            "CSV 欄位不符合預期：" + parser.getHeaderNames());
                }

                // 每次匯入前先清除舊服務項目
                jdbcTemplate.update("DELETE FROM care_services");

                for (CSVRecord record : parser) {

                    String code = value(record, "機構代碼");
                    String name = value(record, "機構名稱");

                    if (code == null || name == null) {
                        continue;
                    }

                    // 新增機構；代碼已存在時更新資料
                    jdbcTemplate.update("""
                        INSERT INTO care_institutions
                        (institution_code, institution_name,
                         institution_type, city_code, district_code,
                         address, longitude, latitude, abc_type,
                         phone, email, manager_name,
                         contract_start, contract_end, source_updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                        institution_name = VALUES(institution_name),
                        institution_type = VALUES(institution_type),
                        city_code = VALUES(city_code),
                        district_code = VALUES(district_code),
                        address = VALUES(address),
                        longitude = VALUES(longitude),
                        latitude = VALUES(latitude),
                        abc_type = VALUES(abc_type),
                        phone = VALUES(phone),
                        email = VALUES(email),
                        manager_name = VALUES(manager_name),
                        contract_start = VALUES(contract_start),
                        contract_end = VALUES(contract_end),
                        source_updated_at = VALUES(source_updated_at)
                        """,
                            code,
                            name,
                            value(record, "機構種類"),
                            value(record, "縣市"),
                            value(record, "區"),
                            value(record, "地址全址"),
                            decimalValue(record, "經度"),
                            decimalValue(record, "緯度"),
                            value(record, "O_ABC"),
                            value(record, "機構電話"),
                            value(record, "電子郵件"),
                            value(record, "機構負責人姓名"),
                            dateValue(record, "特約起日"),
                            dateValue(record, "特約迄日"),
                            dateTimeValue(record, "最後異動時間")
                    );

                    // 取得機構在資料庫中的 id
                    Long institutionId = jdbcTemplate.queryForObject(
                            "SELECT id FROM care_institutions " +
                                    "WHERE institution_code = ?",
                            Long.class,
                            code
                    );

                    // 寫入服務項目
                    String serviceName = value(record, "特約服務項目");

                    if (serviceName != null) {
                        jdbcTemplate.update("""
                            INSERT INTO care_services
                            (institution_id, service_name,
                             contract_city_code, contract_district,
                             contract_start, contract_end)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                                institutionId,
                                serviceName,
                                value(record, "特約縣市"),
                                value(record, "特約區域"),
                                dateValue(record, "特約起日"),
                                dateValue(record, "特約迄日")
                        );

                        serviceCount++;
                    }

                    institutionCount++;
                }
            }
        }

        return "CSV 匯入完成！\n"
                + "處理資料筆數：" + institutionCount + "\n"
                + "寫入服務項目筆數：" + serviceCount;
    }

    // 讀取文字欄位
    private String value(CSVRecord record, String column) {
        String value = record.get(column).trim();
        return value.isEmpty() ? null : value;
    }

    // 讀取經緯度
    private Double decimalValue(CSVRecord record, String column) {
        String value = value(record, column);

        if (value == null) {
            return null;
        }

        return Double.valueOf(value);
    }

    // 將 yyyyMMdd 轉成資料庫日期
    private Date dateValue(CSVRecord record, String column) {
        String value = value(record, column);

        if (value == null || !value.matches("\\d{8}")) {
            return null;
        }

        return Date.valueOf(LocalDate.parse(
                value,
                DateTimeFormatter.BASIC_ISO_DATE
        ));
    }

    // 將 yyyyMMdd HH:mm:ss 轉成資料庫時間
    private Timestamp dateTimeValue(
            CSVRecord record, String column) {

        String value = value(record, column);

        if (value == null) {
            return null;
        }

        return Timestamp.valueOf(
                LocalDateTime.parse(
                        value,
                        DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")
                )
        );
    }
}

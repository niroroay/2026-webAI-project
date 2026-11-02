
package longcare_api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class CsvDownloadService {

    private static final String CSV_URL =
            "https://ltcpap.mohw.gov.tw/publish/abc.csv";

    public String downloadAndPreview() throws Exception {

        URL url = URI.create(CSV_URL).toURL();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        url.openStream(),
                        StandardCharsets.UTF_8))) {

            StringBuilder result = new StringBuilder();
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null
                    && count < 6) {

                result.append(line).append("\n");
                count++;
            }

            return result.toString();
        }
    }
}

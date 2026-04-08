import java.util.*;

public class MultipartParser {

    public static Map<String, String> parseMultipart(String body, String boundary) {
        Map<String, String> result = new HashMap<>();

        String[] parts = body.split("--" + boundary);

        for (String part : parts) {
            if (part.contains("Content-Disposition")) {
                String[] lines = part.split("\r\n");
                String fieldName = null;
                StringBuilder value = new StringBuilder();
                boolean inValue = false;

                for (String line : lines) {
                    if (line.contains("Content-Disposition") && line.contains("name=\"")) {
                        int start = line.indexOf("name=\"") + 6;
                        int end = line.indexOf("\"", start);
                        fieldName = line.substring(start, end);
                    } else if (line.isEmpty()) {
                        inValue = true;
                    } else if (inValue && !line.contains("--")) {
                        value.append(line);
                    }
                }

                if (fieldName != null) {
                    result.put(fieldName, value.toString().trim());
                }
            }
        }

        return result;
    }
}
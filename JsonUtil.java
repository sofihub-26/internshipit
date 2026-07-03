import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(entry.getKey())).append('"').append(':');
            sb.append(toJsonValue(entry.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    public static String toJson(Iterable<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Map<String, Object> item : list) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(toJson(item));
        }
        sb.append(']');
        return sb.toString();
    }

    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return '"' + escape(value.toString()) + '"';
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static Map<String, String> fromJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) {
            return map;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        boolean inQuotes = false;
        boolean escape = false;
        StringBuilder current = new StringBuilder();
        String key = null;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (escape) {
                current.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && c == ':' && key == null) {
                key = current.toString().trim();
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                current.setLength(0);
                continue;
            }
            if (!inQuotes && c == ',' && key != null) {
                String value = current.toString().trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(unescape(key), unescape(value));
                key = null;
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (key != null) {
            String value = current.toString().trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            map.put(unescape(key), unescape(value));
        }
        return map;
    }

    public static List<Map<String, Object>> fromJsonArray(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (json == null) {
            return list;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return list;
        }
        trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        boolean inQuotes = false;
        boolean escape = false;
        int braceDepth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (escape) {
                current.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                current.append(c);
                continue;
            }
            if (c == '"') {
                inQuotes = !inQuotes;
            }
            if (!inQuotes && c == '{') {
                braceDepth++;
            }
            if (!inQuotes && c == '}') {
                braceDepth--;
            }
            current.append(c);
            if (!inQuotes && braceDepth == 0 && c == '}') {
                list.add(parseObject(current.toString()));
                current.setLength(0);
            }
        }
        return list;
    }

    private static Map<String, Object> parseObject(String json) {
        Map<String, String> raw = fromJson(json);
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            String value = entry.getValue();
            if (value.matches("^-?\\d+$")) {
                result.put(entry.getKey(), Integer.parseInt(value));
            } else if (value.matches("^-?\\d+\\.\\d+$")) {
                result.put(entry.getKey(), Double.parseDouble(value));
            } else if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                result.put(entry.getKey(), Boolean.parseBoolean(value));
            } else if (value.equals("null")) {
                result.put(entry.getKey(), null);
            } else {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
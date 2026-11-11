package org.json;

import java.util.*;

public class JSONObject {
    private final Map<String, Object> map = new LinkedHashMap<>();

    public JSONObject() {}

    public JSONObject(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        jsonStr = jsonStr.trim();
        if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1).trim();
            if (jsonStr.isEmpty()) return;
            // Parse top-level keys
            int len = jsonStr.length();
            int i = 0;
            while (i < len) {
                while (i < len && (jsonStr.charAt(i) == ' ' || jsonStr.charAt(i) == ',')) i++;
                if (i >= len) break;
                if (jsonStr.charAt(i) != '"') break;
                int keyStart = i + 1;
                int keyEnd = jsonStr.indexOf('"', keyStart);
                if (keyEnd == -1) break;
                String key = jsonStr.substring(keyStart, keyEnd);
                i = keyEnd + 1;
                while (i < len && (jsonStr.charAt(i) == ' ' || jsonStr.charAt(i) == ':')) i++;
                if (i >= len) break;
                // Parse value
                char c = jsonStr.charAt(i);
                if (c == '"') {
                    int valStart = i + 1;
                    int valEnd = valStart;
                    while (valEnd < len) {
                        if (jsonStr.charAt(valEnd) == '"' && jsonStr.charAt(valEnd - 1) != '\\') break;
                        valEnd++;
                    }
                    String val = jsonStr.substring(valStart, valEnd).replace("\\\"", "\"").replace("\\n", "\n");
                    map.put(key, val);
                    i = valEnd + 1;
                } else if (c == '{') {
                    int depth = 1;
                    int valStart = i;
                    i++;
                    while (i < len && depth > 0) {
                        if (jsonStr.charAt(i) == '{') depth++;
                        else if (jsonStr.charAt(i) == '}') depth--;
                        i++;
                    }
                    map.put(key, new JSONObject(jsonStr.substring(valStart, i)));
                } else if (c == '[') {
                    int depth = 1;
                    int valStart = i;
                    i++;
                    while (i < len && depth > 0) {
                        if (jsonStr.charAt(i) == '[') depth++;
                        else if (jsonStr.charAt(i) == ']') depth--;
                        i++;
                    }
                    map.put(key, new JSONArray(jsonStr.substring(valStart, i)));
                } else {
                    int valStart = i;
                    while (i < len && jsonStr.charAt(i) != ',' && jsonStr.charAt(i) != '}') i++;
                    String raw = jsonStr.substring(valStart, i).trim();
                    if ("true".equalsIgnoreCase(raw)) map.put(key, Boolean.TRUE);
                    else if ("false".equalsIgnoreCase(raw)) map.put(key, Boolean.FALSE);
                    else if ("null".equalsIgnoreCase(raw)) map.put(key, null);
                    else {
                        try {
                            if (raw.contains(".")) map.put(key, Double.parseDouble(raw));
                            else map.put(key, Integer.parseInt(raw));
                        } catch (Exception e) {
                            map.put(key, raw);
                        }
                    }
                }
            }
        }
    }

    public JSONObject put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object opt(String key) { return map.get(key); }
    public String optString(String key) { return optString(key, ""); }
    public String optString(String key, String def) {
        Object val = map.get(key);
        return val != null ? val.toString() : def;
    }

    public int optInt(String key) { return optInt(key, 0); }
    public int optInt(String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (Exception ignored) {}
        }
        return def;
    }

    public double optDouble(String key, double def) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (Exception ignored) {}
        }
        return def;
    }

    public boolean optBoolean(String key, boolean def) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return def;
    }

    public JSONObject optJSONObject(String key) {
        Object val = map.get(key);
        if (val instanceof JSONObject) return (JSONObject) val;
        return null;
    }

    public JSONArray optJSONArray(String key) {
        Object val = map.get(key);
        if (val instanceof JSONArray) return (JSONArray) val;
        return null;
    }

    public boolean has(String key) { return map.containsKey(key); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            Object val = e.getValue();
            if (val == null) sb.append("null");
            else if (val instanceof String) sb.append("\"").append(escape((String) val)).append("\"");
            else if (val instanceof Number || val instanceof Boolean || val instanceof JSONObject || val instanceof JSONArray) sb.append(val);
            else sb.append("\"").append(escape(val.toString())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

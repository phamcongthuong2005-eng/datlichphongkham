package org.json;

import java.util.*;

public class JSONArray {
    private final List<Object> list = new ArrayList<>();

    public JSONArray() {}

    public JSONArray(Collection<?> col) {
        if (col != null) list.addAll(col);
    }

    public JSONArray(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) return;
        jsonStr = jsonStr.trim();
        if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1).trim();
            if (jsonStr.isEmpty()) return;
            // Parse elements
            int len = jsonStr.length();
            int i = 0;
            while (i < len) {
                while (i < len && (jsonStr.charAt(i) == ' ' || jsonStr.charAt(i) == ',')) i++;
                if (i >= len) break;
                char c = jsonStr.charAt(i);
                if (c == '{') {
                    int depth = 1;
                    int valStart = i;
                    i++;
                    while (i < len && depth > 0) {
                        if (jsonStr.charAt(i) == '{') depth++;
                        else if (jsonStr.charAt(i) == '}') depth--;
                        i++;
                    }
                    list.add(new JSONObject(jsonStr.substring(valStart, i)));
                } else if (c == '"') {
                    int valStart = i + 1;
                    int valEnd = valStart;
                    while (valEnd < len) {
                        if (jsonStr.charAt(valEnd) == '"' && jsonStr.charAt(valEnd - 1) != '\\') break;
                        valEnd++;
                    }
                    list.add(jsonStr.substring(valStart, valEnd));
                    i = valEnd + 1;
                } else {
                    int valStart = i;
                    while (i < len && jsonStr.charAt(i) != ',' && jsonStr.charAt(i) != ']') i++;
                    String raw = jsonStr.substring(valStart, i).trim();
                    list.add(raw);
                }
            }
        }
    }

    public JSONArray put(Object value) {
        list.add(value);
        return this;
    }

    public int length() { return list.size(); }

    public JSONObject optJSONObject(int index) {
        if (index >= 0 && index < list.size()) {
            Object val = list.get(index);
            if (val instanceof JSONObject) return (JSONObject) val;
        }
        return null;
    }

    public Object get(int index) {
        return list.get(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            first = false;
            if (item == null) sb.append("null");
            else if (item instanceof String) sb.append("\"").append(item.toString().replace("\"", "\\\"")).append("\"");
            else sb.append(item);
        }
        sb.append("]");
        return sb.toString();
    }
}

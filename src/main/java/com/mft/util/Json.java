/*
 * Copyright 2026 Srinivas Adilapuram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mft.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal, dependency-free JSON writer/reader used by the reference REST
 * API. Intentionally supports only the flat, controlled request/response
 * schemas used by this project's endpoints (string, number and boolean
 * fields) rather than being a general-purpose JSON library — this keeps
 * the reference implementation buildable with zero external dependencies.
 */
public final class Json {

    private Json() {}

    public static final class ObjectBuilder {
        private final Map<String, Object> fields = new LinkedHashMap<>();

        public ObjectBuilder put(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        public String build() {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append('"').append(escape(entry.getKey())).append("\":");
                sb.append(renderValue(entry.getValue()));
            }
            return sb.append("}").toString();
        }

        private static String renderValue(Object value) {
            if (value == null) return "null";
            if (value instanceof Number || value instanceof Boolean) return value.toString();
            return '"' + escape(value.toString()) + '"';
        }
    }

    public static ObjectBuilder object() {
        return new ObjectBuilder();
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Extracts a flat, top-level string field value from a simple JSON object body. */
    public static String extractString(String json, String field) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) return null;
        return unescape(m.group(1));
    }

    /** Extracts a flat, top-level numeric field value from a simple JSON object body. */
    public static Double extractNumber(String json, String field) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)").matcher(json);
        if (!m.find()) return null;
        return Double.parseDouble(m.group(1));
    }

    /** Extracts a flat, top-level boolean field value from a simple JSON object body. */
    public static Boolean extractBoolean(String json, String field) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)").matcher(json);
        if (!m.find()) return null;
        return Boolean.parseBoolean(m.group(1));
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

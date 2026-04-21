package com.imarouter.sdk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() {}

    public static Object parse(String json) {
        return new Parser(json).parse();
    }

    public static String toJson(Object value) {
        StringBuilder builder = new StringBuilder();
        writeJson(value, builder, false, 0);
        return builder.toString();
    }

    public static String toPrettyJson(Object value) {
        StringBuilder builder = new StringBuilder();
        writeJson(value, builder, true, 0);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return null;
    }

    public static String asString(Object value) {
        return value instanceof String string ? string : null;
    }

    public static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    public static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public static Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return null;
    }

    public static Boolean asBoolean(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private static void writeJson(Object value, StringBuilder builder, boolean pretty, int level) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof String string) {
            builder.append('"').append(escape(string)).append('"');
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }

        if (value instanceof Map<?, ?> map) {
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                if (pretty) {
                    builder.append('\n');
                    indent(builder, level + 1);
                }
                builder.append('"').append(escape(String.valueOf(entry.getKey()))).append('"').append(':');
                if (pretty) {
                    builder.append(' ');
                }
                writeJson(entry.getValue(), builder, pretty, level + 1);
            }
            if (pretty && !map.isEmpty()) {
                builder.append('\n');
                indent(builder, level);
            }
            builder.append('}');
            return;
        }

        if (value instanceof Iterable<?> iterable) {
            builder.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                if (pretty) {
                    builder.append('\n');
                    indent(builder, level + 1);
                }
                writeJson(item, builder, pretty, level + 1);
            }
            if (pretty && iterable.iterator().hasNext()) {
                builder.append('\n');
                indent(builder, level);
            }
            builder.append(']');
            return;
        }

        builder.append('"').append(escape(String.valueOf(value))).append('"');
    }

    private static void indent(StringBuilder builder, int level) {
        builder.append("  ".repeat(Math.max(level, 0)));
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        builder.append(String.format("\\u%04x", (int) ch));
                    } else {
                        builder.append(ch);
                    }
                }
            }
        }
        return builder.toString();
    }

    private static final class Parser {
        private final String json;
        private int index;

        private Parser(String json) {
            this.json = json == null ? "" : json;
        }

        private Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != json.length()) {
                throw new IllegalArgumentException("Unexpected trailing content at index " + index);
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= json.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON input");
            }

            char ch = json.charAt(index);
            return switch (ch) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return object;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                object.put(key, parseValue());
                skipWhitespace();
                if (peek('}')) {
                    expect('}');
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> array = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return array;
            }

            while (true) {
                array.add(parseValue());
                skipWhitespace();
                if (peek(']')) {
                    expect(']');
                    return array;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (index < json.length()) {
                char ch = json.charAt(index++);
                if (ch == '"') {
                    return builder.toString();
                }
                if (ch != '\\') {
                    builder.append(ch);
                    continue;
                }
                if (index >= json.length()) {
                    throw new IllegalArgumentException("Invalid string escape at index " + index);
                }
                char escaped = json.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> builder.append(escaped);
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> builder.append(parseUnicodeEscape());
                    default -> throw new IllegalArgumentException("Unsupported escape \\" + escaped + " at index " + index);
                }
            }
            throw new IllegalArgumentException("Unterminated string literal");
        }

        private char parseUnicodeEscape() {
            if (index + 4 > json.length()) {
                throw new IllegalArgumentException("Invalid unicode escape at index " + index);
            }
            String hex = json.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object parseLiteral(String literal, Object value) {
            if (!json.startsWith(literal, index)) {
                throw new IllegalArgumentException("Expected " + literal + " at index " + index);
            }
            index += literal.length();
            return value;
        }

        private Number parseNumber() {
            int start = index;
            while (index < json.length()) {
                char ch = json.charAt(index);
                if ((ch >= '0' && ch <= '9') || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E') {
                    index++;
                } else {
                    break;
                }
            }

            String token = json.substring(start, index);
            if (token.contains(".") || token.contains("e") || token.contains("E")) {
                return Double.parseDouble(token);
            }
            try {
                return Integer.parseInt(token);
            } catch (NumberFormatException ignored) {
                return Long.parseLong(token);
            }
        }

        private void skipWhitespace() {
            while (index < json.length()) {
                char ch = json.charAt(index);
                if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                    index++;
                    continue;
                }
                return;
            }
        }

        private boolean peek(char expected) {
            return index < json.length() && json.charAt(index) == expected;
        }

        private void expect(char expected) {
            if (index >= json.length() || json.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at index " + index);
            }
            index++;
        }
    }
}

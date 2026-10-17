package com.AngryHomies.AngryBirds;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Developer event log, written as JSON lines to {@code $ANGRYHOMIES_DEMO/events.jsonl} only in
 * demo mode. Records every rule-state change, launch, special move, hit, break, score change,
 * save/load and periodic per-entity snapshots, so a run can be audited after the fact.
 */
public final class DevLog {
    private static Writer writer;
    private static float clock;

    private DevLog() {
    }

    public static boolean enabled() {
        return Demo.enabled();
    }

    public static void tick(float delta) {
        clock += delta;
    }

    /** @param fields alternating key, value pairs; values are numbers, booleans or strings */
    public static void event(String type, Object... fields) {
        if (!enabled()) {
            return;
        }
        StringBuilder line = new StringBuilder(128);
        line.append("{\"t\":").append(String.format(Locale.ROOT, "%.3f", clock)).append(",\"type\":\"").append(type).append('"');
        for (int i = 0; i + 1 < fields.length; i += 2) {
            line.append(",\"").append(fields[i]).append("\":").append(json(fields[i + 1]));
        }
        line.append("}\n");
        write(line.toString());
    }

    private static String json(Object value) {
        if (value instanceof Float || value instanceof Double) {
            return String.format(Locale.ROOT, "%.3f", ((Number) value).doubleValue());
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof String && ((String) value).startsWith("[[")) {
            return (String) value; // pre-built JSON array
        }
        return "\"" + String.valueOf(value).replace("\"", "'") + "\"";
    }

    private static synchronized void write(String text) {
        try {
            if (writer == null) {
                writer = new FileWriter(System.getenv("ANGRYHOMIES_DEMO") + "/events.jsonl", false);
            }
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            writer = null;
        }
    }
}

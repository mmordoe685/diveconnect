package com.diveconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class EmojiStripMigration implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    /** Rangos Unicode que se consideran emoji para esta limpieza. */
    private static final Pattern EMOJI = Pattern.compile(
        "[\\x{1F300}-\\x{1FAFF}" +      // pictographs
        "\\x{2600}-\\x{26FF}"  +        // miscellaneous symbols
        "\\x{2700}-\\x{27BF}"  +        // dingbats
        "\\x{1F000}-\\x{1F2FF}" +       // mahjong / playing cards
        "\\x{FE0F}]");                  // variation selector

    /** Tabla y columna a limpiar. */
    private record Target(String table, String idColumn, String column) {}

    private static final List<Target> TARGETS = List.of(
        new Target("publicaciones", "id", "contenido"),
        new Target("comentarios",   "id", "contenido"),
        new Target("historias",     "id", "texto"),
        new Target("usuarios",      "id", "biografia"),
        new Target("usuarios",      "id", "descripcion_empresa"),
        new Target("centros_buceo", "id", "descripcion")
    );

    @Override
    public void run(String... args) {
        int total = 0;
        for (Target t : TARGETS) {
            total += stripTable(t);
        }
        if (total > 0) {
            log.info("EmojiStripMigration: limpiados {} registros", total);
        }
    }

    private int stripTable(Target t) {
        String selectSql = "SELECT " + t.idColumn() + ", " + t.column() +
                           " FROM " + t.table() +
                           " WHERE " + t.column() + " IS NOT NULL";
        try {
            var rows = jdbc.queryForList(selectSql);
            int updated = 0;
            for (var row : rows) {
                Object idRaw = row.get(t.idColumn());
                String text = (String) row.get(t.column());
                if (text == null) continue;
                String cleaned = stripEmojis(text);
                if (!cleaned.equals(text)) {
                    jdbc.update(
                        "UPDATE " + t.table() + " SET " + t.column() + " = ? WHERE " + t.idColumn() + " = ?",
                        cleaned, idRaw);
                    updated++;
                }
            }
            return updated;
        } catch (Exception e) {
            // Si la tabla o columna no existe (DDL en evolución), seguimos.
            log.debug("EmojiStripMigration skipped {}.{}: {}", t.table(), t.column(), e.getMessage());
            return 0;
        }
    }

    static String stripEmojis(String text) {
        String s = EMOJI.matcher(text).replaceAll("");
        // Espacios huérfanos antes de signos de puntuación
        s = s.replaceAll(" ([,.;:!?])", "$1");
        // Dobles espacios resultantes (no toca tabuladores)
        s = s.replaceAll(" {2,}", " ");
        // Espacios en los extremos de cada línea
        s = s.replaceAll("(?m)[ \\t]+$", "");
        return s.trim();
    }
}

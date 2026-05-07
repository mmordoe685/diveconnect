package com.diveconnect.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmojiStripMigrationTest {

    @Test
    @DisplayName("Texto sin emojis: queda intacto")
    void textoSinEmojis_quedaIntacto() {
        String input = "Una inmersión maravillosa en Cabo de Gata.";
        assertThat(EmojiStripMigration.stripEmojis(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("Quita emoji de pictographs (U+1F300-1FAFF)")
    void emojiPictograph_seQuita() {
        String input = "Buceo en Tabarca 🐠 con visibilidad 20 m";
        String esperado = "Buceo en Tabarca con visibilidad 20 m";
        assertThat(EmojiStripMigration.stripEmojis(input)).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Quita emoji de miscellaneous symbols (U+2600-26FF)")
    void emojiMiscSymbols_seQuita() {
        String input = "Hace sol ☀ y la temperatura es buena";
        assertThat(EmojiStripMigration.stripEmojis(input))
                .isEqualTo("Hace sol y la temperatura es buena");
    }

    @Test
    @DisplayName("Quita variation selector (U+FE0F) y dingbats (U+2700)")
    void variationSelectorYDingbats_seQuitan() {
        String input = "Aprobado ✔️ por todos";
        assertThat(EmojiStripMigration.stripEmojis(input))
                .isEqualTo("Aprobado por todos");
    }

    @Test
    @DisplayName("Compacta dobles espacios resultantes")
    void dobleEspacio_seCompacta() {
        String input = "Hola 🐠 mundo";
        // tras quitar 🐠 quedaría "Hola  mundo" → debe compactarse a "Hola mundo"
        assertThat(EmojiStripMigration.stripEmojis(input))
                .isEqualTo("Hola mundo");
    }

    @Test
    @DisplayName("Quita espacio antes de signo de puntuación")
    void espacioAntesPuntuacion_seQuita() {
        String input = "Increíble 🎉, qué inmersión.";
        assertThat(EmojiStripMigration.stripEmojis(input))
                .isEqualTo("Increíble, qué inmersión.");
    }

    @Test
    @DisplayName("Multiples emojis seguidos: todos se quitan")
    void multiplesEmojis_todosSeQuitan() {
        String input = "Día perfecto 🌊🤿🐠🦈 ¡a 30 metros!";
        assertThat(EmojiStripMigration.stripEmojis(input))
                .isEqualTo("Día perfecto ¡a 30 metros!");
    }

    @Test
    @DisplayName("Salto de línea preserva pero limpia trailing whitespace")
    void saltoDeLinea_preservaSinTrailing() {
        String input = "Línea uno 🐠   \nLínea dos 🦑";
        String resultado = EmojiStripMigration.stripEmojis(input);
        assertThat(resultado).contains("\n");
        assertThat(resultado).doesNotContain("   \n");
    }
}

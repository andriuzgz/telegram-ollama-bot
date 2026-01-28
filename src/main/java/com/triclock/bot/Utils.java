package com.triclock.bot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Utils {

    // Limpieza básica de texto
    public static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ");
    }

    // Filtro de lenguaje
    private static final String[] noSlur = {
        "tonto", "idiota", "estúpido", "puta", "mierda",
        "fuck", "shit", "bitch", "polla", "gilipollas", "coño"
    };

    public static boolean badWord(String texto) {
        if (texto == null) return false;

        String textoLower = texto.toLowerCase();
        for (String palabra : noSlur) {
            if (textoLower.contains(palabra)) {
                return true;
            }
        }
        return false;
    }

    // Generar ID de evento (2 dígitos, único)
    public static String generarIdEvento(Set<String> existentes) {
        String id;
        do {
            id = String.format("%02d", (int) (Math.random() * 100));
        } while (existentes.contains(id));
        return id;
    }

    // Null-safe
    public static String safe(String v) {
        return (v == null || v.isBlank()) ? "" : v;
    }

    // Boolean → Si / No
    public static String boolToSiNo(boolean v) {
        return v ? "Si" : "No";
    }

    // Limitar título
    public static String limitarTitulo(String t) {
        if (t == null) return "";
        t = limpiarTexto(t);
        return t.length() > 30 ? t.substring(0, 30) + "…" : t;
    }

    // Parseo de fecha
    public static String parseFecha(String input) {
        LocalDate hoy = LocalDate.now();

        if (input == null || input.isBlank()) {
            return hoy.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        input = input.toLowerCase();

        if (input.contains("mañana")) {
            return hoy.plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        if (input.contains("hoy")) {
            return hoy.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        // Asumimos que ya viene en formato correcto
        return input;
    }

    // Parseo de hora
    public static String parseHora(String h) {
        return (h == null || h.isBlank()) ? "--:--" : h;
    }
}

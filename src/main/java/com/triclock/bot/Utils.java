package com.triclock.bot;

public class Utils {
    
    public static String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim().replaceAll("\\s+", " ");
    }

    public static void log(String msg) {
        System.out.println("[LOG] >> " + msg);
    }

    private static final String[] noSlur = {
        "tonto", "idiota", "estúpido", "puta", "mierda", "fuck", "shit", "bitch", "polla", "gilipollas", "coño" // etc...
    };

    public static boolean contienePalabrasProhibidas(String texto) {
        String textoLower = texto.toLowerCase();
        for (String palabra : noSlur) {
            if (textoLower.contains(palabra)) {
                return true;
            }
        }
        return false;
    }
}

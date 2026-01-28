package com.triclock.bot;

public class CommandManager {

    public boolean isCommand(String text) {
        return text.startsWith("/");
    }

    public String handleCommand(String command, String chatId) {
        switch (command) {

            case "/start":
                return "¡Hola! Soy 3oClock ⏰, tu asistente de recordatorios.\n" +
                        "Usa /help para ver los comandos disponibles.";

            case "/help":
                return """
                📌 Comandos disponibles:
                /newevent → Crea un nuevo evento
                /delevent → Elimina un evento guardado
                /remindme → Ver eventos próximos
                /forgotme → Elimina todos tus eventos
                /summevent → Resume los eventos actuales
                """;

            case "/remindme":
                return obtenerEventos(chatId); // por implementar

            case "/forgotme":
                return eliminarEventos(chatId); // por implementar

            case "/newevent":
                return "✍️ Por favor, escribe el evento en este formato:\n" +
                        "`newevent: [nombre del evento] - [fecha opcional]`";

            case "/delevent":
                return "🗑️ Por favor, indica el nombre del evento a eliminar.";

            case "/summevent":
                return resumenEventos(chatId); // por implementar

            default:
                return "❌ No reconozco el comando. Usa /help para ver los comandos disponibles.";
        }
    }

    public String resumenTexto(String input) {
        // Aquí eventualmente conectarías con IA para resumir, si quieres
        return "🔍 Resumen del evento: " + input;
    }

    public String crearEvento(String texto) {
        // Esto debería guardar el evento
        return "✅ Evento registrado: " + texto;
    }

    // Métodos simulados
    private String obtenerEventos(String chatId) {
        return "📅 Aquí irían tus próximos eventos (simulado).";
    }

    private String eliminarEventos(String chatId) {
        return "🗑️ Todos tus eventos han sido eliminados (simulado).";
    }

    private String resumenEventos(String chatId) {
        return "📖 Resumen de tus eventos:\n- evento1\n- evento2 (simulado)";
    }
}

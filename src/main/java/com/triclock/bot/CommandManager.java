package com.triclock.bot;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class CommandManager {

    private final ReminderService reminderService = new ReminderService();

    public String handleCommand(String commandText, String chatId) {
        String[] partes = commandText.trim().split("\\s+", 2);
        String cmd = partes[0];
        String args = partes.length > 1 ? partes[1] : "";

        switch (cmd) {
            case "/start":
                return """
                Presentacion del bot 3oClock. ⏰

                3oClock es un asistente diseñado para la gestión de eventos
                y recordatorios personales, permitiendo la interacción tanto
                mediante comandos como mediante lenguaje natural.

                Para consultar los comandos disponibles, utilice /help.
                """;

            case "/help":
                return """
                📋 Lista de Comandos 3oClock ⏰:\n
                /newevent <titulo> [fecha] [hora] [true/false]
                /delevent <id>
                /edevent <id> campo valor
                /forgot <id>
                /remind <id>
                /list
                """;

            case "/newevent":
                return args.isBlank()
                        ? "✏️ El sintaxis correcto del comando es:\n /newevent <titulo> [fecha] [hora] [true/false]\n\n💡Ejemplo: /newevent Dentista 10-10-2026 10:00 true"
                        : crearEvento(args, chatId);

            case "/list":
                return listarEventos(chatId);

            case "/delevent":
                return borrarEventosConfirmado(chatId, args);

            case "/edevent":
                return editarEvento(chatId, args);

            case "/remind":
                return activarRecordatorio(chatId, args);

            case "/forgot":
                return desactivarRecordatorio(chatId, args);
            default:
                return "⏰ No reconozco el comando, usa /help para ver la lista de comandos. 💡";
        }
    }

    // Confirmacion antes del borrado
    public String prepararBorrado(String chatId, String args) {
        if (args.isBlank()) {
            return "⚠️ Uso correcto: /delevent <id>";
        }

        List<Evento> eventos = reminderService.obtenerEventos(chatId);

        for (Evento e : eventos) {
            if (e.getId().equals(args)) {
                return String.format("""
                ⚠️ ¿Desea borrar este evento?

                ⏰ %s | Título: %s | Fecha: %s %s | Recordatorio: %s

                /yes ✅  |  /no ❌
                """,
                    e.getId(),
                    e.getTitulo(),
                    e.getFecha(),
                    e.getHora(),
                    Utils.boolToSiNo(e.isRecordatorio())
                );
            }
        }

        return """
        ❌ No hay ningún evento con ese ID.
        Consulte la lista con /list
        """;
    }


    // Olvidar recordatorio
    private String desactivarRecordatorio(String chatId, String args) {
        if (args.isBlank()) {
            return "⚠️ Uso correcto: /forgot <id>";
        }
    
        for (Evento e : reminderService.obtenerEventos(chatId)) {
            if (e.getId().equals(args)) {
                if (!e.isRecordatorio()) {
                    return "ℹ️ El evento " + args + " no tenía recordatorio activo.";
                }
                e.setRecordatorio(false);
                reminderService.actualizarEvento(e);
                return "📴 Recordatorio del evento " + args + " desactivado.";
            }
        }
    
        return """
        ❌ No hay ningún evento con ese ID para descartarte.\n
        Consulta la lista con /list.
        """;
    }

    // Activar recordatorio
    private String activarRecordatorio(String chatId, String args) {
        if (args.isBlank()) {
            return "⚠️ Uso correcto: /remind <id>";
        }

        for (Evento e : reminderService.obtenerEventos(chatId)) {
            if (e.getId().equals(args)) {
                if (e.isRecordatorio()) {
                    return """
                    ℹ️ Este evento ya tiene recordatorio activo.
                    Si deseas quitarlo, usa /forgot %s.
                    """.formatted(args);
                }
                e.setRecordatorio(true);
                reminderService.actualizarEvento(e);
                return "🔔 Recordatorio activado para el evento " + args + ".";
            }
        }

        return """
        ❌ No hay ningún evento con ese ID para recordarte.
        Consulta la lista con /list.
        """;
    }

    // Crear evento
    private String crearEvento(String texto, String chatId) {
        texto = Utils.limpiarTexto(texto);
        String[] p = texto.split("\\s+");

        String titulo = Utils.limitarTitulo(p[0]);
        String fecha = Utils.parseFecha(p.length > 1 ? p[1] : "");
        String hora = Utils.parseHora(p.length > 2 ? p[2] : "");
        boolean rec = p.length > 3 && p[3].equalsIgnoreCase("true");

        Set<String> ids = reminderService.obtenerEventos(chatId)
                .stream().map(Evento::getId).collect(Collectors.toSet());

        String id = Utils.generarIdEvento(ids);

        Evento e = new Evento(id, chatId, titulo, fecha, hora, rec);
        reminderService.guardarEvento(e);

        return "✅ Evento creado con ID " + id;
    }

    // Listar eventos
    private String listarEventos(String chatId) {
        List<Evento> eventos = reminderService.obtenerEventos(chatId);

        if (eventos.isEmpty()) return "📭 No tienes ningun evento agendado\n⭐ Puedes crear un nuevo evento con /newevent.";

        StringBuilder sb = new StringBuilder();
        for (Evento e : eventos) {
            sb.append(String.format(
                "⏰ %s | Titulo: %s | Fecha: %s %s | Recordatorio: %s\n",
                e.getId(),
                e.getTitulo(),
                e.getFecha(),
                e.getHora(),
                Utils.boolToSiNo(e.isRecordatorio())
            ));
        }
        return sb.toString();
    }

    // Borrar eventos
    private String borrarEventosConfirmado(String chatId, String args) {
        if (args.isBlank()) return "⚠️ Por favor indique el ID.\n✏️ El sintaxis correcto del comando es: /delevent <id>\n\n💡Ejemplo: /delevent 40";

        StringBuilder sb = new StringBuilder();
        for (String id : args.split("\\s+")) {
            sb.append(reminderService.eliminarPorId(chatId, id)
                ? "🗑️ Evento " + id + " eliminado\n"
                : "❌ Evento " + id + " no existe\n");
        }
        return sb.toString();
    }

    // Editar evento
    private String editarEvento(String chatId, String args) {
        String[] p = args.split("\\s+", 3);
        if (p.length < 3) return "⚠️ Por favor indique el ID.\n✏️ El sintaxis correcto del comando es:\n/editevent <id> <campo> <valor>\n\n💡 Ejemplo: /editevent 40 titulo [Dentista]";

        String id = p[0];
        String campo = p[1];
        String valor = p[2];

        for (Evento e : reminderService.obtenerEventos(chatId)) {
            if (e.getId().equals(id)) {
                switch (campo) {
                    case "titulo" -> e.setTitulo(Utils.limitarTitulo(valor));
                    case "fecha" -> e.setFecha(Utils.parseFecha(valor));
                    case "hora" -> e.setHora(Utils.parseHora(valor));
                    case "recordatorio" -> e.setRecordatorio(valor.equalsIgnoreCase("true"));
                    default -> { return "❌ Campo no válido."; }
                }
                reminderService.actualizarEvento(e);
                return "✏️ Evento " + id + " actualizado.";
            }
        }
        return "❌ Evento no encontrado.";
    }

    public String crearEventoDesdeIA(JSONObject data, String chatId) {
        String titulo = Utils.limitarTitulo(data.optString("evento", ""));
        String fecha = Utils.parseFecha(data.optString("fecha", ""));
        String hora = Utils.parseHora(data.optString("hora", ""));
        boolean rec = data.optBoolean("importante", false);

        var ids = reminderService.obtenerEventos(chatId)
                .stream().map(Evento::getId).collect(java.util.stream.Collectors.toSet());

        String id = Utils.generarIdEvento(ids);

        Evento e = new Evento(id, chatId, titulo, fecha, hora, rec);
        reminderService.guardarEvento(e);

        return "✅ Evento creado con ID " + id;
    }

    public List<Evento> getEventos(String chatId) {
    return reminderService.obtenerEventos(chatId);
}

}

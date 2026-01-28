package com.triclock.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.github.cdimascio.dotenv.Dotenv;

public class BotHandler extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(BotHandler.class);
    private static final Dotenv dotenv = Dotenv.load();

    private final String BOT_TOKEN = dotenv.get("BOT_TOKEN");
    private final String BOT_USERNAME = "TriClockBot";

    private final OllamaService ollama = new OllamaService();
    private final CommandManager commands = new CommandManager();

    // Estados de confirmación
    private final Map<String, JSONObject> eventoPendienteCrear = new HashMap<>();
    private final Map<String, String> eventoPendienteBorrar = new HashMap<>();

    public BotHandler() {
        super(dotenv.get("BOT_TOKEN"));
        logger.info("[3oClock] Bot online -> @" + BOT_USERNAME);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String userMessage = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        logger.info("[3oClock] Msg: '{}' | chat: {}", userMessage, chatId);

        // 🚫 filtro de lenguaje
        if (Utils.badWord(userMessage)) {
            send(chatId, "⚠️ Usa un lenguaje más apropiado 🙏");
            return;
        }

        // ✅ confirmar creación
        if (userMessage.equalsIgnoreCase("/yes") && eventoPendienteCrear.containsKey(chatId)) {
            JSONObject data = eventoPendienteCrear.remove(chatId);
            send(chatId, commands.crearEventoDesdeIA(data, chatId));
            return;
        }

        // ✅ confirmar borrado
        if (userMessage.equalsIgnoreCase("/yes") && eventoPendienteBorrar.containsKey(chatId)) {
            String id = eventoPendienteBorrar.remove(chatId);
            send(chatId, commands.handleCommand("/delevent " + id, chatId));
            return;
        }

        // ❌ cancelar cualquier operación
        if (userMessage.equalsIgnoreCase("/no")) {
            eventoPendienteCrear.remove(chatId);
            eventoPendienteBorrar.remove(chatId);
            send(chatId, "❌ Operación cancelada.");
            return;
        }

        // 📌 comandos directos
        if (userMessage.startsWith("/")) {
            send(chatId, commands.handleCommand(userMessage, chatId));
            return;
        }

        // 🧠 lenguaje natural
        send(chatId, "🤖 Analizando su solicitud...");

        JSONObject data = ollama.analizarIntencion(userMessage);
        String intent = data.optString("intencion");
        String response;
        String textoLower = userMessage.toLowerCase();
            
        // Corrección semántica defensiva
        if (intent.equals("eliminar")) {
            if (textoLower.contains("recordatorio")
                || textoLower.contains("aviso")
                || textoLower.contains("alarma")) {
                intent = "recordatorio";
            }
        }
        

        switch (intent) {

            // -------- CREAR --------
            case "crear" -> {
                String preview = construirVistaPrevia(data);

                if (preview.isBlank()) {
                    response = """
                    🤔 Entiendo que desea crear un evento,
                    pero necesito más información.

                    Ejemplo:
                    "Crea un recordatorio mañana a las 10"
                    """;
                    break;
                }

                eventoPendienteCrear.put(chatId, data);

                response = """
                ⏰ He interpretado el siguiente evento:

                %s

                ¿Desea guardarlo?
                /yes ✅  |  /no ❌
                """.formatted(preview);
            }

            // -------- ELIMINAR --------
            case "eliminar" -> {
                String id = extraerId(data);

                if ((id == null || id.isBlank()) && data.has("evento")) {
                    String posibleId = data.optString("evento").toLowerCase();
                
                    // extraer números tipo "24", "id:24", "id 24"
                    posibleId = posibleId.replaceAll("[^0-9]", "");
                
                    if (posibleId.matches("\\d{1,2}")) {
                        id = posibleId;
                    }
                }  

                if (id.isBlank()) {
                    response = """
                    ❗ No he podido identificar el ID del evento a eliminar.

                    Ejemplo:
                    "Borra el evento con id 24"
                    """;
                    break;
                }

                List<Evento> eventos = commands.getEventos(chatId);
                Evento encontrado = null;

                for (Evento e : eventos) {
                    if (e.getId().equals(id)) {
                        encontrado = e;
                        break;
                    }
                }

                if (encontrado == null) {
                    response = """
                    ❌ No existe ningún evento con ese ID.

                    Consulte la lista con /list
                    """;
                    break;
                }

                eventoPendienteBorrar.put(chatId, id);

                response = String.format("""
                ⚠️ ¿Desea borrar este evento?

                ⏰ %s | Título: %s | Fecha: %s %s | Recordatorio: %s

                /yes ✅  |  /no ❌
                """,
                        encontrado.getId(),
                        encontrado.getTitulo(),
                        encontrado.getFecha(),
                        encontrado.getHora(),
                        Utils.boolToSiNo(encontrado.isRecordatorio())
                );
            }

            case "recordatorio" -> {
                String id = extraerId(data);
                        
                if (id.isBlank()) {
                    response = """
                    ❗ No he podido identificar el ID del evento.
                
                    Ejemplo:
                    "Quita el recordatorio del evento 24"
                    """;
                    break;
                }
            
                response = commands.handleCommand("/forgot " + id, chatId);
            }

            // -------- LISTAR --------
            case "listar" -> response = commands.handleCommand("/list", chatId);

            // -------- AYUDA / DUDAS --------
            case "ayuda" -> response = commands.handleCommand("/help", chatId);

            case "duda" -> response = """
                🤔 Puede interactuar con el bot mediante lenguaje natural
                o usando comandos explícitos.

                Ejemplos:
                - "Crea un recordatorio mañana a las 10"
                - "¿Qué eventos tengo?"
                - "Borra el evento con id 24"

                También puede usar /help 📖
                """;

            default -> response = """
                ❓ No he podido interpretar su mensaje.

                Use /help o escriba algo como:
                "Crea un evento mañana a las 9"
                """;
        }

        send(chatId, response);
    }

    // Vista previa del evento (solo visual)
    private String construirVistaPrevia(JSONObject j) {
        String titulo = Utils.limitarTitulo(j.optString("evento", ""));
        String fecha = Utils.parseFecha(j.optString("fecha", ""));
        String hora = Utils.parseHora(j.optString("hora", ""));
        boolean rec = j.optBoolean("importante", false);

        if (titulo.isBlank()) return "";

        return String.format(
                "⏰ Título: %s | Fecha: %s %s | Recordatorio: %s",
                titulo,
                fecha,
                hora,
                Utils.boolToSiNo(rec)
        );
    }

    private String extraerId(JSONObject data) {
        if (data.has("ids")) {
            return data.optString("ids").replaceAll("\\D", "");
        }
        if (data.has("evento")) {
            return data.optString("evento").replaceAll("\\D", "");
        }
        return "";
    }


    private void send(String chatId, String text) {
        try {
            SendMessage msg = new SendMessage(chatId, text);
            msg.setParseMode("Markdown");
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Error sending msg", e);
        }
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }
}

package com.triclock.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotHandler extends TelegramLongPollingBot {

    public BotHandler() {
        super(System.getenv("BOT_TOKEN"));
    }
    private final String BOT_USERNAME = "TriClockBot";
    private final OllamaService osv = new OllamaService();
    private final CommandManager cmg = new CommandManager();

    // Metodos
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
        
            // 🔒 Filtro de lenguaje ofensivo del usuario
            if (Utils.contienePalabrasProhibidas(userMessage)) {
                sendSafeMessage(chatId, "Por favor, usa un lenguaje más apropiado 🙏");
                return;
            }
        
            // Primero intentamos manejarlo como un comando
            String response = cmg.handleCommand(userMessage, chatId);
        
            // Si no es comando válido, se lo mandamos al modelo de IA
            if (response == null) {
                response = osv.getResponse(userMessage);
            }
        
            // 🔒 Filtro de lenguaje ofensivo de la respuesta (por si la IA devuelve algo malo)
            if (Utils.contienePalabrasProhibidas(response)) {
                response = "He generado una respuesta inapropiada, lo siento.";
            }
        
            sendSafeMessage(chatId, response);  // usamos un método auxiliar para enviar
        }
    }

    private void sendSafeMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    

    // Metodos Getter
    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }
}

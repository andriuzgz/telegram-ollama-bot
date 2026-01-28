package main.java.com.triclock.bot;

public class Main {
    public static void main(String [] args) {

        try {
            TelegramBotsApi tba = new TelegramBotsApi(DefaultBotSession.class);
            tba.registerBot(new BotHandler());
            System.out.println("El bot 3oClock se ha Activado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
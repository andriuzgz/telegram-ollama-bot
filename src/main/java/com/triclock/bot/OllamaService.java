package com.triclock.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OllamaService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    private final String modelName = System.getenv().getOrDefault("OLLAMA_MODEL", "mistral:instruct");
    private final String ollamaURL = "http://localhost:11434/api/generate";

    public String getResponse(String prompt) {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("[OLLAMA] Inizialiting request to Ollama API");
            logger.debug("[OLLAMA] Model: {}", modelName);
            logger.debug("[OLLAMA] Prompt received ({} chars)", prompt.length());

            HttpClient client = HttpClient.newHttpClient();

            JSONObject json = new JSONObject();
            json.put("model", modelName);
            json.put("prompt", prompt);
            json.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

            logger.debug("[OLLAMA] POST {}", ollamaURL);

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            long elapsed = System.currentTimeMillis() - startTime;
            logger.debug("[OLLAMA] Reply received in {} ms", elapsed);

            return extractResponse(response.body());

        } catch (Exception e) {
            logger.error("[OLLAMA] Error in communication with model", e);
            return "⚠️ El modelo de IA no está disponible ahora mismo.";
        }
    }

    private String extractResponse(String body) {
        try {
            JSONObject json = new JSONObject(body);
            return json.getString("response").trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public JSONObject analizarIntencion(String mensajeUsuario) {

        logger.debug("[OLLAMA] Analyzing user intent: {}", mensajeUsuario);

        String prompt = """
        Devuelve SOLO un JSON válido en una sola línea. Sin texto extra.

        {
          "intencion": "crear|eliminar|listar|consultar|ayuda|duda|desconocida",
          "evento": "",
          "fecha": "",
          "hora": "",
          "descripcion": "",
          "importante": false
        }

        Definiciones:
        - "crear": el usuario quiere crear o agendar un evento.
        - "eliminar": quiere borrar o cancelar un evento.
        - "listar": pide ver todos sus eventos.
        - "consultar": pregunta si tiene eventos en una fecha concreta.
        - "ayuda": pide ayuda, instrucciones o soporte.
        - "duda": expresa incertidumbre o no sabe qué hacer.
        - "desconocida": no se puede interpretar.

        Reglas:
        - Si el usuario pregunta con frases como: "tengo algo mañana?", "creo que tenía algo", "no sé si tengo eventos" usa "consultar" o "duda".
        - NO ejecutes acciones si la intención es "duda" o "ayuda".
        - No inventes datos.
        - No expliques nada.
        - No uses markdown.
        - Si un campo no existe, usa "" o false.

        Reglas sobre el recordatorio:
        - Solo marca "importante": true si el usuario EXPRESA explícitamente que quiere recordatorio.
        - Si el usuario dice "no hace falta", "sin recordatorio", "no quiero recordatorio", "no es necesario", entonces "importante" debe ser false.
        - Si no se menciona el recordatorio, usa false por defecto.

        Reglas para eliminar:
        - Si la intención es "eliminar" y el usuario menciona un número, colócalo en el campo "evento" sin texto adicional.

        Mensaje del usuario:
        %s
        """.formatted(mensajeUsuario);

        try {
            String respuesta = getResponse(prompt);

            System.out.println("[OLLAMA RAW] " + respuesta);

            int start = respuesta.indexOf('{');
            int end = respuesta.lastIndexOf('}');

            if (start >= 0 && end > start) {
                String jsonString = respuesta.substring(start, end + 1);
                return new JSONObject(jsonString);
            }

            throw new RuntimeException("JSON inválido");

        } catch (Exception e) {
            logger.error("[OLLAMA] Error of analyzing user intention", e);

            return new JSONObject()
                .put("intencion", "desconocida")
                .put("evento", "")
                .put("fecha", "")
                .put("hora", "")
                .put("importante", false)
                .put("descripcion", "");
        }
    }
}

package com.triclock.bot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class OllamaService {

    // Atributos
    private final String modelName = System.getenv().getOrDefault("OLLAMA_MODEL", "mistral:instruct");
    private final String ollamaURL = "http://localhost:11434/api/generate";

    public String getResponse(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Crear JSON limpio y seguro
            JSONObject json = new JSONObject();
            json.put("model", modelName);
            json.put("prompt", prompt);
            json.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return extractResponse(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al conectar al modelo.";
        }
    }

    private String extractResponse(String body) {
        try {
            JSONObject json = new JSONObject(body);
            return json.getString("response").replace("\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
            return "No puedo redactar una respuesta válida.";
        }
    }
}


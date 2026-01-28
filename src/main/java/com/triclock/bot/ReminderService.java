package com.triclock.bot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReminderService {

    private static final String FILE_PATH = "recordatorios.json";

    // Guardar un nuevo recordatorio
    public void guardarRecordatorio(String chatId, String evento) {
        JSONArray recordatorios = leerTodos();

        JSONObject nuevo = new JSONObject();
        nuevo.put("chatId", chatId);
        nuevo.put("evento", evento);

        recordatorios.put(nuevo);
        escribirJSON(recordatorios);
    }

    // Leer todos los recordatorios
    public JSONArray leerTodos() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new JSONArray();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
            return new JSONArray(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    // Obtener los recordatorios por chat ID
    public List<String> obtenerPorChatId(String chatId) {
        JSONArray all = leerTodos();
        List<String> eventos = new ArrayList<>();

        for (int i = 0; i < all.length(); i++) {
            JSONObject r = all.getJSONObject(i);
            if (r.getString("chatId").equals(chatId)) {
                eventos.add(r.getString("evento"));
            }
        }
        return eventos;
    }

    // Escribir el array actualizado al archivo
    private void escribirJSON(JSONArray array) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(array.toString(2)); // bonito formato
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

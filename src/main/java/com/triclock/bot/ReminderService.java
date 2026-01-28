package com.triclock.bot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderService {

    private static final String FILE_PATH = "recordatorios.json";

    // Guardar evento
    public void guardarEvento(Evento e) {
        JSONArray arr = leerTodos();

        JSONObject obj = new JSONObject();
        obj.put("id", e.getId());
        obj.put("chatId", e.getChatId());
        obj.put("titulo", e.getTitulo());
        obj.put("fecha", e.getFecha());
        obj.put("hora", e.getHora());
        obj.put("recordatorio", e.isRecordatorio());

        arr.put(obj);
        escribirJSON(arr);
    }

    // Leer JSON completo
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

    // Obtener eventos por chatId (con migración legacy)
    public List<Evento> obtenerEventos(String chatId) {
        JSONArray arr = leerTodos();
        List<Evento> lista = new ArrayList<>();
        boolean migrar = false;

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.has("id")) ids.add(o.getString("id"));
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (!o.getString("chatId").equals(chatId)) continue;

            // legacy
            if (!o.has("id")) {
                String nuevoId = Utils.generarIdEvento(ids);
                ids.add(nuevoId);

                o.put("id", nuevoId);
                o.put("titulo", o.optString("evento", "Evento"));
                o.put("fecha", Utils.parseFecha(""));
                o.put("hora", "--:--");
                o.put("recordatorio", false);
                o.remove("evento");

                migrar = true;
            }

            lista.add(new Evento(
                o.getString("id"),
                chatId,
                o.getString("titulo"),
                o.getString("fecha"),
                o.getString("hora"),
                o.getBoolean("recordatorio")
            ));
        }

        if (migrar) escribirJSON(arr);
        return lista;
    }

    // Eliminar todos los eventos del usuario
    public void eliminarTodosDeChat(String chatId) {
        JSONArray arr = leerTodos();
        JSONArray nuevo = new JSONArray();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (!o.getString("chatId").equals(chatId)) {
                nuevo.put(o);
            }
        }
        escribirJSON(nuevo);
    }

    // Eliminar por ID
    public boolean eliminarPorId(String chatId, String id) {
        JSONArray arr = leerTodos();
        JSONArray nuevo = new JSONArray();
        boolean eliminado = false;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.getString("chatId").equals(chatId) &&
                o.getString("id").equals(id)) {
                eliminado = true;
            } else {
                nuevo.put(o);
            }
        }

        if (eliminado) escribirJSON(nuevo);
        return eliminado;
    }

    // Actualizar evento por ID
    public boolean actualizarEvento(Evento actualizado) {
        JSONArray arr = leerTodos();
        boolean encontrado = false;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.getString("id").equals(actualizado.getId()) &&
                o.getString("chatId").equals(actualizado.getChatId())) {

                o.put("titulo", actualizado.getTitulo());
                o.put("fecha", actualizado.getFecha());
                o.put("hora", actualizado.getHora());
                o.put("recordatorio", actualizado.isRecordatorio());
                encontrado = true;
                break;
            }
        }

        if (encontrado) escribirJSON(arr);
        return encontrado;
    }

    private void escribirJSON(JSONArray array) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(array.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

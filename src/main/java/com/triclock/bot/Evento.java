package com.triclock.bot;

public class Evento {

    private String id;        // 2 dígitos
    private String chatId;
    private String titulo;    // limitado
    private String fecha;     // DD-MM-YYYY
    private String hora;      // HH:mm o "--:--"
    private boolean recordatorio;

    public Evento(String id, String chatId, String titulo, String fecha, String hora, boolean recordatorio) {
        this.id = id;
        this.chatId = chatId;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.recordatorio = recordatorio;
    }

    public String getId() { return id; }
    public String getChatId() { return chatId; }
    public String getTitulo() { return titulo; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public boolean isRecordatorio() { return recordatorio; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setHora(String hora) { this.hora = hora; }
    public void setRecordatorio(boolean recordatorio) { this.recordatorio = recordatorio; }
}

package ProyectoF;

import java.time.LocalDate; // <-- Importante sumar este import

public class Jugador {
    public static final int eloInicial = 1200;
    private int id;
    private String nombre;
    private int elo;
    private LocalDate fechaRegistro; // <-- El nuevo atributo

 // Constructor para jugadores NUEVOS de hoy (usa la constante automáticamente)
    public Jugador(String nombre) {
        this.nombre = nombre;
        this.elo = eloInicial; // <-- Ahora sí puede usarla acá
        this.fechaRegistro = LocalDate.now(); // Por defecto hoy
    }
    

    // Constructor para jugadores HISTÓRICOS (vos le elegís la fecha exacta del pasado)
    public Jugador(String nombre, int elo, LocalDate fechaRegistro) {
        this.nombre = nombre;
        this.elo = elo;
        this.fechaRegistro = fechaRegistro;
    }

    // Constructor completo para cuando traemos datos de la BD
    public Jugador(int id, String nombre, int elo, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.elo = elo;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters para la fecha
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public String showJugador() {
        return "Nombre: " + nombre + ", ELO: " + elo + ", Registrado el: " + fechaRegistro;
    }
}
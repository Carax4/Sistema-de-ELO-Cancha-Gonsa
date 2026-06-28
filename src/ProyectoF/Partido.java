package ProyectoF;

import java.time.LocalDate;

public class Partido {
    private int id;
    private LocalDate fecha;
    private String descripcion;
    private int resultadoEquipoA;
    private int resultadoEquipoB;

    // 1. Constructor para partidos NUEVOS de hoy
    // (Por si estás jugando el torneo en tiempo real y querés registrar el resultado de hoy rápido)
    public Partido(String descripcion, int resultadoEquipoA, int resultadoEquipoB) {
        this.fecha = LocalDate.now(); // Clava la fecha actual automáticamente
        this.descripcion = descripcion;
        this.resultadoEquipoA = resultadoEquipoA;
        this.resultadoEquipoB = resultadoEquipoB;
    }

    // 2. Constructor para partidos HISTÓRICOS (Tu caso de uso para la migración)
    // Te permite elegir una fecha del pasado para cargar el año y medio de historial
    public Partido(LocalDate fecha, String descripcion, int resultadoEquipoA, int resultadoEquipoB) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.resultadoEquipoA = resultadoEquipoA;
        this.resultadoEquipoB = resultadoEquipoB;
    }

    // 3. Constructor COMPLETO
    // Lo va a usar el PartidoDAO cuando traiga los datos de Postgres con el ID autogenerado
    public Partido(int id, LocalDate fecha, String descripcion, int resultadoEquipoA, int resultadoEquipoB) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.resultadoEquipoA = resultadoEquipoA;
        this.resultadoEquipoB = resultadoEquipoB;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getResultadoEquipoA() { return resultadoEquipoA; }
    public void setResultadoEquipoA(int resultadoEquipoA) { this.resultadoEquipoA = resultadoEquipoA; }

    public int getResultadoEquipoB() { return resultadoEquipoB; }
    public void setResultadoEquipoB(int resultadoEquipoB) { this.resultadoEquipoB = resultadoEquipoB; }

    // --- Método de visualización ---
    public String showPartido() {
        return "Partido ID: " + id + " [" + fecha + "] - " + descripcion + " | Resultado: " + resultadoEquipoA + " - " + resultadoEquipoB;
    }
}
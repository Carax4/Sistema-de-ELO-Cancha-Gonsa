package ProyectoF;

public class JugadorRankingDTO {
    private int id;
    private String nombre;
    private int elo;
    private int partidosJugados;
    private int ultimaVariacion;

    public JugadorRankingDTO(int id, String nombre, int elo, int partidosJugados, int ultimaVariacion) {
        this.id = id;
        this.nombre = nombre;
        this.elo = elo;
        this.partidosJugados = partidosJugados;
        this.ultimaVariacion = ultimaVariacion;
    }

    // Getters necesarios para el programa
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getElo() { return elo; }
    public int getPartidosJugados() { return partidosJugados; }
    public int getUltimaVariacion() { return ultimaVariacion; }
}
package ProyectoF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PartidoDAO {
    
    private final String url = "jdbc:postgresql://localhost:5432/sistema_futbol";
    private final String usuario = "postgres";
    private final String contrasenia = "casalean4";

    private Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasenia);
    }

    // CAMBIO 1: Ahora devuelve un int (el ID generado) en vez de ser void
    public int guardar(Partido partido) {
        String sql = "INSERT INTO partidos (fecha, descripcion, resultado_equipo_a, resultado_equipo_b) VALUES (?, ?, ?, ?);";
        
        // Agregamos Statement.RETURN_GENERATED_KEYS para pedirle a JDBC que capture el ID serial de Postgres
        try (Connection con = obtenerConexion();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(partido.getFecha()));
            stmt.setString(2, partido.getDescripcion());
            stmt.setInt(3, partido.getResultadoEquipoA());
            stmt.setInt(4, partido.getResultadoEquipoB());
            
            stmt.executeUpdate();
            
            // Leemos el ID que generó la base de datos
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    System.out.println("¡Partido registrado con éxito en el historial! ID: " + idGenerado);
                    return idGenerado; // Devolvemos el ID correcto
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error al guardar el partido: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Si algo falla, devuelve -1 como bandera de error
    }

    // CAMBIO 2: El nuevo método para la tabla intermedia con tus campos exactos
    public void guardarEstadisticaJugador(int partidoId, int jugadorId, char equipo, int goles, int golesEnContra, int eloAntes, int eloDespues) {
        String sql = "INSERT INTO estadisticas_partido (partido_id, jugador_id, equipo, goles, goles_en_contra, elo_antes_partido, elo_despues_partido) VALUES (?, ?, ?, ?, ?, ?, ?);";
        
        try (Connection con = obtenerConexion(); 
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            stmt.setInt(1, partidoId);
            stmt.setInt(2, jugadorId);
            stmt.setString(3, String.valueOf(equipo));
            stmt.setInt(4, goles);
            stmt.setInt(5, golesEnContra); // <-- LA NUEVA COLUMNA
            stmt.setInt(6, eloAntes);
            stmt.setInt(7, eloDespues);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al guardar estadística del jugador ID " + jugadorId + ": " + e.getMessage());
        }
    }
}
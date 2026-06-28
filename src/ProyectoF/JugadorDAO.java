package ProyectoF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // para el ResultSet
import java.sql.SQLException;
import java.time.LocalDate; // <-- ESTE ES EL QUE TE FALTA SUMAR
import java.util.ArrayList; // para el ArrayList
import java.util.List; // para la interfaz List

public class JugadorDAO {

	// Datos de configuración inmutables para este objeto DAO
	private final String url = "jdbc:postgresql://localhost:5432/sistema_futbol";
	private final String usuario = "postgres";
	private final String contrasenia = "casalean4";

	// Método privado para abrir el canal de comunicación con postgres
	private Connection obtenerConexion() throws SQLException {
		return DriverManager.getConnection(url, usuario, contrasenia);
	}

	// Método profesional para insertar jugadores (nuevos o históricos)
	public int guardar(Jugador jugador) {
        String sql = "INSERT INTO jugadores (nombre, elo_actual, fecha_registro) VALUES (?, ?, ?);";

        // Agregamos Statement.RETURN_GENERATED_KEYS igual que hiciste en PartidoDAO
        try (Connection con = obtenerConexion(); 
             PreparedStatement stmt = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, jugador.getNombre());
            stmt.setInt(2, jugador.getElo());
            stmt.setDate(3, java.sql.Date.valueOf(jugador.getFechaRegistro()));

            stmt.executeUpdate();

            // Capturamos el ID generado por el SERIAL de Postgres
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    System.out.println("¡" + jugador.getNombre() + " guardado con éxito! ID asignado: " + idGenerado);
                    return idGenerado;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al guardar el jugador: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

	public List<Jugador> listar() {
		List<Jugador> lista = new ArrayList<>();
		String sql = "SELECT id, nombre, elo_actual, fecha_registro FROM jugadores;";

		// Usamos try-with-resources para que se cierre la conexión, el stmt y el rs automáticamente 
		try (Connection con = obtenerConexion();
				PreparedStatement stmt = con.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) { // executeQuery() se usa para SELECTs

			// rs.next() Avanza a la siguiente fila. si hay una fila, devuelve true; si se terminaron, devuelve false
			while (rs.next()) {
				//Sacamos los datos de las columnas de la fila actual
				int id = rs.getInt("id");
				String nombre = rs.getString("nombre");
				int elo = rs.getInt("elo_actual");

				// Ojo acá: traemos la fecha como java.sql.Date y la transformamos a LocalDate de Java 8
				LocalDate fecha = null;
				if (rs.getDate("fecha_registro") != null) {
					fecha = rs.getDate("fecha_registro").toLocalDate();
				}

				// Usamos el tercer constructor de Jugador (el completo que recibe el ID de la BD)
				Jugador jugador = new Jugador(id, nombre, elo, fecha);

				// Lo metemos en nuestra lista
				lista.add(jugador);
			}
		} catch (SQLException e) {
			System.out.println("Error al listar los jugadores: " + e.getMessage());
			e.printStackTrace();
		}

		return lista; // Devolvemos la lista con todos los jugadores que encontramos
	}

	public int contarPartidosJugados(int jugadorId) {
	    // Usamos la tabla intermedia real 'estadisticas_partido' y el campo 'jugador_id'
	    String sql = "SELECT COUNT(*) AS total FROM estadisticas_partido WHERE jugador_id = ?;";
	    
	    try (Connection con = obtenerConexion(); 
	         PreparedStatement stmt = con.prepareStatement(sql)) {
	        
	        stmt.setInt(1, jugadorId);
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("total"); // Devolvemos la cantidad real de filas encontradas
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error al contar partidos del jugador ID " + jugadorId + ": " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return 0; // Por defecto si falla o es nuevo, devuelve 0 para activar la calibración (K inicial doble)
	}

	public void actualizarElo(Jugador jugador) {
		String sql = "UPDATE jugadores SET elo_actual = ? WHERE id = ?;";

		try (Connection con = obtenerConexion(); PreparedStatement stmt = con.prepareStatement(sql)) {

			// Inyectamos el nuevo ELO y el ID del jugador para pegarle a la fila correcta
			stmt.setInt(1, jugador.getElo()); // Si en tu clase es getElo(), recordá cambiarlo a j.getElo()
			stmt.setInt(2, jugador.getId());

			stmt.executeUpdate();
			// No ponemos un cartel de éxito acá para no saturar la consola con 10 mensajes
			// por partido

		} catch (SQLException e) {
			System.out.println("Error al actualizar el ELO del jugador " + jugador.getNombre() + ": " + e.getMessage());
		}
	}
	
	public Jugador buscarPorId(int idBuscar) {
		String sql = "SELECT id, nombre, elo_actual, fecha_registro FROM jugadores WHERE id = ?;";
		
		try (Connection con = obtenerConexion(); 
			 PreparedStatement stmt = con.prepareStatement(sql)) {
			
			stmt.setInt(1, idBuscar);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int id = rs.getInt("id");
					String nombre = rs.getString("nombre");
					int elo = rs.getInt("elo_actual");
					
					LocalDate fecha = null;
					if (rs.getDate("fecha_registro") != null) {
						fecha = rs.getDate("fecha_registro").toLocalDate();
					}
					
					// Construimos y devolvemos el objeto Jugador con sus datos reales
					return new Jugador(id, nombre, elo, fecha);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error al buscar jugador por ID: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null; // Si pusieran un ID que no existe en la base de datos, devuelve null
	}
	
	public List<JugadorRankingDTO> obtenerRanking() {
	    List<JugadorRankingDTO> lista = new ArrayList<>();
	    // Cambiado j.elo por j.elo_actual para que coincida con tu columna de Postgres
	    String sql = "SELECT j.id, j.nombre, j.elo_actual, " +
	                 "COUNT(e.partido_id) as partidos_jugados, " +
	                 "COALESCE((" +
	                 "  SELECT (e2.elo_despues_partido - e2.elo_antes_partido) " +
	                 "  FROM estadisticas_partido e2 " +
	                 "  WHERE e2.jugador_id = j.id " +
	                 "  ORDER BY e2.partido_id DESC LIMIT 1" +
	                 "), 0) as ultima_variacion " +
	                 "FROM jugadores j " + 
	                 "LEFT JOIN estadisticas_partido e ON j.id = e.jugador_id " +
	                 "GROUP BY j.id, j.nombre, j.elo_actual " + 
	                 "ORDER BY j.elo_actual DESC;"; 

	    try (Connection con = obtenerConexion();
	         PreparedStatement stmt = con.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            // Ojo: acá usamos rs.getInt("elo_actual") porque mapeamos el nombre real de la columna
	            JugadorRankingDTO dto = new JugadorRankingDTO(
	                rs.getInt("id"),
	                rs.getString("nombre"),
	                rs.getInt("elo_actual"), 
	                rs.getInt("partidos_jugados"),
	                rs.getInt("ultima_variacion")
	            );
	            lista.add(dto);
	        }
	    } catch (SQLException e) {
	        System.out.println("Error al obtener ranking: " + e.getMessage());
	    }
	    return lista;
	}
}
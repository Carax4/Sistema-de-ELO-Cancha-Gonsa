package ProyectoF;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    public static void main(String[] args) {
        // 1. La URL de conexión: le decimos que busque en nuestra compu (localhost),
        // en el puerto de Postgres (5432) y la base de datos de futbol.
        String url = "jdbc:postgresql://localhost:5432/sistema_futbol";
        String usuario = "postgres"; 
        String contrasenia = "casalean4";

        System.out.println("Intentando conectar a la base de datos...");

        // 2. Intentamos abrir el canal de comunicación
        try (Connection conexion = DriverManager.getConnection(url, usuario, contrasenia)) {
            
            if (conexion != null) {
                System.out.println("¡Conexión exitosa al proyecto de fútbol!");
            }
            
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
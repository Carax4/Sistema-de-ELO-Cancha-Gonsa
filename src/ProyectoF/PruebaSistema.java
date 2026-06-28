package ProyectoF;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//https://github.com/Carax4/Sistema-de-ELO-Cancha-Gonsa.git -> GitHub

public class PruebaSistema {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        JugadorDAO jugadorDAO = new JugadorDAO();
        PartidoDAO partidoDAO = new PartidoDAO();
        EloService eloService = new EloService();

        // Mostrar los jugadores disponibles
        System.out.println("=== JUGADORES REGISTRADOS ===");
        List<Jugador> listaCompleta = jugadorDAO.listar();
        for (Jugador j : listaCompleta) {
            System.out.println("[" + j.getId() + "] " + j.getNombre() + " (ELO Actual: " + j.getElo() + ")");
        }
        System.out.println("=============================");

        // Pedir datos generales del partido
        System.out.println("\n--- NUEVO PARTIDO ---");
        System.out.print("Fecha (YYYY-MM-DD) o presione ENTER para usar HOY: ");
        String fechaInput = scanner.nextLine().trim();
        
        System.out.print("Descripción corta del partido: ");
        String descripcion = scanner.nextLine().trim();

        LocalDate fechaPartido;
        if (fechaInput.isEmpty()) {
            fechaPartido = LocalDate.now();
        } else {
            try {
                fechaPartido = LocalDate.parse(fechaInput);
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Formato de fecha inválido. Se usará la fecha de HOY por seguridad.");
                fechaPartido = LocalDate.now();
            }
        }

        // Estructuras Equipo A (Agregamos lista para sus goles en contra)
        List<Jugador> equipoA = new ArrayList<>();
        List<Integer> golesA = new ArrayList<>();
        List<Integer> golesContraA = new ArrayList<>();

        System.out.println("\n>> EQUIPO A <<");
        cargarEquipo(scanner, jugadorDAO, equipoA, golesA, golesContraA, fechaPartido);

        // Estructuras Equipo B (Agregamos lista para sus goles en contra)
        List<Jugador> equipoB = new ArrayList<>();
        List<Integer> golesB = new ArrayList<>();
        List<Integer> golesContraB = new ArrayList<>();

        System.out.println("\n>> EQUIPO B <<");
        cargarEquipo(scanner, jugadorDAO, equipoB, golesB, golesContraB, fechaPartido);

        // --- MATEMÁTICA REAL CRUZADA ---
        int golesFavorA = equipoA.isEmpty() ? 0 : golesA.stream().mapToInt(Integer::intValue).sum();
        int golesFavorB = equipoB.isEmpty() ? 0 : golesB.stream().mapToInt(Integer::intValue).sum();
        
        int golesEnContraA = equipoA.isEmpty() ? 0 : golesContraA.stream().mapToInt(Integer::intValue).sum();
        int golesEnContraB = equipoB.isEmpty() ? 0 : golesContraB.stream().mapToInt(Integer::intValue).sum();
        int totalMarcadorA = golesFavorA + golesEnContraB;
        int totalMarcadorB = golesFavorB + golesEnContraA;

        System.out.println(" Resultado: Equipo A (" + totalMarcadorA + ") vs Equipo B (" + totalMarcadorB + ")");

        // Crear el objeto Partido maestro
        Partido partidoMaestro = new Partido(fechaPartido, descripcion, totalMarcadorA, totalMarcadorB);
        int partidoId = partidoDAO.guardar(partidoMaestro);

        if (partidoId == -1) {
            System.out.println("❌ Error crítico: No se pudo registrar el partido base. Operación abortada.");
            return;
        }

        // Fotos de ELO antes de procesar
        List<Integer> eloAntesA = new ArrayList<>();
        for (Jugador j : equipoA) { eloAntesA.add(j.getElo()); }

        List<Integer> eloAntesB = new ArrayList<>();
        for (Jugador j : equipoB) { eloAntesB.add(j.getElo()); }

        // El servicio procesa el ELO según el marcador global real
        eloService.procesarPartido(partidoMaestro, equipoA, equipoB);

        // Guardar estadísticas en la BD (Pasando goles a favor Y goles en contra)
        for (int i = 0; i < equipoA.size(); i++) {
            Jugador j = equipoA.get(i);
            int favor = golesA.get(i);
            int contra = golesContraA.get(i);
            int antes = eloAntesA.get(i);
            int despues = j.getElo();
            
            partidoDAO.guardarEstadisticaJugador(partidoId, j.getId(), 'A', favor, contra, antes, despues);
        }

        for (int i = 0; i < equipoB.size(); i++) {
            Jugador j = equipoB.get(i);
            int favor = golesB.get(i);
            int contra = golesContraB.get(i);
            int antes = eloAntesB.get(i);
            int despues = j.getElo();
            
            partidoDAO.guardarEstadisticaJugador(partidoId, j.getId(), 'B', favor, contra, antes, despues);
        }

        System.out.println("\n🏆 ¡Proceso finalizado exitosamente y datos guardados!");
        scanner.close();
    }

    // MÉTODO AUXILIAR ACTUALIZADO CON GOLES A FAVOR Y GOLES EN CONTRA INDIVIDUALES
 // MÉTODO AUXILIAR CON VALIDACIÓN PASO A PASO Y CONFIRMACIÓN POR JUGADOR
    private static void cargarEquipo(Scanner scanner, JugadorDAO jugadorDAO, List<Jugador> equipo, List<Integer> goles, List<Integer> golesContra, LocalDate fechaPartido) {
        System.out.println("(Ingrese ID 0 para terminar de cargar este equipo)");
        
        while (true) {
            System.out.print("\nID Jugador: ");
            int id;
            try {
                id = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Debe ingresar un número entero válido para el ID.");
                continue; // Vuelve a pedir el ID
            }
            
            if (id == 0) {
                break;
            }

            // --- BUSCAR O REGISTRAR JUGADOR ---
            Jugador j = jugadorDAO.buscarPorId(id);
            
            if (j == null) {
                System.out.println("⚠️ El ID " + id + " no existe en el sistema.");
                System.out.print("Presione ENTER para reintentar otro ID o escriba el NOMBRE del nuevo jugador para darlo de alta: ");
                String nombreNuevo = scanner.nextLine().trim();
                
                if (!nombreNuevo.isEmpty()) {
                    if (nombreNuevo.length() < 3) {
                        System.out.println("❌ Alta de jugador rechazada: El nombre debe tener al menos 3 caracteres.");
                        continue;
                    }
                    
                    boolean confirmadoAlta = false;
                    while (true) {
                        System.out.print("❓ ¿Está seguro que desea registrar a '" + nombreNuevo + "' con ELO inicial 1200? (S/N): ");
                        String respuesta = scanner.nextLine().trim().toUpperCase();
                        if (respuesta.equals("S")) { confirmadoAlta = true; break; }
                        else if (respuesta.equals("N")) { System.out.println("❌ Registro cancelado."); break; }
                        else { System.out.println("⚠️ Opción inválida. Responda S o N."); }
                    }
                    
                    if (confirmadoAlta) {
                        Jugador nuevo = new Jugador(nombreNuevo, 1200, fechaPartido);
                        int idGenerado = jugadorDAO.guardar(nuevo);
                        if (idGenerado != -1) {
                            j = jugadorDAO.buscarPorId(idGenerado);
                        }
                    }
                }
                
                // Si el usuario dio ENTER o canceló el alta, volvemos a pedir ID
                if (j == null) {
                    System.out.println("❌ Operación omitida. Volviendo a pedir ID...");
                    continue;
                }
            }

            // ---  BUCLE PARA GOLES A FAVOR (Bloqueante ante errores) ---
            int golesJugador = 0;
            while (true) {
                System.out.print("Goles A FAVOR lícitos de " + j.getNombre() + ": ");
                try {
                    golesJugador = Integer.parseInt(scanner.nextLine().trim());
                    if (golesJugador < 0) {
                        System.out.println("⚠️ Los goles no pueden ser negativos. Intente de nuevo.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Ingrese un número válido para los goles.");
                }
            }

            // --- BUCLE PARA GOLES EN CONTRA (Bloqueante ante errores) ---
            int golesEnContraJugador = 0;
            while (true) {
                System.out.print("¿Hizo algún GOL EN CONTRA " + j.getNombre() + " en el partido?: ");
                try {
                    golesEnContraJugador = Integer.parseInt(scanner.nextLine().trim());
                    if (golesEnContraJugador < 0) {
                        System.out.println("⚠️ La cantidad no puede ser negativa. Intente de nuevo.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Ingrese un número válido para los goles en contra.");
                }
            }

            // --- CARTEL DE CONFIRMACIÓN FINAL PARA EL JUGADOR ---
            boolean agregarAlEquipo = false;
            while (true) {
                System.out.print("❓ " + j.getNombre() + " hizo " + golesJugador + " gol(es) a favor y " + golesEnContraJugador + " en contra. ¿Confirmar datos de este jugador? (S/N): ");
                String respuesta = scanner.nextLine().trim().toUpperCase();
                
                if (respuesta.equals("S")) {
                    agregarAlEquipo = true;
                    break;
                } else if (respuesta.equals("N")) {
                    System.out.println("❌ Carga del jugador '" + j.getNombre() + "' descartada.");
                    break;
                } else {
                    System.out.println("⚠️ Opción inválida. Responda con 'S' para Sí o 'N' para No.");
                }
            }

            // Solo si se confirmó, se guarda en las estructuras temporales de Java
            if (agregarAlEquipo) {
                equipo.add(j);
                goles.add(golesJugador);
                golesContra.add(golesEnContraJugador);
                System.out.println("✅ " + j.getNombre() + " agregado exitosamente en memoria.");
            } else {
                System.out.println("🔄 Volviendo al inicio. Ingrese el ID nuevamente.");
            }
        }
    }
}
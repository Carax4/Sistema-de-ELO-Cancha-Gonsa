package ProyectoF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VerRanking {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        JugadorDAO jugadorDAO = new JugadorDAO();

        System.out.println("=== SISTEMA DE RANKING ELO ===");
        
        // 1. Preguntar por el filtro de partidos mínimos
        int minPartidos = 0;
        while (true) {
            System.out.print("Ingrese la cantidad mínima de partidos jugados para filtrar (0 para ver todos): ");
            try {
                minPartidos = Integer.parseInt(scanner.nextLine().trim());
                if (minPartidos >= 0) {
                    break;
                }
                System.out.println("⚠️ El número no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Ingrese un número entero válido.");
            }
        }

        // 2. Obtener los datos base desde la BD (Vienen ordenados por ELO Actual desc)
        List<JugadorRankingDTO> rankingActual = jugadorDAO.obtenerRanking();

        // 3. --- CÁLCULO DE POSICIONES ANTERIORES EN MEMORIA ---
        // Duplicamos la lista para calcular cómo estaba la tabla ANTES del último partido
        List<JugadorRankingDTO> rankingAnterior = new ArrayList<>(rankingActual);
        
        // Ordenamos esta lista temporal basándonos en el ELO que tenían ANTES de su última variación
        Collections.sort(rankingAnterior, new Comparator<JugadorRankingDTO>() {
            @Override
            public int compare(JugadorRankingDTO j1, JugadorRankingDTO j2) {
                int eloAntesJ1 = j1.getElo() - j1.getUltimaVariacion();
                int eloAntesJ2 = j2.getElo() - j2.getUltimaVariacion();
                return Integer.compare(eloAntesJ2, eloAntesJ1); // Descendente (Mayor a menor)
            }
        });

        // Guardamos en un mapa qué puesto ocupaba cada ID de jugador en la foto del pasado
        Map<Integer, Integer> puestosAnterioresMap = new HashMap<>();
        int puestoAnt = 1;
        for (JugadorRankingDTO j : rankingAnterior) {
            // Solo contamos en el ranking anterior a los que cumplen el filtro actual
            if (j.getPartidosJugados() < minPartidos) {
                continue; 
            }
            puestosAnterioresMap.put(j.getId(), puestoAnt);
            puestoAnt++;
        }

        // 4. --- IMPRESIÓN DE LA TABLA REAL CON COMPARACIÓN DE PUESTOS ---
        System.out.println("\n=======================================================");
        System.out.println("               TABLA DE POSICIONES HISTÓRICA           ");
        System.out.println("=======================================================");
        System.out.printf("%-4s %-18s %-8s %-10s %-10s\n", "Pos", "Jugador", "Rating", "Partidos", "Tendencia");
        System.out.println("-------------------------------------------------------");

        int puestoActual = 1;
        for (JugadorRankingDTO j : rankingActual) {
            // Aplicar el filtro dinámico
            if (j.getPartidosJugados() < minPartidos) {
                continue; 
            }

            // Regla del signo de pregunta si tiene menos de 10 partidos
            String eloMostrar = String.valueOf(j.getElo());
            if (j.getPartidosJugados() < 10) {
                eloMostrar += "?";
            }

            // DETERMINAR LA TENDENCIA DE PUESTO (A vs B)
            String flecha = "•"; // Por defecto no se movió de puesto
            
            // Si el jugador existía en la tabla filtrada anterior, comparamos puestos
            if (puestosAnterioresMap.containsKey(j.getId())) {
                int puestoQueTenia = puestosAnterioresMap.get(j.getId());
                
                if (puestoActual < puestoQueTenia) {
                    // Recordar que en un ranking, un número menor significa estar más arriba (ej: #2 es mejor que #4)
                    flecha = "▲"; 
                } else if (puestoActual > puestoQueTenia) {
                    flecha = "▼";
                }
            } else {
                // Si no tiene puesto anterior registrado (ej: fue su primer partido y antes tenía 0 partidos), 
                // aparece como "Nuevo" en el ranking filtrado
                flecha = "New"; 
            }

            // Imprimir la fila con formato prolijo alineado
            System.out.printf("#%-3d %-18s %-8s %-10d %-10s\n", 
                puestoActual, 
                j.getNombre(), 
                eloMostrar, 
                j.getPartidosJugados(),
                flecha
            );
            
            puestoActual++;
        }

        System.out.println("=======================================================");
        System.out.println("Nota: (?) Menos de 10 partidos. (▲/▼) Variación de posición en la tabla.");
        scanner.close();
    }
}
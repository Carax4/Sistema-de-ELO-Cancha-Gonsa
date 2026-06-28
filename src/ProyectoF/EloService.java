package ProyectoF;

import java.util.List;

public class EloService {

    private JugadorDAO jugadorDAO = new JugadorDAO();

    public void procesarPartido(Partido partido, List<Jugador> equipoA, List<Jugador> equipoB) {
        
        // 1. Calcular el ELO promedio de cada equipo
        double promedioA = 0;
        for (Jugador j : equipoA) promedioA += j.getElo();
        promedioA = promedioA / equipoA.size();

        double promedioB = 0;
        for (Jugador j : equipoB) promedioB += j.getElo();
        promedioB = promedioB / equipoB.size();

        //Calcular la expectativa del resultado
        double expectativaA = 1.0 / (1.0 + Math.pow(10, (promedioB - promedioA) / 400.0));
        double expectativaB = 1.0 - expectativaA;

        //Determinar el resultado (1 = Ganó, 0 = Perdió, 0.5 = Empate)
        double resultadoRealA;
        double resultadoRealB;
        
        int golesA = partido.getResultadoEquipoA();
        int golesB = partido.getResultadoEquipoB();

        if (golesA > golesB) {
            resultadoRealA = 1.0;
            resultadoRealB = 0.0;
        } else if (golesB > golesA) {
            resultadoRealA = 0.0;
            resultadoRealB = 1.0;
        } else {
            resultadoRealA = 0.5;
            resultadoRealB = 0.5;
        }

        // Modificador en caso de goleada (Goleada >= 3 -> se multiplica K por 1.5)
        int diferenciaGoles = Math.abs(golesA - golesB);
        double multiplicadorGoleada = (diferenciaGoles >= 3) ? 1.5 : 1.0;
        
        double kBasePartido = 32.0 * multiplicadorGoleada; // Puede ser 32.0 o 48.0

        System.out.println("\n--- DETALLES DEL CÁLCULO DE ELO ---");
        System.out.printf("Promedio Equipo A: %.1f | Expectativa: %.2f\n", promedioA, expectativaA);
        System.out.printf("Promedio Equipo B: %.1f | Expectativa: %.2f\n", promedioB, expectativaB);
        System.out.println("K Base para este partido: " + kBasePartido);

        //Aplicar la actualización INDIVIDUAL para el EQUIPO A
        System.out.println("\nActualizando variaciones del Equipo A:");
        for (Jugador j : equipoA) {
            // Buscamos cuántos partidos jugó en la BD para ver si está en calibración
            int partidosJugados = jugadorDAO.contarPartidosJugados(j.getId());
            double kJugador = (partidosJugados < 10) ? (kBasePartido * 2) : kBasePartido;

            // Fórmula de actualización de ELO
            int eloAnterior = j.getElo();
            int nuevoElo = (int) Math.round(eloAnterior + kJugador * (resultadoRealA - expectativaA));
            
            j.setElo(nuevoElo); // Lo cambiamos en la memoria de Java
            jugadorDAO.actualizarElo(j); // <- Próximo paso: Impactarlo en Postgres
            
            System.out.println("- " + j.getNombre() + " (Partidos: " + (partidosJugados + 1) + "): " + eloAnterior + " -> " + nuevoElo + " (" + (nuevoElo - eloAnterior) + ")");
        }

        // 6. Aplicar la actualización INDIVIDUAL para el EQUIPO B
        System.out.println("\nActualizando variaciones del Equipo B:");
        for (Jugador j : equipoB) {
            int partidosJugados = jugadorDAO.contarPartidosJugados(j.getId());
            double kJugador = (partidosJugados < 10) ? (kBasePartido * 2) : kBasePartido;
            int eloAnterior = j.getElo();
            int nuevoElo = (int) Math.round(eloAnterior + kJugador * (resultadoRealB - expectativaB));
            
            j.setElo(nuevoElo); // Lo cambiamos en la memoria de Java
            jugadorDAO.actualizarElo(j); // <- Próximo paso: Impactarlo en Postgres
            
            System.out.println("- " + j.getNombre() + " (Partidos: " + (partidosJugados + 1) + "): " + eloAnterior + " -> " + nuevoElo + " (" + (nuevoElo - eloAnterior) + ")");
        }
    }
}
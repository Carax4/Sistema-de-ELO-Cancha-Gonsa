CREATE TABLE jugadores (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    elo_actual INT DEFAULT 1200
);

CREATE TABLE partidos (
    id SERIAL PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    descripcion VARCHAR(255),
    resultado_equipo_a INT NOT NULL,
    resultado_equipo_b INT NOT NULL
);

CREATE TABLE estadisticas_partido (
    id SERIAL PRIMARY KEY,
    jugador_id INT REFERENCES jugadores(id),
    partido_id INT REFERENCES partidos(id),
    equipo CHAR(1) NOT NULL, -- 'A' o 'B'
    goles INT DEFAULT 0,
    elo_antes_partido INT NOT NULL,
    elo_despues_partido INT NOT NULL,
    goles_en_contra INT DEFAULT 0
);
-- =====================================================================
-- ServiceDesk 360 - Guía 5: esquema MySQL
-- Módulo: Desarrollo de Aplicaciones Multiplataforma (COIDS3104)
--
-- Ejecutar con una cuenta administrativa (por ejemplo root) desde
-- MySQL Workbench: File > Open SQL Script > ejecutar todo (rayo).
-- El script es reproducible: elimina y vuelve a crear las tablas.
-- =====================================================================

-- Garantiza que las tildes y la ñ se guarden correctamente.
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS servicedesk360_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE servicedesk360_db;

-- Orden inverso a las dependencias para poder recrear el esquema.
DROP TABLE IF EXISTS seguimientos;
DROP TABLE IF EXISTS tickets;
DROP TABLE IF EXISTS equipos;
DROP TABLE IF EXISTS categorias;
DROP TABLE IF EXISTS tecnicos;
DROP TABLE IF EXISTS clientes;

-- ---------------------------------------------------------------------
-- Clientes (en la aplicación MVC corresponden a Solicitante)
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
  id_cliente BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre     VARCHAR(120) NOT NULL,
  correo     VARCHAR(160) NOT NULL,
  empresa    VARCHAR(120) NULL,
  telefono   VARCHAR(20)  NULL,
  activo     BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_cliente_correo UNIQUE (correo)
);

-- ---------------------------------------------------------------------
-- Equipos de cada cliente
-- ---------------------------------------------------------------------
CREATE TABLE equipos (
  id_equipo         BIGINT PRIMARY KEY AUTO_INCREMENT,
  id_cliente        BIGINT NOT NULL,
  codigo_inventario VARCHAR(40) NOT NULL,
  tipo              VARCHAR(80) NOT NULL,
  marca             VARCHAR(80) NULL,
  modelo            VARCHAR(80) NULL,
  activo            BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_equipo_codigo UNIQUE (codigo_inventario),
  CONSTRAINT fk_equipo_cliente
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
);

-- ---------------------------------------------------------------------
-- Técnicos (nivel_soporte replica la regla 1..3 de la clase Tecnico)
-- ---------------------------------------------------------------------
CREATE TABLE tecnicos (
  id_tecnico    BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre        VARCHAR(120) NOT NULL,
  correo        VARCHAR(160) NOT NULL,
  especialidad  VARCHAR(100) NULL,
  nivel_soporte TINYINT NOT NULL DEFAULT 1,
  activo        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT uq_tecnico_correo UNIQUE (correo),
  CONSTRAINT chk_tecnico_nivel CHECK (nivel_soporte BETWEEN 1 AND 3)
);

-- ---------------------------------------------------------------------
-- Categorías de ticket
-- ---------------------------------------------------------------------
CREATE TABLE categorias (
  id_categoria BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre       VARCHAR(80) NOT NULL,
  CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

-- ---------------------------------------------------------------------
-- Tickets. Prioridad y estado usan los mismos valores que los enums
-- PrioridadTicket y EstadoTicket del proyecto (Guía 3/4).
-- ---------------------------------------------------------------------
CREATE TABLE tickets (
  id_ticket      BIGINT PRIMARY KEY AUTO_INCREMENT,
  id_cliente     BIGINT NOT NULL,
  id_equipo      BIGINT NULL,
  id_tecnico     BIGINT NULL,
  id_categoria   BIGINT NOT NULL,
  titulo         VARCHAR(150)  NOT NULL,
  descripcion    VARCHAR(1000) NOT NULL,
  prioridad      VARCHAR(20)   NOT NULL,
  estado         VARCHAR(20)   NOT NULL DEFAULT 'ABIERTO',
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ticket_cliente   FOREIGN KEY (id_cliente)   REFERENCES clientes(id_cliente),
  CONSTRAINT fk_ticket_equipo    FOREIGN KEY (id_equipo)    REFERENCES equipos(id_equipo),
  CONSTRAINT fk_ticket_tecnico   FOREIGN KEY (id_tecnico)   REFERENCES tecnicos(id_tecnico),
  CONSTRAINT fk_ticket_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
  CONSTRAINT chk_ticket_prioridad CHECK (prioridad IN ('BAJA','MEDIA','ALTA','CRITICA')),
  CONSTRAINT chk_ticket_estado    CHECK (estado IN ('ABIERTO','ASIGNADO','EN_PROCESO','RESUELTO','CERRADO')),
  CONSTRAINT chk_ticket_titulo    CHECK (CHAR_LENGTH(TRIM(titulo)) >= 5)
);

CREATE INDEX idx_ticket_estado_prioridad ON tickets(estado, prioridad);

-- ---------------------------------------------------------------------
-- Seguimientos de cada ticket
-- ---------------------------------------------------------------------
CREATE TABLE seguimientos (
  id_seguimiento BIGINT PRIMARY KEY AUTO_INCREMENT,
  id_ticket      BIGINT NOT NULL,
  detalle        VARCHAR(800) NOT NULL,
  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_seguimiento_ticket
    FOREIGN KEY (id_ticket) REFERENCES tickets(id_ticket)
);

-- =====================================================================
-- Usuario de aplicación con privilegios limitados (mínimo privilegio).
-- La aplicación NUNCA se conecta con root.
--
-- IMPORTANTE: la contraseña de abajo es solo un marcador. Antes de
-- ejecutar, reemplácela por su propia clave local y NO la suba a Git.
-- La misma clave se coloca en ~/.servicedesk360/db.properties.
-- =====================================================================
CREATE USER IF NOT EXISTS 'servicedesk_app'@'localhost'
  IDENTIFIED BY 'Cambiar_Esta_Clave_2026!';

GRANT SELECT, INSERT, UPDATE, DELETE
  ON servicedesk360_db.*
  TO 'servicedesk_app'@'localhost';

FLUSH PRIVILEGES;

-- Comprobación de privilegios (evidencia para la matriz de pruebas):
-- SHOW GRANTS FOR 'servicedesk_app'@'localhost';

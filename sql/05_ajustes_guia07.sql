-- =====================================================================
-- ServiceDesk 360 - Guía 7: ajustes de esquema e índices
-- Ejecutar con la cuenta administrativa (root) después de 01 y 02.
-- Es reproducible: si un cambio ya existe, no lo vuelve a aplicar.
-- =====================================================================

SET NAMES utf8mb4;
USE servicedesk360_db;

-- ---------------------------------------------------------------------
-- Paso 1: claves foráneas del proceso de tickets
-- (la guía usa TABLE_SCHEMA = 'servicedesk360'; la base del proyecto
--  se llama servicedesk360_db)
-- ---------------------------------------------------------------------
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME,
       REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'servicedesk360_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;

-- ---------------------------------------------------------------------
-- Ajuste 1: categorías habilitadas/deshabilitadas.
-- La regla X.1 exige "La categoría debe existir y estar habilitada",
-- pero la tabla de la Guía 5 no tenía columna de estado.
-- ---------------------------------------------------------------------
SET @existe := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'servicedesk360_db'
                  AND TABLE_NAME = 'categorias' AND COLUMN_NAME = 'activo');
SET @sql := IF(@existe = 0,
    'ALTER TABLE categorias ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT ''categorias.activo ya existe'' AS aviso');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- ---------------------------------------------------------------------
-- Paso 3: índices de apoyo.
-- MySQL ya crea un índice por cada FOREIGN KEY (id_cliente de equipos,
-- id_tecnico de tickets, id_ticket de seguimientos...), y la Guía 5 creó
-- idx_ticket_estado_prioridad (estado, prioridad). Por eso los índices
-- sugeridos idx_equipos_cliente, idx_tickets_estado, idx_tickets_tecnico
-- e idx_seguimientos_ticket serían duplicados. Se agregan solo los que
-- faltan para el listado: orden por fecha y filtro solo por prioridad.
-- ---------------------------------------------------------------------
SET @existe := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = 'servicedesk360_db' AND TABLE_NAME = 'tickets'
                  AND INDEX_NAME = 'idx_tickets_fecha_creacion');
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_tickets_fecha_creacion ON tickets(fecha_creacion)',
    'SELECT ''idx_tickets_fecha_creacion ya existe'' AS aviso');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

SET @existe := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = 'servicedesk360_db' AND TABLE_NAME = 'tickets'
                  AND INDEX_NAME = 'idx_tickets_prioridad');
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_tickets_prioridad ON tickets(prioridad)',
    'SELECT ''idx_tickets_prioridad ya existe'' AS aviso');
PREPARE st FROM @sql; EXECUTE st; DEALLOCATE PREPARE st;

-- Comprobación de índices (evidencia)
SHOW INDEX FROM tickets;
SHOW INDEX FROM equipos;
SHOW INDEX FROM seguimientos;

-- ---------------------------------------------------------------------
-- Paso 2: datos base para los catálogos
-- ---------------------------------------------------------------------
SELECT id_cliente, nombre, activo FROM clientes ORDER BY nombre;
SELECT id_equipo, id_cliente, codigo_inventario, tipo, marca, modelo, activo
FROM equipos ORDER BY codigo_inventario;
SELECT id_categoria, nombre, activo FROM categorias ORDER BY nombre;
SELECT id_tecnico, nombre, especialidad, nivel_soporte, activo FROM tecnicos ORDER BY nombre;

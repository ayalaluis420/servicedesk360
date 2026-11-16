-- =====================================================================
-- ServiceDesk 360 - Guía 5: pruebas controladas de integridad
-- Ejecutar SOLO sobre la base de laboratorio, después de 01 y 02.
--
-- Cada sentencia DEBE FALLAR. En MySQL Workbench ejecute una por una
-- (Ctrl+Enter con el cursor en la línea) y capture el mensaje del
-- panel "Output" como evidencia.
-- =====================================================================

-- Garantiza que las tildes y la ñ se guarden correctamente.
SET NAMES utf8mb4;

USE servicedesk360_db;

-- Prueba 10: clave foránea inexistente
-- Esperado: Error 1452 (23000) Cannot add or update a child row:
--           a foreign key constraint fails (... fk_equipo_cliente ...)
INSERT INTO equipos (id_cliente, codigo_inventario, tipo)
VALUES (999999, 'EQ-ERROR', 'Laptop');

-- Ejercicio 1: restricción UNIQUE (correo repetido)
-- Esperado: Error 1062 (23000) Duplicate entry 'ana.lopez@demo.local'
--           for key 'clientes.uq_cliente_correo'
INSERT INTO clientes (nombre, correo)
VALUES ('Ana Duplicada', 'ana.lopez@demo.local');

-- Restricción CHECK de dominio (prioridad no válida)
-- Esperado: Error 3819 (HY000) Check constraint 'chk_ticket_prioridad'
--           is violated.
INSERT INTO tickets (id_cliente, id_categoria, titulo, descripcion, prioridad)
VALUES (1, 1, 'Prueba de prioridad', 'Prioridad fuera del dominio.', 'URGENTE');

-- Restricción CHECK de nivel de técnico
-- Esperado: Error 3819 (HY000) Check constraint 'chk_tecnico_nivel'
--           is violated.
INSERT INTO tecnicos (nombre, correo, nivel_soporte)
VALUES ('Técnico Inválido', 'invalido@demo.local', 5);

-- No se puede eliminar un cliente que tiene equipos/tickets
-- Esperado: Error 1451 (23000) Cannot delete or update a parent row
DELETE FROM clientes WHERE id_cliente = 2;

-- El usuario de aplicación no puede modificar la estructura.
-- Conéctese como servicedesk_app y ejecute:
--   DROP TABLE seguimientos;
-- Esperado: Error 1142 (42000) DROP command denied to user
--           'servicedesk_app'@'localhost'

-- Verificación de consistencia ticket + seguimiento (Parte D, paso 16)
SELECT t.id_ticket, t.titulo, s.id_seguimiento, s.detalle
FROM tickets t
LEFT JOIN seguimientos s ON s.id_ticket = t.id_ticket
ORDER BY t.id_ticket DESC;

-- Tickets sin seguimiento (debe devolver 0 filas si no hubo
-- operaciones parciales)
SELECT t.id_ticket, t.titulo
FROM tickets t
LEFT JOIN seguimientos s ON s.id_ticket = t.id_ticket
WHERE s.id_seguimiento IS NULL;

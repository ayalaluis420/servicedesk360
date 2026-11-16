-- =====================================================================
-- ServiceDesk 360 - Guía 6: consultas de validación del CRUD de Clientes
-- (y del personal técnico, entidad propia del proyecto).
--
-- La guía lo llama 03_pruebas_crud_clientes.sql; se numeró 04 porque
-- 03_pruebas_integridad.sql ya existe desde la Guía 5.
--
-- Uso: después de cada operación en /servicedesk360/clientes, ejecute
-- la consulta correspondiente en MySQL Workbench (Ctrl+Enter) y capture
-- el resultado como evidencia.
-- =====================================================================

SET NAMES utf8mb4;
USE servicedesk360_db;

-- Paso 2: confirmar el esquema de clientes
DESCRIBE clientes;
SELECT id_cliente, nombre, correo, activo
FROM clientes
ORDER BY id_cliente;

-- Paso 26 / V03 (CREATE): el cliente nuevo aparece con su id generado
SELECT * FROM clientes ORDER BY id_cliente DESC LIMIT 5;

-- Paso 28 / V06 (UPDATE): el total no cambia y el id se conserva
SELECT COUNT(*) AS total_clientes FROM clientes;

-- Paso 29 / V05 (correo duplicado): cada correo aparece una sola vez
SELECT LOWER(correo) AS correo, COUNT(*) AS veces
FROM clientes
GROUP BY LOWER(correo)
HAVING COUNT(*) > 1;          -- debe devolver 0 filas

-- Paso 30 / V08 (DELETE físico): reemplace ? por el id eliminado
-- SELECT * FROM clientes WHERE id_cliente = ?;   -- debe devolver 0 filas

-- Paso 31 / V09 (integridad referencial) y Ejercicio 3:
-- relaciones de cada cliente. Solo los que tienen 0 pueden eliminarse.
SELECT c.id_cliente, c.nombre,
       (SELECT COUNT(*) FROM equipos e WHERE e.id_cliente = c.id_cliente) AS equipos,
       (SELECT COUNT(*) FROM tickets t WHERE t.id_cliente = c.id_cliente) AS tickets,
       c.activo
FROM clientes c
ORDER BY c.id_cliente;

-- Paso 32 / V10-V11 (desactivación lógica): el registro sigue existiendo
SELECT id_cliente, nombre, activo FROM clientes WHERE activo = FALSE;

-- Paso 33 / V12 (apóstrofe): el valor se guardó tal cual
SELECT id_cliente, nombre, correo FROM clientes WHERE nombre LIKE '%''%';

-- Entidad propia: personal técnico y sus tickets asignados
SELECT tc.id_tecnico, tc.nombre, tc.correo, tc.nivel_soporte, tc.activo,
       (SELECT COUNT(*) FROM tickets t WHERE t.id_tecnico = tc.id_tecnico) AS tickets
FROM tecnicos tc
ORDER BY tc.id_tecnico;

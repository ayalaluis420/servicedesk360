-- =====================================================================
-- ServiceDesk 360 - Guía 5: datos mínimos de prueba
-- Ejecutar después de 01_esquema_servicedesk360.sql.
-- =====================================================================

-- Garantiza que las tildes y la ñ se guarden correctamente.
SET NAMES utf8mb4;

USE servicedesk360_db;

INSERT INTO clientes (nombre, correo, empresa, telefono)
VALUES ('Ana López',     'ana.lopez@demo.local',     'Innovación S.A.',   '7000-0001'),
       ('Carlos Méndez', 'carlos.mendez@demo.local', 'Comercial El Sol',  '7000-0002');

INSERT INTO categorias (nombre)
VALUES ('Software'), ('Hardware'), ('Acceso');

INSERT INTO tecnicos (nombre, correo, especialidad, nivel_soporte)
VALUES ('María Ruiz',  'maria.ruiz@demo.local',  'Aplicaciones web', 2),
       ('Jorge Pérez', 'jorge.perez@demo.local', 'Infraestructura',  3);

INSERT INTO equipos (id_cliente, codigo_inventario, tipo, marca, modelo)
VALUES (1, 'EQ-0001', 'Laptop',     'Dell', 'Latitude 5440'),
       (2, 'EQ-0002', 'Impresora',  'HP',   'LaserJet M404');

-- Un ticket de ejemplo con su seguimiento inicial.
INSERT INTO tickets (id_cliente, id_equipo, id_tecnico, id_categoria,
                     titulo, descripcion, prioridad, estado)
VALUES (2, 2, 1, 2,
        'Impresora no imprime',
        'La impresora del área contable muestra error de papel atascado.',
        'MEDIA', 'ASIGNADO');

INSERT INTO seguimientos (id_ticket, detalle)
VALUES (LAST_INSERT_ID(), 'Ticket registrado con datos de prueba.');

-- Consultas de validación
SHOW TABLES;
DESCRIBE tickets;
SELECT * FROM clientes;
SELECT * FROM categorias;
SELECT * FROM tecnicos;
SELECT * FROM equipos;

-- =====================================================================
-- ServiceDesk 360 - Guía 7: datos de prueba para el proceso de tickets
-- Ejecutar una sola vez, con root, después de 05_ajustes_guia07.sql.
-- Paso 32 de la guía: al menos 15 tickets para filtros y paginación.
-- Todos los datos son ficticios.
-- =====================================================================

SET NAMES utf8mb4;
USE servicedesk360_db;

-- Catálogos adicionales (INSERT IGNORE: no falla si ya existen)
INSERT IGNORE INTO categorias (nombre, activo) VALUES ('Redes', TRUE);
INSERT IGNORE INTO categorias (nombre, activo) VALUES ('Telefonía (retirada)', FALSE);

INSERT IGNORE INTO equipos (id_cliente, codigo_inventario, tipo, marca, modelo)
SELECT id_cliente, 'EQ-0003', 'Monitor', 'LG', '24MK430H'
FROM clientes WHERE correo = 'ana.lopez@demo.local';

INSERT IGNORE INTO equipos (id_cliente, codigo_inventario, tipo, marca, modelo)
SELECT id_cliente, 'EQ-0004', 'Laptop', 'Lenovo', 'ThinkPad E14'
FROM clientes WHERE correo = 'carlos.mendez@demo.local';

-- Identificadores por clave natural (el script no depende de ids fijos)
SET @ana    := (SELECT id_cliente FROM clientes WHERE correo = 'ana.lopez@demo.local');
SET @carlos := (SELECT id_cliente FROM clientes WHERE correo = 'carlos.mendez@demo.local');
SET @eq1 := (SELECT id_equipo FROM equipos WHERE codigo_inventario = 'EQ-0001');
SET @eq2 := (SELECT id_equipo FROM equipos WHERE codigo_inventario = 'EQ-0002');
SET @eq3 := (SELECT id_equipo FROM equipos WHERE codigo_inventario = 'EQ-0003');
SET @eq4 := (SELECT id_equipo FROM equipos WHERE codigo_inventario = 'EQ-0004');
SET @soft := (SELECT id_categoria FROM categorias WHERE nombre = 'Software');
SET @hard := (SELECT id_categoria FROM categorias WHERE nombre = 'Hardware');
SET @acc  := (SELECT id_categoria FROM categorias WHERE nombre = 'Acceso');
SET @red  := (SELECT id_categoria FROM categorias WHERE nombre = 'Redes');
SET @maria := (SELECT id_tecnico FROM tecnicos WHERE correo = 'maria.ruiz@demo.local');
SET @jorge := (SELECT id_tecnico FROM tecnicos WHERE correo = 'jorge.perez@demo.local');

-- 16 tickets con estados y prioridades variados.
-- Los CRITICA se asignan a Jorge (nivel 3), como exige la regla del servicio.
INSERT INTO tickets (id_cliente, id_equipo, id_tecnico, id_categoria,
                     titulo, descripcion, prioridad, estado, fecha_creacion)
VALUES
 (@ana,    @eq1, @maria, @soft, 'Correo no sincroniza',        'Outlook no descarga mensajes nuevos desde ayer.',          'MEDIA',   'ABIERTO',    NOW() - INTERVAL 30 HOUR),
 (@ana,    @eq3, @maria, @hard, 'Monitor parpadea',            'El monitor parpadea al encender el equipo.',               'BAJA',    'ABIERTO',    NOW() - INTERVAL 29 HOUR),
 (@carlos, @eq2, @jorge, @hard, 'Impresora atasca papel',      'Atasco de papel constante en la bandeja 2.',               'ALTA',    'EN_PROCESO', NOW() - INTERVAL 28 HOUR),
 (@carlos, @eq4, @jorge, @red,  'Sin acceso a internet',       'La laptop no obtiene dirección IP en la oficina.',         'CRITICA', 'ABIERTO',    NOW() - INTERVAL 27 HOUR),
 (@ana,    @eq1, @maria, @acc,  'Contraseña bloqueada',        'La cuenta de dominio se bloqueó tras varios intentos.',     'ALTA',    'CERRADO',    NOW() - INTERVAL 26 HOUR),
 (@carlos, @eq4, @maria, @soft, 'Licencia de Office vencida',  'Aparece aviso de licencia vencida al abrir Excel.',         'MEDIA',   'ASIGNADO',   NOW() - INTERVAL 25 HOUR),
 (@ana,    @eq1, @jorge, @red,  'VPN se desconecta',           'La VPN se cae cada 10 minutos.',                           'ALTA',    'EN_PROCESO', NOW() - INTERVAL 24 HOUR),
 (@carlos, @eq2, @maria, @hard, 'Tóner agotado',               'Solicitan cambio de tóner negro.',                         'BAJA',    'RESUELTO',   NOW() - INTERVAL 23 HOUR),
 (@ana,    @eq3, @maria, @soft, 'Actualización de drivers',    'Se requieren drivers nuevos para el monitor.',             'BAJA',    'CERRADO',    NOW() - INTERVAL 22 HOUR),
 (@carlos, @eq4, @jorge, @soft, 'Sistema contable no abre',    'El sistema contable muestra error al iniciar.',            'CRITICA', 'EN_PROCESO', NOW() - INTERVAL 21 HOUR),
 (@ana,    @eq1, @maria, @acc,  'Permisos de carpeta',         'Necesita acceso a la carpeta compartida de ventas.',       'MEDIA',   'ABIERTO',    NOW() - INTERVAL 20 HOUR),
 (@carlos, @eq2, @maria, @red,  'Impresora fuera de red',      'La impresora no aparece en la red del área contable.',     'MEDIA',   'ABIERTO',    NOW() - INTERVAL 19 HOUR),
 (@ana,    @eq1, @jorge, @hard, 'Batería no carga',            'La batería de la laptop se queda en 0 %.',                 'ALTA',    'ABIERTO',    NOW() - INTERVAL 18 HOUR),
 (@carlos, @eq4, @maria, @acc,  'Alta de usuario nuevo',       'Crear credenciales para personal de nuevo ingreso.',       'BAJA',    'CERRADO',    NOW() - INTERVAL 17 HOUR),
 (@ana,    @eq3, @maria, @soft, 'Navegador lento',             'El navegador tarda mucho en abrir las páginas.',           'MEDIA',   'EN_PROCESO', NOW() - INTERVAL 16 HOUR),
 (@carlos, @eq2, @jorge, @hard, 'Ruido en la impresora',       'La impresora hace un ruido metálico al imprimir.',         'BAJA',    'ABIERTO',    NOW() - INTERVAL 15 HOUR);

-- Seguimiento inicial para cada ticket que todavía no tenga uno
INSERT INTO seguimientos (id_ticket, detalle, fecha_registro)
SELECT t.id_ticket, 'Ticket registrado (datos de prueba de la Guía 7).', t.fecha_creacion
FROM tickets t
LEFT JOIN seguimientos s ON s.id_ticket = t.id_ticket
WHERE s.id_seguimiento IS NULL;

-- Comprobación
SELECT estado, prioridad, COUNT(*) AS total
FROM tickets GROUP BY estado, prioridad ORDER BY estado, prioridad;

SELECT COUNT(*) AS tickets_sin_seguimiento
FROM tickets t LEFT JOIN seguimientos s ON s.id_ticket = t.id_ticket
WHERE s.id_seguimiento IS NULL;   -- debe ser 0

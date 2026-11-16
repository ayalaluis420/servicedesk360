# ServiceDesk 360

## Descripción
Caso modelo de una aplicación web para gestionar solicitudes de soporte técnico.

## Tecnologías
- Java 11 o superior
- JSP, Servlets, EL y JSTL 1.2
- Apache Tomcat 9 (javax.servlet)
- Apache NetBeans
- Maven

## Requisitos
1. JDK configurado.
2. Tomcat 9 registrado en el IDE.
3. Puerto del servidor disponible.

## Ejecución
1. Abrir el proyecto en NetBeans.
2. Limpiar y construir.
3. Ejecutar sobre Tomcat.
4. Abrir /servicedesk360 en el navegador.

## Decisiones de diseño de la semana 2 (formularios, sesión y cookies)
Se implementó el flujo de registro e inicio de acceso con `RegistroServlet` y
`AccesoServlet`: validación de campos, transformación de contraseñas con SHA-256 (solo por
continuidad didáctica), `HttpSession` para mantener al usuario autenticado y una cookie
(`ultimoUsuario`) para recordar el correo. En esa etapa los usuarios se guardaban en un
`Map<String, Map<String, String>>` en memoria, compartido mediante el `ServletContext`.

## Decisiones de diseño de la semana 3 (modelo POO y principios SOLID)

### Modelo de clases
Se reemplazaron los mapas por objetos del dominio: `Usuario` (clase abstracta) con las
especializaciones `Solicitante` y `Tecnico` por herencia, y `CuentaUsuario` y
`TicketSoporte` por composición, junto con las enumeraciones `RolUsuario`,
`PrioridadTicket` y `EstadoTicket`.

### Interfaces, servicios y almacenamiento
- `BuscadorCuentas` y `RegistradorCuentas`: interfaces pequeñas (ISP) implementadas por
  `DirectorioCuentasEnMemoria`, el único que conoce el `ConcurrentHashMap`.
- `SeguridadClave` transforma contraseñas, `ValidadorRegistro` valida entradas y
  `ServicioRegistro` coordina el caso de uso. `ServicioAutenticacion` implementa
  `Autenticador` y depende solo de `BuscadorCuentas`.
- Las dependencias se inyectan por constructor; `AplicacionListener` crea las instancias
  una sola vez y las comparte en el `ServletContext`.

### Principios SOLID aplicados
| Principio | Evidencia |
|---|---|
| SRP | `Usuario` modela datos; `ValidadorRegistro` valida; `ServicioRegistro` coordina |
| OCP | Un nuevo tipo de usuario (por ejemplo `Administrador`) se agrega como subclase sin modificar `Usuario` |
| LSP | `Solicitante` y `Tecnico` se usan donde se espera un `Usuario` sin romper su contrato |
| ISP | `BuscadorCuentas` y `RegistradorCuentas` están separados |
| DIP | Los servicios dependen de interfaces, no de `DirectorioCuentasEnMemoria` |

### Limitaciones
- El almacenamiento sigue en memoria y se reinicia con Tomcat.
- Los métodos `crearSolicitud()` y `verificarClave()` del diagrama de clases no se
  implementaron en esta etapa.

## Decisiones de diseño de la semana 4 (MVC, tickets y filtro)

### Rutas y métodos HTTP
| Ruta | Método | Controlador | Protegida | Resultado | Vista / redirección |
|---|---|---|---|---|---|
| /registro | GET | RegistroServlet | No | Muestra formulario | registro.jsp |
| /registro | POST | RegistroServlet | No | Crea cuenta o valida | redirect /acceso o forward registro.jsp |
| /acceso | GET | AccesoServlet | No | Muestra login | login.jsp |
| /acceso | POST | AccesoServlet | No | Autentica | redirect /panel o forward login.jsp |
| /panel | GET | PanelServlet | Sí | Muestra panel | /WEB-INF/views/panel.jsp |
| /tickets | GET | TicketListadoServlet | Sí | Lista tickets | /WEB-INF/views/tickets/listado.jsp |
| /tickets/nuevo | GET | TicketNuevoServlet | Sí | Muestra formulario | /WEB-INF/views/tickets/nuevo.jsp |
| /tickets/nuevo | POST | TicketNuevoServlet | Sí | Crea o valida | redirect /tickets?estado=creado o forward nuevo.jsp |
| /cerrar-sesion | POST | CerrarSesionServlet | Sí | Invalida la sesión | redirect /acceso?estado=cerrada |

### Lógica trasladada a servicios
Las reglas de creación de tickets (solo un solicitante puede abrir tickets, título de al
menos 5 caracteres, descripción de al menos 10 y prioridad válida) viven en
`ServicioTickets`. Los Servlets leen parámetros, invocan al servicio y deciden entre
forward y redirect. Ningún Servlet genera HTML ni accede a colecciones, y el servicio no
conoce `HttpServletRequest`, `HttpSession` ni las JSP.

### Vistas en WEB-INF/views
`panel.jsp` se movió a `WEB-INF/views`, y las vistas nuevas (`tickets/nuevo.jsp`,
`tickets/listado.jsp` y `error.jsp`) se crearon allí. Tomcat no sirve archivos dentro de
`WEB-INF`, así que el único camino hacia ellas es el controlador.

### Protección de rutas con AutenticacionFilter
Un solo `@WebFilter` cubre `/panel`, `/tickets`, `/tickets/*` y `/cerrar-sesion`. Obtiene
la sesión con `getSession(false)` y, si no existe `usuarioAutenticado`, redirige a
`/acceso?estado=sesion` sin ejecutar el Servlet. Los controladores de tickets no repiten
la validación de sesión; `PanelServlet` conserva la suya como segunda capa.

### Post/Redirect/Get
`TicketNuevoServlet` hace forward a `nuevo.jsp` cuando la validación falla (con errores y
valores anteriores como atributos de request) y `sendRedirect` a `/tickets?estado=creado`
cuando el POST tiene éxito. Al refrescar el listado solo se repite el GET, sin duplicar
el ticket.

### Preparación para MySQL/JDBC
Se sustituirá `DirectorioTicketsEnMemoria`. `ServicioTickets` depende de `BuscadorTickets`
y `RegistradorTickets`, por lo que bastará con crear una implementación JDBC de ambos
contratos y cambiar la instancia creada en `AplicacionListener`. Los Servlets, las JSP y
el flujo HTTP no cambian.

### Limitaciones actuales
- Tickets y cuentas viven en memoria y se pierden al reiniciar Tomcat; los IDs de ticket
  vuelven a empezar en 1.
- El mensaje "Ticket registrado correctamente" reaparece al refrescar, porque depende del
  parámetro `estado=creado` y no de un mensaje flash.
- El listado muestra todos los tickets a cualquier usuario autenticado.
- La única regla de autorización es la del servicio, que impide que un técnico cree
  tickets.
- `index.jsp`, `login.jsp` y `registro.jsp` siguen fuera de `WEB-INF`.

## Equipo
- Luis Ángel Ayala


package sv.edu.itca.servicedesk360.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Ejecuta las pruebas JDBC y transaccionales de la Guía 5 y muestra un
 * resultado por prueba (numeración de la matriz de pruebas, sección XII,
 * y de los ejercicios complementarios, sección XIV).
 *
 * <p>Requisitos: haber ejecutado sql/01 y sql/02, y tener el archivo
 * ~/.servicedesk360/db.properties con el usuario servicedesk_app.
 *
 * <p>Deja registrados dos tickets de prueba (pasos 15 y ejercicio 3)
 * para comprobar después que persisten al reiniciar (prueba 13).
 */
public class PruebasJdbc {

    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        ClientesJdbc clientes = new ClientesJdbc();
        TicketsJdbc tickets = new TicketsJdbc();

        // 5. ConexionBD abre y cierra conexión
        try (Connection cn = ConexionBD.abrir()) {
            verificar("5", "ConexionBD abre y cierra conexión",
                    cn.isValid(3), "isValid(3) = " + cn.isValid(3));
        }

        // 7. PreparedStatement busca por parámetro
        ClienteRegistro ana = clientes.buscarClientePorCorreo("ana.lopez@demo.local");
        verificar("7", "PreparedStatement busca por parámetro",
                ana != null && "ana.lopez@demo.local".equals(ana.getCorreo()),
                String.valueOf(ana));

        // 8. Entrada especial se trata como dato (paso 13)
        String correoPrueba = "' OR '1'='1";
        ClienteRegistro encontrado = clientes.buscarClientePorCorreo(correoPrueba);
        verificar("8", "Entrada especial se trata como dato",
                encontrado == null,
                encontrado == null
                        ? "Entrada tratada como dato: sin coincidencias"
                        : "Revisar implementación");

        // 9. Inserción devuelve clave generada (paso 12)
        String correoNuevo = "prueba." + System.currentTimeMillis() + "@demo.local";
        long idNuevo = clientes.insertarCliente("Cliente de prueba", correoNuevo);
        verificar("9", "Inserción devuelve clave generada",
                idNuevo > 0, "id_cliente generado = " + idNuevo);

        // Ejercicio 1. Restricción UNIQUE
        try {
            clientes.insertarCliente("Cliente duplicado", correoNuevo);
            verificar("E1", "UNIQUE rechaza correo repetido", false,
                    "Se insertó un duplicado");
        } catch (SQLException ex) {
            verificar("E1", "UNIQUE rechaza correo repetido",
                    ex.getErrorCode() == 1062,
                    "SQLState=" + ex.getSQLState() + " código=" + ex.getErrorCode()
                    + " | usuario ve: " + DiagnosticoBD.mensajeUsuario(ex));
        }
        clientes.eliminarCliente(idNuevo); // limpieza del cliente de prueba

        // 10. FK rechaza referencia inexistente
        long t0 = tickets.contar("tickets");
        try {
            tickets.registrarTicketConSeguimiento(999999L, null, null, 1L,
                    "Cliente inexistente", "Prueba de clave foránea.",
                    "MEDIA", "No debe registrarse.");
            verificar("10", "FK rechaza referencia inexistente", false,
                    "Se registró un ticket inválido");
        } catch (SQLException ex) {
            verificar("10", "FK rechaza referencia inexistente",
                    "23000".equals(ex.getSQLState())
                    && tickets.contar("tickets") == t0,
                    "SQLState=" + ex.getSQLState() + " código=" + ex.getErrorCode());
        }

        // 11. Transacción exitosa confirma 2 operaciones (paso 15)
        long tAntes = tickets.contar("tickets");
        long sAntes = tickets.contar("seguimientos");
        long id = tickets.registrarTicketConSeguimiento(
                1L, null, 1L, 1L,
                "Error al ingresar al portal",
                "El usuario recibe un mensaje al autenticar.",
                "MEDIA",
                "Ticket registrado desde la Guía 5");
        long tDespues = tickets.contar("tickets");
        long sDespues = tickets.contar("seguimientos");
        verificar("11", "Transacción exitosa confirma 2 operaciones",
                id > 0 && tDespues == tAntes + 1 && sDespues == sAntes + 1,
                "Ticket confirmado: " + id + " | tickets " + tAntes + "->" + tDespues
                + " | seguimientos " + sAntes + "->" + sDespues);

        // 12a. Fallo en la PRIMERA operación (categoría inexistente, paso 16)
        tAntes = tickets.contar("tickets");
        sAntes = tickets.contar("seguimientos");
        try {
            tickets.registrarTicketConSeguimiento(1L, null, null, 999999L,
                    "Categoría inexistente", "Debe revertirse completo.",
                    "ALTA", "No debe quedar.");
            verificar("12a", "Rollback con categoría inexistente", false,
                    "Se confirmó una operación inválida");
        } catch (SQLException ex) {
            verificar("12a", "Rollback con categoría inexistente",
                    tickets.contar("tickets") == tAntes
                    && tickets.contar("seguimientos") == sAntes,
                    "código=" + ex.getErrorCode() + " | tickets " + tAntes + "->"
                    + tickets.contar("tickets") + " | seguimientos " + sAntes
                    + "->" + tickets.contar("seguimientos"));
        }

        // 12b. Fallo en la SEGUNDA operación: el ticket ya se insertó dentro
        // de la transacción y el rollback debe eliminarlo.
        try {
            tickets.registrarTicketConSeguimiento(1L, null, null, 1L,
                    "Seguimiento inválido", "El seguimiento viola NOT NULL.",
                    "ALTA", null);
            verificar("12b", "Rollback cuando falla el seguimiento", false,
                    "Se confirmó una operación parcial");
        } catch (SQLException ex) {
            verificar("12b", "Rollback cuando falla el seguimiento",
                    tickets.contar("tickets") == tAntes
                    && tickets.contarTicketsSinSeguimiento() == 0,
                    "código=" + ex.getErrorCode() + " | tickets " + tAntes + "->"
                    + tickets.contar("tickets") + " | tickets sin seguimiento = "
                    + tickets.contarTicketsSinSeguimiento());
        }

        // Ejercicio 2. Búsqueda con múltiples parámetros
        List<TicketVista> filtrados = tickets.buscarPorEstadoYPrioridad("ABIERTO", "MEDIA");
        boolean todosCumplen = !filtrados.isEmpty() && filtrados.stream().allMatch(
                t -> "ABIERTO".equals(t.getEstado()) && "MEDIA".equals(t.getPrioridad()));
        verificar("E2", "Búsqueda estado + prioridad parametrizada",
                todosCumplen, filtrados.size() + " fila(s) ABIERTO/MEDIA");

        // Ejercicio 3. Transacción de tres operaciones, fallo en la tercera
        tAntes = tickets.contar("tickets");
        sAntes = tickets.contar("seguimientos");
        try {
            tickets.registrarTicketConSeguimientoYAsignacion(2L, 2L, 2L,
                    "Impresora con ruido", "Ruido fuerte al imprimir.",
                    "BAJA", "Registro inicial", 999999L);
            verificar("E3a", "3 operaciones: falla la tercera", false,
                    "Se confirmó con técnico inexistente");
        } catch (SQLException ex) {
            verificar("E3a", "3 operaciones: falla la tercera",
                    tickets.contar("tickets") == tAntes
                    && tickets.contar("seguimientos") == sAntes,
                    "código=" + ex.getErrorCode() + " | tickets " + tAntes + "->"
                    + tickets.contar("tickets") + " | seguimientos " + sAntes
                    + "->" + tickets.contar("seguimientos"));
        }
        long id3 = tickets.registrarTicketConSeguimientoYAsignacion(2L, 2L, 2L,
                "Impresora con ruido", "Ruido fuerte al imprimir.",
                "BAJA", "Registro inicial", 1L);
        verificar("E3b", "3 operaciones: caso exitoso",
                tickets.contar("tickets") == tAntes + 1
                && tickets.contar("seguimientos") == sAntes + 1,
                "Ticket " + id3 + " registrado y ASIGNADO");

        // 14. SQLException se controla (mensaje sin credenciales)
        SQLException simulada = new SQLException(
                "Access denied for user 'servicedesk_app'@'localhost'", "28000", 1045);
        String mensaje = DiagnosticoBD.mensajeUsuario(simulada);
        verificar("14", "SQLException se controla",
                !mensaje.contains("servicedesk_app") && !mensaje.contains("jdbc:"),
                "usuario ve: " + mensaje);

        // 17. Listado con JOIN
        List<TicketVista> todos = tickets.listarTickets();
        verificar("17", "Listado de tickets con JOIN",
                !todos.isEmpty(), todos.size() + " ticket(s)");
        todos.forEach(t -> System.out.println("       " + t));

        System.out.println();
        System.out.println(fallidas == 0
                ? "RESULTADO: todas las pruebas pasaron."
                : "RESULTADO: " + fallidas + " prueba(s) fallaron.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void verificar(String numero, String prueba,
            boolean ok, String detalle) {
        if (!ok) {
            fallidas++;
        }
        System.out.printf("[%s] %-4s %-45s | %s%n",
                ok ? "PASA " : "FALLA", numero, prueba, detalle);
    }
}

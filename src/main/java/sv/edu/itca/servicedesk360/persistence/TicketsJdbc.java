package sv.edu.itca.servicedesk360.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Partes D y E de la Guía 5: transacción ticket + seguimiento y
 * consultas de lectura con JOIN. Clase de laboratorio; en la Guía 6 se
 * reorganizará mediante el patrón DAO.
 */
public class TicketsJdbc {

    private static final String SQL_TICKET = "INSERT INTO tickets "
            + "(id_cliente, id_equipo, id_tecnico, id_categoria, "
            + "titulo, descripcion, prioridad) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SEGUIMIENTO
            = "INSERT INTO seguimientos(id_ticket, detalle) VALUES (?, ?)";

    private static final String SQL_ASIGNAR = "UPDATE tickets "
            + "SET id_tecnico = ?, estado = 'ASIGNADO' WHERE id_ticket = ?";

    private static final String SQL_LISTADO
            = "SELECT t.id_ticket, t.titulo, t.prioridad, t.estado, "
            + "t.fecha_creacion, c.nombre AS cliente, "
            + "cat.nombre AS categoria "
            + "FROM tickets t "
            + "JOIN clientes c ON c.id_cliente = t.id_cliente "
            + "JOIN categorias cat ON cat.id_categoria = t.id_categoria ";

    /**
     * Paso 14: registra un ticket y su seguimiento inicial como una sola
     * unidad de trabajo. Si cualquiera falla, se ejecuta rollback.
     */
    public long registrarTicketConSeguimiento(
            long idCliente, Long idEquipo, Long idTecnico,
            long idCategoria, String titulo, String descripcion,
            String prioridad, String detalleInicial) throws SQLException {

        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try {
                long idTicket = insertarTicket(cn, idCliente, idEquipo,
                        idTecnico, idCategoria, titulo, descripcion, prioridad);
                insertarSeguimiento(cn, idTicket, detalleInicial);
                cn.commit();
                return idTicket;
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    /**
     * Ejercicio 3: transacción de tres operaciones (ticket, seguimiento y
     * asignación de técnico). Si la tercera falla, las dos primeras se
     * revierten.
     */
    public long registrarTicketConSeguimientoYAsignacion(
            long idCliente, Long idEquipo, long idCategoria,
            String titulo, String descripcion, String prioridad,
            String detalleInicial, long idTecnicoAsignado)
            throws SQLException {

        try (Connection cn = ConexionBD.abrir()) {
            cn.setAutoCommit(false);
            try {
                long idTicket = insertarTicket(cn, idCliente, idEquipo,
                        null, idCategoria, titulo, descripcion, prioridad);
                insertarSeguimiento(cn, idTicket, detalleInicial);
                try (PreparedStatement ps = cn.prepareStatement(SQL_ASIGNAR)) {
                    ps.setLong(1, idTecnicoAsignado);
                    ps.setLong(2, idTicket);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo asignar el técnico.");
                    }
                }
                cn.commit();
                return idTicket;
            } catch (SQLException | RuntimeException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private long insertarTicket(Connection cn, long idCliente, Long idEquipo,
            Long idTecnico, long idCategoria, String titulo,
            String descripcion, String prioridad) throws SQLException {

        try (PreparedStatement ps = cn.prepareStatement(
                SQL_TICKET, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, idCliente);
            if (idEquipo == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, idEquipo);
            }
            if (idTecnico == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(3, idTecnico);
            }
            ps.setLong(4, idCategoria);
            ps.setString(5, titulo);
            ps.setString(6, descripcion);
            ps.setString(7, prioridad);
            ps.executeUpdate();

            try (ResultSet k = ps.getGeneratedKeys()) {
                if (!k.next()) {
                    throw new SQLException("Sin id_ticket generado");
                }
                return k.getLong(1);
            }
        }
    }

    private void insertarSeguimiento(Connection cn, long idTicket,
            String detalle) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_SEGUIMIENTO)) {
            ps.setLong(1, idTicket);
            ps.setString(2, detalle);
            ps.executeUpdate();
        }
    }

    /** Paso 17: listado de tickets con JOIN. */
    public List<TicketVista> listarTickets() throws SQLException {
        String sql = SQL_LISTADO + "ORDER BY t.fecha_creacion DESC, t.id_ticket DESC";
        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return leer(rs);
        }
    }

    /** Ejercicio 2: búsqueda con dos parámetros (estado + prioridad). */
    public List<TicketVista> buscarPorEstadoYPrioridad(
            String estado, String prioridad) throws SQLException {
        String sql = SQL_LISTADO
                + "WHERE t.estado = ? AND t.prioridad = ? "
                + "ORDER BY t.fecha_creacion DESC, t.id_ticket DESC";
        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, prioridad);
            try (ResultSet rs = ps.executeQuery()) {
                return leer(rs);
            }
        }
    }

    /** Cuenta filas de tickets o seguimientos (evidencia antes/después). */
    public long contar(String tabla) throws SQLException {
        if (!"tickets".equals(tabla) && !"seguimientos".equals(tabla)) {
            // Un nombre de tabla no puede ser parámetro; se usa lista blanca.
            throw new IllegalArgumentException("Tabla no permitida: " + tabla);
        }
        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT COUNT(*) FROM " + tabla);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Tickets que no tienen ningún seguimiento (operaciones parciales). */
    public long contarTicketsSinSeguimiento() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tickets t "
                + "LEFT JOIN seguimientos s ON s.id_ticket = t.id_ticket "
                + "WHERE s.id_seguimiento IS NULL";
        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private List<TicketVista> leer(ResultSet rs) throws SQLException {
        List<TicketVista> salida = new ArrayList<>();
        while (rs.next()) {
            salida.add(new TicketVista(
                    rs.getLong("id_ticket"),
                    rs.getString("titulo"),
                    rs.getString("prioridad"),
                    rs.getString("estado"),
                    rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                    rs.getString("cliente"),
                    rs.getString("categoria")));
        }
        return salida;
    }
}

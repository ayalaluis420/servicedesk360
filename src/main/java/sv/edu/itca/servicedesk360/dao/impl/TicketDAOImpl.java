package sv.edu.itca.servicedesk360.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.TicketDAO;
import sv.edu.itca.servicedesk360.dto.ProductividadDTO;
import sv.edu.itca.servicedesk360.dto.TicketDetalleDTO;
import sv.edu.itca.servicedesk360.model.Ticket;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/**
 * Guía 7: implementación JDBC/MySQL de {@link TicketDAO}.
 *
 * <p>Los métodos que reciben una Connection no la cierran ni confirman:
 * eso le corresponde al servicio que coordina la transacción.
 */
public class TicketDAOImpl implements TicketDAO {

    /**
     * Consulta de negocio (paso 11). Se usa LEFT JOIN para equipos y
     * técnicos porque esas columnas admiten NULL en el esquema; con
     * INNER JOIN, un ticket sin técnico desaparecería del listado.
     * CONCAT_WS omite marca o modelo NULL (CONCAT devolvería NULL).
     */
    private static final String SELECT_DETALLE
            = "SELECT t.id_ticket, c.nombre AS cliente, "
            + "CONCAT_WS(' - ', e.codigo_inventario, CONCAT_WS(' ', e.marca, e.modelo)) AS equipo, "
            + "cat.nombre AS categoria, tec.nombre AS tecnico, "
            + "t.titulo, t.descripcion, t.prioridad, t.estado, t.fecha_creacion "
            + "FROM tickets t "
            + "INNER JOIN clientes c ON c.id_cliente = t.id_cliente "
            + "INNER JOIN categorias cat ON cat.id_categoria = t.id_categoria "
            + "LEFT JOIN equipos e ON e.id_equipo = t.id_equipo "
            + "LEFT JOIN tecnicos tec ON tec.id_tecnico = t.id_tecnico ";

    private static final String FILTRO
            = "WHERE (? IS NULL OR t.estado = ?) AND (? IS NULL OR t.prioridad = ?) ";

    private DAOException error(String mensaje, SQLException ex) {
        DiagnosticoBD.registrar(mensaje, ex);
        return new DAOException(mensaje, ex);
    }

    private static void setLongONull(PreparedStatement ps, int i, Long valor)
            throws SQLException {
        if (valor == null) {
            ps.setNull(i, Types.BIGINT);
        } else {
            ps.setLong(i, valor);
        }
    }

    /** Asigna los cuatro parámetros de FILTRO a partir de la posición dada. */
    private static int asignarFiltro(PreparedStatement ps, int i,
            String estado, String prioridad) throws SQLException {
        ps.setString(i++, estado);
        ps.setString(i++, estado);
        ps.setString(i++, prioridad);
        ps.setString(i++, prioridad);
        return i;
    }

    // ---------------------------------------------------------- escritura

    @Override
    public long registrar(Connection cn, Ticket t) {
        String sql = "INSERT INTO tickets (id_cliente, id_equipo, id_tecnico, id_categoria, "
                + "titulo, descripcion, prioridad, estado, fecha_creacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ABIERTO', NOW())";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getIdCliente());
            setLongONull(ps, 2, t.getIdEquipo());
            setLongONull(ps, 3, t.getIdTecnico());
            ps.setLong(4, t.getIdCategoria());
            ps.setString(5, t.getTitulo());
            ps.setString(6, t.getDescripcion());
            ps.setString(7, t.getPrioridad());
            if (ps.executeUpdate() != 1) {
                throw new DAOException("No se insertó el ticket");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    t.setIdTicket(id);
                    t.setEstado("ABIERTO");
                    return id;
                }
            }
            throw new DAOException("MySQL no devolvió el id del ticket");
        } catch (SQLException ex) {
            throw error("Error registrando el ticket", ex);
        }
    }

    @Override
    public boolean actualizarEstado(Connection cn, long idTicket,
            String estadoEsperado, String nuevoEstado) {
        String sql = "UPDATE tickets SET estado = ? WHERE id_ticket = ? AND estado = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setLong(2, idTicket);
            ps.setString(3, estadoEsperado);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("Error actualizando el estado del ticket", ex);
        }
    }

    @Override
    public boolean actualizar(Connection cn, Ticket t) {
        String sql = "UPDATE tickets SET id_cliente = ?, id_equipo = ?, id_tecnico = ?, "
                + "id_categoria = ?, titulo = ?, descripcion = ?, prioridad = ? "
                + "WHERE id_ticket = ? AND estado <> 'CERRADO'";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, t.getIdCliente());
            setLongONull(ps, 2, t.getIdEquipo());
            setLongONull(ps, 3, t.getIdTecnico());
            ps.setLong(4, t.getIdCategoria());
            ps.setString(5, t.getTitulo());
            ps.setString(6, t.getDescripcion());
            ps.setString(7, t.getPrioridad());
            ps.setLong(8, t.getIdTicket());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("Error actualizando el ticket", ex);
        }
    }

    // ----------------------------------------------------------- lectura

    @Override
    public Optional<Ticket> buscarPorId(long idTicket) {
        String sql = "SELECT id_ticket, id_cliente, id_equipo, id_tecnico, id_categoria, "
                + "titulo, descripcion, prioridad, estado, fecha_creacion "
                + "FROM tickets WHERE id_ticket = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idTicket);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                long eq = rs.getLong("id_equipo");
                Long idEquipo = rs.wasNull() ? null : eq;
                long tec = rs.getLong("id_tecnico");
                Long idTecnico = rs.wasNull() ? null : tec;
                return Optional.of(new Ticket(rs.getLong("id_ticket"), rs.getLong("id_cliente"),
                        idEquipo, idTecnico, rs.getLong("id_categoria"), rs.getString("titulo"),
                        rs.getString("descripcion"), rs.getString("prioridad"), rs.getString("estado"),
                        rs.getTimestamp("fecha_creacion").toLocalDateTime()));
            }
        } catch (SQLException ex) {
            throw error("No fue posible consultar el ticket", ex);
        }
    }

    @Override
    public List<TicketDetalleDTO> listarDetalle(String estado, String prioridad,
            int limite, int offset) {
        String sql = SELECT_DETALLE + FILTRO
                + "ORDER BY t.fecha_creacion DESC, t.id_ticket DESC LIMIT ? OFFSET ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = asignarFiltro(ps, 1, estado, prioridad);
            ps.setInt(i++, limite);
            ps.setInt(i, offset);
            List<TicketDetalleDTO> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw error("No fue posible listar los tickets", ex);
        }
    }

    @Override
    public int contar(String estado, String prioridad) {
        String sql = "SELECT COUNT(*) FROM tickets t " + FILTRO;
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarFiltro(ps, 1, estado, prioridad);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw error("No fue posible contar los tickets", ex);
        }
    }

    @Override
    public Optional<TicketDetalleDTO> buscarDetalle(long idTicket) {
        String sql = SELECT_DETALLE + "WHERE t.id_ticket = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idTicket);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("No fue posible consultar el ticket", ex);
        }
    }

    @Override
    public List<ProductividadDTO> productividadPorTecnico() {
        String sql = "SELECT tec.nombre, t.estado, COUNT(*) AS total "
                + "FROM tickets t "
                + "INNER JOIN tecnicos tec ON tec.id_tecnico = t.id_tecnico "
                + "GROUP BY tec.nombre, t.estado "
                + "ORDER BY total DESC, tec.nombre, t.estado";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<ProductividadDTO> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new ProductividadDTO(
                        rs.getString("nombre"), rs.getString("estado"), rs.getInt("total")));
            }
            return lista;
        } catch (SQLException ex) {
            throw error("No fue posible calcular la productividad", ex);
        }
    }

    /** Paso 12: fila del JOIN -> DTO listo para la vista. */
    private TicketDetalleDTO mapear(ResultSet rs) throws SQLException {
        TicketDetalleDTO d = new TicketDetalleDTO();
        d.setIdTicket(rs.getLong("id_ticket"));
        d.setCliente(rs.getString("cliente"));
        d.setEquipo(rs.getString("equipo"));
        d.setCategoria(rs.getString("categoria"));
        d.setTecnico(rs.getString("tecnico"));
        d.setTitulo(rs.getString("titulo"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setPrioridad(rs.getString("prioridad"));
        d.setEstado(rs.getString("estado"));
        d.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        return d;
    }
}

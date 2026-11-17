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
import sv.edu.itca.servicedesk360.dao.EquipoDAO;
import sv.edu.itca.servicedesk360.dto.EquipoDetalleDTO;
import sv.edu.itca.servicedesk360.model.Equipo;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/** Guía 7: implementación JDBC/MySQL de {@link EquipoDAO}. */
public class EquipoDAOImpl implements EquipoDAO {

    private static final String COLUMNAS = "SELECT id_equipo, id_cliente, codigo_inventario, "
            + "tipo, marca, modelo, activo FROM equipos ";

    private DAOException error(String mensaje, SQLException ex) {
        DiagnosticoBD.registrar(mensaje, ex);
        return new DAOException(mensaje, ex);
    }

    private Equipo mapear(ResultSet rs) throws SQLException {
        return new Equipo(rs.getLong("id_equipo"), rs.getLong("id_cliente"),
                rs.getString("codigo_inventario"), rs.getString("tipo"),
                rs.getString("marca"), rs.getString("modelo"), rs.getBoolean("activo"));
    }

    private static void setTextoONull(PreparedStatement ps, int i, String v) throws SQLException {
        if (v == null || v.isBlank()) {
            ps.setNull(i, Types.VARCHAR);
        } else {
            ps.setString(i, v);
        }
    }

    // --------------------------------------------- proceso de tickets

    @Override
    public List<Equipo> listarPorCliente(long idCliente) {
        String sql = COLUMNAS + "WHERE id_cliente = ? AND activo = TRUE ORDER BY codigo_inventario";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            List<Equipo> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException ex) {
            throw error("No fue posible listar los equipos del cliente", ex);
        }
    }

    @Override
    public boolean perteneceACliente(long idEquipo, long idCliente) {
        String sql = "SELECT COUNT(*) FROM equipos "
                + "WHERE id_equipo = ? AND id_cliente = ? AND activo = TRUE";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            ps.setLong(2, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException ex) {
            throw error("No fue posible validar el equipo", ex);
        }
    }

    // ------------------------------------------------- CRUD de equipos

    @Override
    public List<EquipoDetalleDTO> listarDetalle() {
        String sql = "SELECT e.id_equipo, e.codigo_inventario, e.tipo, e.marca, e.modelo, "
                + "e.activo, c.nombre AS cliente, "
                + "(SELECT COUNT(*) FROM tickets t WHERE t.id_equipo = e.id_equipo) AS tickets "
                + "FROM equipos e INNER JOIN clientes c ON c.id_cliente = e.id_cliente "
                + "ORDER BY e.codigo_inventario";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<EquipoDetalleDTO> lista = new ArrayList<>();
            while (rs.next()) {
                EquipoDetalleDTO d = new EquipoDetalleDTO();
                d.setIdEquipo(rs.getLong("id_equipo"));
                d.setCodigoInventario(rs.getString("codigo_inventario"));
                d.setTipo(rs.getString("tipo"));
                d.setMarca(rs.getString("marca"));
                d.setModelo(rs.getString("modelo"));
                d.setActivo(rs.getBoolean("activo"));
                d.setCliente(rs.getString("cliente"));
                d.setTickets(rs.getInt("tickets"));
                lista.add(d);
            }
            return lista;
        } catch (SQLException ex) {
            throw error("No fue posible listar los equipos", ex);
        }
    }

    @Override
    public Optional<Equipo> buscarPorId(long id) {
        String sql = COLUMNAS + "WHERE id_equipo = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("No fue posible consultar el equipo", ex);
        }
    }

    @Override
    public boolean existeCodigo(String codigo, Long excluirId) {
        String sql = "SELECT COUNT(*) FROM equipos WHERE UPPER(codigo_inventario) = UPPER(?) "
                + "AND (? IS NULL OR id_equipo <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            if (excluirId == null) {
                ps.setNull(2, Types.BIGINT);
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setLong(2, excluirId);
                ps.setLong(3, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw error("No fue posible validar el código de inventario", ex);
        }
    }

    @Override
    public long insertar(Equipo e) {
        String sql = "INSERT INTO equipos (id_cliente, codigo_inventario, tipo, marca, modelo, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, e.getIdCliente());
            ps.setString(2, e.getCodigoInventario());
            ps.setString(3, e.getTipo());
            setTextoONull(ps, 4, e.getMarca());
            setTextoONull(ps, 5, e.getModelo());
            ps.setBoolean(6, e.isActivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    e.setIdEquipo(id);
                    return id;
                }
            }
            throw new DAOException("MySQL no devolvió la clave generada");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                throw error("El código de inventario ya está registrado", ex);
            }
            throw error("No fue posible registrar el equipo", ex);
        }
    }

    @Override
    public boolean actualizar(Equipo e) {
        String sql = "UPDATE equipos SET id_cliente = ?, codigo_inventario = ?, tipo = ?, "
                + "marca = ?, modelo = ? WHERE id_equipo = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, e.getIdCliente());
            ps.setString(2, e.getCodigoInventario());
            ps.setString(3, e.getTipo());
            setTextoONull(ps, 4, e.getMarca());
            setTextoONull(ps, 5, e.getModelo());
            ps.setLong(6, e.getIdEquipo());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                throw error("El código de inventario ya está registrado", ex);
            }
            throw error("No fue posible actualizar el equipo", ex);
        }
    }

    @Override
    public int contarTickets(long id) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE id_equipo = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw error("No fue posible verificar los tickets del equipo", ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM equipos WHERE id_equipo = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible eliminar. El equipo puede tener tickets relacionados", ex);
        }
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        String sql = "UPDATE equipos SET activo = ? WHERE id_equipo = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible cambiar el estado del equipo", ex);
        }
    }
}

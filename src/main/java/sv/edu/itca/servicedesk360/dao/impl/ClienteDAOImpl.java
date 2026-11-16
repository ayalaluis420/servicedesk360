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
import sv.edu.itca.servicedesk360.dao.ClienteDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/**
 * Implementación JDBC/MySQL de {@link ClienteDAO}.
 *
 * <p>Es la única clase que conoce la tabla {@code clientes} y sus
 * columnas. Cada método abre su propia conexión y cierra Connection,
 * PreparedStatement y ResultSet con try-with-resources. Toda
 * {@link SQLException} se registra en el log del servidor y se convierte
 * en {@link DAOException} con un mensaje funcional.
 */
public class ClienteDAOImpl implements ClienteDAO {

    private static final String COLUMNAS
            = "SELECT id_cliente, nombre, correo, activo FROM clientes ";

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getLong("id_cliente"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getBoolean("activo"));
    }

    /** Registra el detalle técnico y crea la excepción para el controlador. */
    private DAOException error(String mensaje, SQLException ex) {
        DiagnosticoBD.registrar(mensaje, ex);
        return new DAOException(mensaje, ex);
    }

    @Override
    public List<Cliente> listar() {
        String sql = COLUMNAS + "ORDER BY id_cliente DESC";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Cliente> clientes = new ArrayList<>();
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
            return clientes;
        } catch (SQLException ex) {
            throw error("No fue posible listar clientes", ex);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(long id) {
        String sql = COLUMNAS + "WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("No fue posible consultar el cliente", ex);
        }
    }

    @Override
    public boolean existeCorreo(String correo, Long excluirId) {
        String sql = "SELECT COUNT(*) FROM clientes "
                + "WHERE LOWER(correo) = LOWER(?) "
                + "AND (? IS NULL OR id_cliente <> ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo);
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
            throw error("No fue posible validar el correo", ex);
        }
    }

    @Override
    public long insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes(nombre, correo, activo) VALUES (?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getCorreo());
            ps.setBoolean(3, cliente.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    cliente.setIdCliente(id);
                    return id;
                }
            }
            throw new DAOException("MySQL no devolvió la clave generada");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                // Defensa adicional: UNIQUE de MySQL (otra petición pudo
                // registrar el mismo correo después de la validación).
                throw error("El correo ya está registrado", ex);
            }
            throw error("No fue posible registrar el cliente", ex);
        }
    }

    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, correo = ?, activo = ? "
                + "WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getCorreo());
            ps.setBoolean(3, cliente.isActivo());
            ps.setLong(4, cliente.getIdCliente());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                throw error("El correo ya está registrado", ex);
            }
            throw error("No fue posible actualizar el cliente", ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible eliminar. "
                    + "El cliente puede tener registros relacionados.", ex);
        }
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        String sql = "UPDATE clientes SET activo = ? WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible cambiar el estado", ex);
        }
    }

    // ----- Ejercicios complementarios -----

    @Override
    public List<Cliente> buscar(String criterio) {
        String sql = COLUMNAS + "WHERE nombre LIKE ? OR correo LIKE ? "
                + "ORDER BY id_cliente DESC";
        // % y _ son comodines de LIKE: se escapan para buscarlos como texto.
        String patron = "%" + (criterio == null ? "" : criterio.trim())
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            return leerLista(ps);
        } catch (SQLException ex) {
            throw error("No fue posible buscar clientes", ex);
        }
    }

    @Override
    public List<Cliente> listarActivos() {
        String sql = COLUMNAS + "WHERE activo = TRUE ORDER BY id_cliente DESC";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            return leerLista(ps);
        } catch (SQLException ex) {
            throw error("No fue posible listar clientes activos", ex);
        }
    }

    @Override
    public int contarRelaciones(long id) {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM equipos WHERE id_cliente = ?) + "
                + "(SELECT COUNT(*) FROM tickets WHERE id_cliente = ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw error("No fue posible verificar las relaciones del cliente", ex);
        }
    }

    @Override
    public List<Cliente> listar(int limite, int offset) {
        if (limite < 1 || offset < 0) {
            throw new IllegalArgumentException("Límite u offset inválido");
        }
        String sql = COLUMNAS + "ORDER BY id_cliente DESC LIMIT ? OFFSET ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limite);
            ps.setInt(2, offset);
            return leerLista(ps);
        } catch (SQLException ex) {
            throw error("No fue posible listar clientes", ex);
        }
    }

    private List<Cliente> leerLista(PreparedStatement ps) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        }
        return clientes;
    }
}

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
import sv.edu.itca.servicedesk360.dao.PersonalTecnicoDAO;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/**
 * Implementación JDBC/MySQL de {@link PersonalTecnicoDAO} (tabla
 * {@code tecnicos}). Sigue el mismo patrón que ClienteDAOImpl.
 */
public class PersonalTecnicoDAOImpl implements PersonalTecnicoDAO {

    private static final String COLUMNAS = "SELECT id_tecnico, nombre, correo, "
            + "especialidad, nivel_soporte, activo FROM tecnicos ";

    private PersonalTecnico mapear(ResultSet rs) throws SQLException {
        return new PersonalTecnico(
                rs.getLong("id_tecnico"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("especialidad"),
                rs.getInt("nivel_soporte"),
                rs.getBoolean("activo"));
    }

    private DAOException error(String mensaje, SQLException ex) {
        DiagnosticoBD.registrar(mensaje, ex);
        return new DAOException(mensaje, ex);
    }

    private void asignarParametros(PreparedStatement ps, PersonalTecnico t)
            throws SQLException {
        ps.setString(1, t.getNombre());
        ps.setString(2, t.getCorreo());
        if (t.getEspecialidad() == null || t.getEspecialidad().isBlank()) {
            ps.setNull(3, Types.VARCHAR);
        } else {
            ps.setString(3, t.getEspecialidad());
        }
        ps.setInt(4, t.getNivelSoporte());
        ps.setBoolean(5, t.isActivo());
    }

    @Override
    public List<PersonalTecnico> listar() {
        String sql = COLUMNAS + "ORDER BY id_tecnico DESC";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<PersonalTecnico> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw error("No fue posible listar técnicos", ex);
        }
    }

    @Override
    public Optional<PersonalTecnico> buscarPorId(long id) {
        String sql = COLUMNAS + "WHERE id_tecnico = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw error("No fue posible consultar el técnico", ex);
        }
    }

    @Override
    public boolean existeCorreo(String correo, Long excluirId) {
        String sql = "SELECT COUNT(*) FROM tecnicos WHERE LOWER(correo) = LOWER(?) "
                + "AND (? IS NULL OR id_tecnico <> ?)";
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
    public long insertar(PersonalTecnico tecnico) {
        String sql = "INSERT INTO tecnicos(nombre, correo, especialidad, "
                + "nivel_soporte, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, tecnico);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    tecnico.setIdTecnico(id);
                    return id;
                }
            }
            throw new DAOException("MySQL no devolvió la clave generada");
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                throw error("El correo ya está registrado", ex);
            }
            throw error("No fue posible registrar el técnico", ex);
        }
    }

    @Override
    public boolean actualizar(PersonalTecnico tecnico) {
        String sql = "UPDATE tecnicos SET nombre = ?, correo = ?, especialidad = ?, "
                + "nivel_soporte = ?, activo = ? WHERE id_tecnico = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            asignarParametros(ps, tecnico);
            ps.setLong(6, tecnico.getIdTecnico());
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                throw error("El correo ya está registrado", ex);
            }
            throw error("No fue posible actualizar el técnico", ex);
        }
    }

    @Override
    public boolean eliminar(long id) {
        String sql = "DELETE FROM tecnicos WHERE id_tecnico = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible eliminar. "
                    + "El técnico puede tener tickets asignados.", ex);
        }
    }

    @Override
    public boolean cambiarEstado(long id, boolean activo) {
        String sql = "UPDATE tecnicos SET activo = ? WHERE id_tecnico = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw error("No fue posible cambiar el estado", ex);
        }
    }

    @Override
    public int contarTicketsAsignados(long id) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE id_tecnico = ?";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw error("No fue posible verificar los tickets del técnico", ex);
        }
    }
}

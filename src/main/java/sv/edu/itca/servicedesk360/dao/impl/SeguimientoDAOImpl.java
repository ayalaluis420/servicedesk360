package sv.edu.itca.servicedesk360.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.dao.SeguimientoDAO;
import sv.edu.itca.servicedesk360.model.Seguimiento;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/** Guía 7: implementación JDBC/MySQL de {@link SeguimientoDAO}. */
public class SeguimientoDAOImpl implements SeguimientoDAO {

    @Override
    public int registrar(Connection cn, Seguimiento s) {
        String sql = "INSERT INTO seguimientos (id_ticket, detalle, fecha_registro) "
                + "VALUES (?, ?, NOW())";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, s.getIdTicket());
            ps.setString(2, s.getDetalle());
            return ps.executeUpdate();
        } catch (SQLException ex) {
            DiagnosticoBD.registrar("Error registrando el seguimiento", ex);
            throw new DAOException("Error registrando el seguimiento", ex);
        }
    }

    @Override
    public List<Seguimiento> listarPorTicket(long idTicket) {
        String sql = "SELECT id_seguimiento, id_ticket, detalle, fecha_registro "
                + "FROM seguimientos WHERE id_ticket = ? "
                + "ORDER BY fecha_registro, id_seguimiento";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idTicket);
            List<Seguimiento> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Seguimiento(
                            rs.getLong("id_seguimiento"),
                            rs.getLong("id_ticket"),
                            rs.getString("detalle"),
                            rs.getTimestamp("fecha_registro").toLocalDateTime()));
                }
            }
            return lista;
        } catch (SQLException ex) {
            DiagnosticoBD.registrar("No fue posible listar los seguimientos", ex);
            throw new DAOException("No fue posible listar los seguimientos", ex);
        }
    }
}

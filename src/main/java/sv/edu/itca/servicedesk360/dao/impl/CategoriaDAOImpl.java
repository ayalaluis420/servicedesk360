package sv.edu.itca.servicedesk360.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sv.edu.itca.servicedesk360.dao.CategoriaDAO;
import sv.edu.itca.servicedesk360.dao.DAOException;
import sv.edu.itca.servicedesk360.model.Categoria;
import sv.edu.itca.servicedesk360.persistence.ConexionBD;
import sv.edu.itca.servicedesk360.persistence.DiagnosticoBD;

/**
 * Guía 7: implementación JDBC/MySQL de {@link CategoriaDAO}.
 * Requiere la columna categorias.activo (sql/05_ajustes_guia07.sql).
 */
public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public List<Categoria> listarActivas() {
        String sql = "SELECT id_categoria, nombre, activo FROM categorias "
                + "WHERE activo = TRUE ORDER BY nombre";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Categoria> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Categoria(rs.getLong("id_categoria"),
                        rs.getString("nombre"), rs.getBoolean("activo")));
            }
            return lista;
        } catch (SQLException ex) {
            DiagnosticoBD.registrar("No fue posible listar las categorías", ex);
            throw new DAOException("No fue posible listar las categorías", ex);
        }
    }

    @Override
    public boolean estaActiva(long idCategoria) {
        String sql = "SELECT COUNT(*) FROM categorias WHERE id_categoria = ? AND activo = TRUE";
        try (Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException ex) {
            DiagnosticoBD.registrar("No fue posible validar la categoría", ex);
            throw new DAOException("No fue posible validar la categoría", ex);
        }
    }
}

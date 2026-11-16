package sv.edu.itca.servicedesk360.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Parte C de la Guía 5: consultas parametrizadas sobre clientes.
 * Clase de laboratorio; en la Guía 6 se encapsulará en un DAO.
 */
public class ClientesJdbc {

    /** Paso 11: búsqueda por correo con PreparedStatement. */
    public ClienteRegistro buscarClientePorCorreo(String correo)
            throws SQLException {

        String sql = "SELECT id_cliente, nombre, correo, activo "
                + "FROM clientes WHERE correo = ?";

        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ClienteRegistro(
                        rs.getLong("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getBoolean("activo"));
            }
        }
    }

    /** Paso 12: inserción que recupera la clave AUTO_INCREMENT. */
    public long insertarCliente(String nombre, String correo)
            throws SQLException {

        String sql = "INSERT INTO clientes(nombre, correo) VALUES (?, ?)";

        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("MySQL no devolvió la clave generada.");
                }
                return keys.getLong(1);
            }
        }
    }

    /** Elimina un cliente de prueba sin relaciones (limpieza). */
    public int eliminarCliente(long idCliente) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.abrir();
                PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idCliente);
            return ps.executeUpdate();
        }
    }
}

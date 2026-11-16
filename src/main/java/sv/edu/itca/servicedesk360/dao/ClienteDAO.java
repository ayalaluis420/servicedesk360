package sv.edu.itca.servicedesk360.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.model.Cliente;

/**
 * Contrato de persistencia de Clientes (Guía 6).
 *
 * <p>Declara capacidades sin revelar cómo se ejecutan: no aparecen JDBC,
 * SQL, nombres de tablas ni objetos HTTP. Todas las operaciones lanzan
 * {@link DAOException} si la persistencia falla.
 */
public interface ClienteDAO {

    /** CU-CLI-01: todos los clientes, del más reciente al más antiguo. */
    List<Cliente> listar();

    /** CU-CLI-03: cliente por identificador. */
    Optional<Cliente> buscarPorId(long id);

    /**
     * CU-CLI-07: indica si el correo ya pertenece a otro cliente.
     *
     * @param excluirId id del cliente que se está editando, o null al crear
     */
    boolean existeCorreo(String correo, Long excluirId);

    /** CU-CLI-02: inserta y devuelve el id generado por la base. */
    long insertar(Cliente cliente);

    /** CU-CLI-04: actualiza nombre, correo y estado. */
    boolean actualizar(Cliente cliente);

    /** CU-CLI-05: eliminación física (solo clientes sin relaciones). */
    boolean eliminar(long id);

    /** CU-CLI-06: desactivación o reactivación lógica. */
    boolean cambiarEstado(long id, boolean activo);

    // ----- Ejercicios complementarios -----

    /** Ejercicio 1: búsqueda por nombre o correo (LIKE parametrizado). */
    List<Cliente> buscar(String criterio);

    /** Ejercicio 2: solo clientes activos (catálogo operativo). */
    List<Cliente> listarActivos();

    /**
     * Ejercicio 3: cantidad de equipos y tickets que referencian al
     * cliente. Si es mayor que cero, no se permite el DELETE físico.
     */
    int contarRelaciones(long id);

    /** Ejercicio 4: paginación lógica (LIMIT/OFFSET). */
    List<Cliente> listar(int limite, int offset);
}

package sv.edu.itca.servicedesk360.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;

/**
 * Contrato de persistencia del personal técnico. Es la adaptación del
 * patrón DAO a una segunda entidad del proyecto (sección XIII de la
 * Guía 6).
 */
public interface PersonalTecnicoDAO {

    List<PersonalTecnico> listar();

    Optional<PersonalTecnico> buscarPorId(long id);

    boolean existeCorreo(String correo, Long excluirId);

    long insertar(PersonalTecnico tecnico);

    boolean actualizar(PersonalTecnico tecnico);

    boolean eliminar(long id);

    boolean cambiarEstado(long id, boolean activo);

    /** Tickets asignados al técnico; si es mayor que cero, no se elimina. */
    int contarTicketsAsignados(long id);
}

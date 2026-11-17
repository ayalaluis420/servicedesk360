package sv.edu.itca.servicedesk360.dao;

import java.util.List;
import sv.edu.itca.servicedesk360.model.Categoria;

/** Guía 7: catálogo de categorías de ticket. */
public interface CategoriaDAO {

    /** Categorías habilitadas, ordenadas por nombre (paso 17). */
    List<Categoria> listarActivas();

    /** La categoría existe y está habilitada (regla X.1). */
    boolean estaActiva(long idCategoria);
}

package sv.edu.itca.servicedesk360.dao;

import java.util.List;
import java.util.Optional;
import sv.edu.itca.servicedesk360.dao.impl.ClienteDAOImpl;
import sv.edu.itca.servicedesk360.dao.impl.PersonalTecnicoDAOImpl;
import sv.edu.itca.servicedesk360.model.Cliente;
import sv.edu.itca.servicedesk360.model.PersonalTecnico;

/**
 * Guía 6: pruebas de la capa DAO sin pasar por HTTP. Usa solo los
 * contratos {@link ClienteDAO} y {@link PersonalTecnicoDAO}.
 *
 * <p>Requisitos: sql/01 y sql/02 ejecutados y ~/.servicedesk360/db.properties.
 * Los registros que crea se eliminan al final, por lo que puede
 * ejecutarse varias veces.
 */
public class PruebasDAO {

    private static int fallidas = 0;

    public static void main(String[] args) {
        ClienteDAO dao = new ClienteDAOImpl();
        String sufijo = String.valueOf(System.currentTimeMillis());

        // V02 Listado inicial
        List<Cliente> inicial = dao.listar();
        verificar("V02", "Listado desde MySQL", !inicial.isEmpty(),
                inicial.size() + " cliente(s)");

        // V03 Alta válida
        Cliente nuevo = new Cliente(null, "Cliente DAO", "dao." + sufijo + "@demo.local", true);
        long id = dao.insertar(nuevo);
        Optional<Cliente> leido = dao.buscarPorId(id);
        verificar("V03", "Alta válida genera id", id > 0 && leido.isPresent(),
                "id generado = " + id + " | " + leido.orElse(null));

        // V05 Correo duplicado (validación de la aplicación y UNIQUE de MySQL)
        boolean existe = dao.existeCorreo(nuevo.getCorreo().toUpperCase(), null);
        boolean noSeAutoExcluye = !dao.existeCorreo(nuevo.getCorreo(), id);
        String mensajeUnique;
        try {
            dao.insertar(new Cliente(null, "Duplicado", nuevo.getCorreo(), true));
            mensajeUnique = null;
        } catch (DAOException ex) {
            mensajeUnique = ex.getMessage();
        }
        verificar("V05", "Correo duplicado bloqueado",
                existe && noSeAutoExcluye && mensajeUnique != null,
                "existeCorreo (mayúsculas) = " + existe + " | al editar se excluye a sí mismo = "
                + noSeAutoExcluye + " | UNIQUE: " + mensajeUnique);

        // V06 Edición existente
        int antes = dao.listar().size();
        Cliente editar = leido.get();
        editar.setNombre("Cliente DAO editado");
        editar.setCorreo("dao.editado." + sufijo + "@demo.local");
        boolean actualizado = dao.actualizar(editar);
        Cliente despues = dao.buscarPorId(id).get();
        verificar("V06", "Edición actualiza exactamente un registro",
                actualizado && dao.listar().size() == antes
                && "Cliente DAO editado".equals(despues.getNombre()) && despues.getIdCliente() == id,
                despues + " | total " + antes + " -> " + dao.listar().size());

        // V07 Id inexistente
        verificar("V07", "Búsqueda de id inexistente", dao.buscarPorId(999999L).isEmpty(),
                "Optional.empty()");

        // V10 / V11 Desactivación y reactivación lógica
        dao.cambiarEstado(id, false);
        boolean inactivo = !dao.buscarPorId(id).get().isActivo();
        verificar("V10", "Desactivación lógica", inactivo, "activo = 0, el registro permanece");
        dao.cambiarEstado(id, true);
        verificar("V11", "Reactivación", dao.buscarPorId(id).get().isActivo(), "activo = 1");

        // V12 Entrada con apóstrofe
        String nombreApostrofe = "Patrick O'Connor";
        long idApostrofe = dao.insertar(new Cliente(null, nombreApostrofe,
                "o'connor." + sufijo + "@demo.local", true));
        Cliente oconnor = dao.buscarPorId(idApostrofe).get();
        verificar("V12", "Apóstrofe se guarda como valor",
                nombreApostrofe.equals(oconnor.getNombre()), oconnor.toString());

        // Ejercicio 1: búsqueda por texto
        List<Cliente> porTexto = dao.buscar("connor");
        List<Cliente> comodin = dao.buscar("%");
        verificar("E1", "Búsqueda LIKE parametrizada",
                porTexto.stream().anyMatch(c -> c.getIdCliente() == idApostrofe) && comodin.isEmpty(),
                "'connor' -> " + porTexto.size() + " | '%' (escapado) -> " + comodin.size());

        // Ejercicio 2: solo activos
        dao.cambiarEstado(idApostrofe, false);
        boolean excluye = dao.listarActivos().stream().noneMatch(c -> c.getIdCliente() == idApostrofe);
        verificar("E2", "listarActivos excluye inactivos", excluye,
                dao.listarActivos().size() + " activo(s) de " + dao.listar().size());

        // Ejercicio 4: paginación
        List<Cliente> pagina = dao.listar(2, 0);
        verificar("E4", "Paginación LIMIT/OFFSET", pagina.size() == 2,
                "listar(2, 0) -> " + pagina.size() + " fila(s)");

        // V09 + Ejercicio 3: DELETE con referencias
        int relaciones = dao.contarRelaciones(1L);
        String mensajeFk;
        try {
            dao.eliminar(1L);
            mensajeFk = null;
        } catch (DAOException ex) {
            mensajeFk = ex.getMessage();
        }
        verificar("V09", "DELETE con referencias se rechaza",
                relaciones > 0 && mensajeFk != null && dao.buscarPorId(1L).isPresent(),
                "relaciones del cliente 1 = " + relaciones + " | " + mensajeFk);

        // V08 DELETE sin referencias
        int relNuevo = dao.contarRelaciones(id);
        boolean eliminado = dao.eliminar(id);
        verificar("V08", "DELETE físico sin referencias",
                relNuevo == 0 && eliminado && dao.buscarPorId(id).isEmpty(),
                "relaciones = " + relNuevo + " | eliminado = " + eliminado);
        dao.eliminar(idApostrofe); // limpieza

        // ---------------- Entidad propia: personal técnico ----------------
        PersonalTecnicoDAO tdao = new PersonalTecnicoDAOImpl();
        PersonalTecnico t = new PersonalTecnico(null, "Técnico DAO",
                "tec." + sufijo + "@demo.local", "Redes", 2, true);
        long idT = tdao.insertar(t);
        verificar("T1", "Técnico: alta con id generado", idT > 0 && tdao.buscarPorId(idT).isPresent(),
                "id = " + idT);

        PersonalTecnico te = tdao.buscarPorId(idT).get();
        te.setNivelSoporte(3);
        tdao.actualizar(te);
        verificar("T2", "Técnico: edición", tdao.buscarPorId(idT).get().getNivelSoporte() == 3,
                "nivel 2 -> 3");

        verificar("T3", "Técnico: correo duplicado", tdao.existeCorreo(t.getCorreo(), null),
                "existeCorreo = true");

        String mensajeCheck;
        try {
            te.setNivelSoporte(5);
            tdao.actualizar(te);
            mensajeCheck = null;
        } catch (DAOException ex) {
            mensajeCheck = ex.getMessage();
        }
        verificar("T4", "Técnico: CHECK nivel 1..3", mensajeCheck != null,
                "nivel 5 rechazado por MySQL: " + mensajeCheck);

        tdao.cambiarEstado(idT, false);
        verificar("T5", "Técnico: desactivación lógica", !tdao.buscarPorId(idT).get().isActivo(),
                "activo = 0");

        int ticketsT1 = tdao.contarTicketsAsignados(1L);
        String mensajeFkT;
        try {
            tdao.eliminar(1L);
            mensajeFkT = null;
        } catch (DAOException ex) {
            mensajeFkT = ex.getMessage();
        }
        verificar("T6", "Técnico con tickets no se elimina", ticketsT1 > 0 && mensajeFkT != null,
                "tickets del técnico 1 = " + ticketsT1 + " | " + mensajeFkT);

        verificar("T7", "Técnico: DELETE sin tickets",
                tdao.eliminar(idT) && tdao.buscarPorId(idT).isEmpty(), "eliminado");

        System.out.println();
        System.out.println(fallidas == 0
                ? "RESULTADO: todas las pruebas DAO pasaron."
                : "RESULTADO: " + fallidas + " prueba(s) fallaron.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    private static void verificar(String id, String prueba, boolean ok, String detalle) {
        if (!ok) {
            fallidas++;
        }
        System.out.printf("[%s] %-4s %-42s | %s%n", ok ? "PASA " : "FALLA", id, prueba, detalle);
    }
}

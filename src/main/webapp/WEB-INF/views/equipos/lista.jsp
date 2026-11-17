<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 7: CRUD de equipos relacionado con clientes. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Equipos | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud">
            <p><a href="${pageContext.request.contextPath}/panel">&larr; Volver al panel</a></p>
            <h1>Equipos</h1>

            <c:if test="${not empty sessionScope.flash}">
                <p class="alerta exito"><c:out value="${sessionScope.flash}"/></p>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <p class="alerta error"><c:out value="${sessionScope.flashError}"/></p>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <div class="barra-crud">
                <a class="boton" href="${pageContext.request.contextPath}/equipos?accion=nuevo">Nuevo equipo</a>
            </div>

            <table class="tabla-crud">
                <thead>
                    <tr><th>Código</th><th>Tipo</th><th>Marca / modelo</th><th>Cliente</th>
                        <th>Tickets</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="e" items="${equipos}">
                        <tr class="${e.activo ? '' : 'inactivo'}">
                            <td><c:out value="${e.codigoInventario}"/></td>
                            <td><c:out value="${e.tipo}"/></td>
                            <td><c:out value="${e.marca} ${e.modelo}"/></td>
                            <td><c:out value="${e.cliente}"/></td>
                            <td>${e.tickets}</td>
                            <td><span class="estado ${e.activo ? 'activo' : 'inactivo'}">${e.activo ? 'Activo' : 'Inactivo'}</span></td>
                            <td class="acciones">
                                <a href="${pageContext.request.contextPath}/equipos?accion=editar&id=${e.idEquipo}">Editar</a>
                                <form method="post" action="${pageContext.request.contextPath}/equipos">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${e.idEquipo}">
                                    <input type="hidden" name="activo" value="${!e.activo}">
                                    <button type="submit">${e.activo ? 'Desactivar' : 'Activar'}</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/equipos"
                                      onsubmit="return confirm('¿Eliminar definitivamente este equipo?');">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${e.idEquipo}">
                                    <button type="submit" class="peligro">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty equipos}"><tr><td colspan="7">No hay equipos registrados.</td></tr></c:if>
                </tbody>
            </table>
            <p class="nota">Solo los equipos activos aparecen en el formulario de tickets. Un equipo con tickets no se elimina: se desactiva.</p>
        </main>
    </body>
</html>

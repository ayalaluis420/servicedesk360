<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 6: listado del personal técnico (entidad propia). --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Técnicos | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud">
            <p><a href="${pageContext.request.contextPath}/panel">&larr; Volver al panel</a></p>
            <h1>Personal técnico</h1>

            <c:if test="${not empty sessionScope.flash}">
                <p class="alerta exito"><c:out value="${sessionScope.flash}"/></p>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <p class="alerta error"><c:out value="${sessionScope.flashError}"/></p>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <div class="barra-crud">
                <a class="boton" href="${pageContext.request.contextPath}/tecnicos?accion=nuevo">Nuevo técnico</a>
            </div>

            <table class="tabla-crud">
                <thead>
                    <tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Especialidad</th>
                        <th>Nivel</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="t" items="${tecnicos}">
                        <tr class="${t.activo ? '' : 'inactivo'}">
                            <td><c:out value="${t.idTecnico}"/></td>
                            <td><c:out value="${t.nombre}"/></td>
                            <td><c:out value="${t.correo}"/></td>
                            <td><c:out value="${empty t.especialidad ? '—' : t.especialidad}"/></td>
                            <td><c:out value="${t.nivelSoporte}"/></td>
                            <td><span class="estado ${t.activo ? 'activo' : 'inactivo'}">
                                    <c:out value="${t.activo ? 'Activo' : 'Inactivo'}"/></span></td>
                            <td class="acciones">
                                <a href="${pageContext.request.contextPath}/tecnicos?accion=editar&id=${t.idTecnico}">Editar</a>
                                <form method="post" action="${pageContext.request.contextPath}/tecnicos">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${t.idTecnico}">
                                    <input type="hidden" name="activo" value="${!t.activo}">
                                    <button type="submit">${t.activo ? 'Desactivar' : 'Activar'}</button>
                                </form>
                                <form method="post" action="${pageContext.request.contextPath}/tecnicos"
                                      onsubmit="return confirm('¿Eliminar definitivamente este técnico?');">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${t.idTecnico}">
                                    <button type="submit" class="peligro">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty tecnicos}">
                        <tr><td colspan="7">No hay técnicos registrados.</td></tr>
                    </c:if>
                </tbody>
            </table>
            <p class="nota">Los técnicos con tickets asignados no se eliminan: se desactivan.</p>
        </main>
    </body>
</html>

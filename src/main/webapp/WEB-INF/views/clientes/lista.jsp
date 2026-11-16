<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 6: listado de clientes. Solo presenta datos; no contiene SQL. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Clientes | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud">
            <p><a href="${pageContext.request.contextPath}/panel">&larr; Volver al panel</a></p>
            <h1>Clientes</h1>

            <c:if test="${not empty sessionScope.flash}">
                <p class="alerta exito"><c:out value="${sessionScope.flash}"/></p>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <p class="alerta error"><c:out value="${sessionScope.flashError}"/></p>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <div class="barra-crud">
                <a class="boton" href="${pageContext.request.contextPath}/clientes?accion=nuevo">Nuevo cliente</a>

                <%-- Ejercicios 1 y 2: búsqueda por texto y filtro de activos (GET) --%>
                <form method="get" action="${pageContext.request.contextPath}/clientes" class="form-busqueda">
                    <input type="search" name="q" value="<c:out value='${q}'/>"
                           placeholder="Buscar por nombre o correo" maxlength="160">
                    <select name="filtro">
                        <option value="todos" ${filtro == 'todos' ? 'selected' : ''}>Todos</option>
                        <option value="activos" ${filtro == 'activos' ? 'selected' : ''}>Solo activos</option>
                    </select>
                    <button type="submit">Filtrar</button>
                    <c:if test="${not empty q or filtro == 'activos'}">
                        <a href="${pageContext.request.contextPath}/clientes">Limpiar</a>
                    </c:if>
                </form>
            </div>

            <table class="tabla-crud">
                <thead>
                    <tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${clientes}">
                        <tr class="${c.activo ? '' : 'inactivo'}">
                            <td><c:out value="${c.idCliente}"/></td>
                            <td><c:out value="${c.nombre}"/></td>
                            <td><c:out value="${c.correo}"/></td>
                            <td><span class="estado ${c.activo ? 'activo' : 'inactivo'}">
                                    <c:out value="${c.activo ? 'Activo' : 'Inactivo'}"/></span></td>
                            <td class="acciones">
                                <a href="${pageContext.request.contextPath}/clientes?accion=editar&id=${c.idCliente}">Editar</a>

                                <%-- Las modificaciones se envían por POST, nunca por un enlace GET --%>
                                <form method="post" action="${pageContext.request.contextPath}/clientes">
                                    <input type="hidden" name="accion" value="estado">
                                    <input type="hidden" name="id" value="${c.idCliente}">
                                    <input type="hidden" name="activo" value="${!c.activo}">
                                    <button type="submit">${c.activo ? 'Desactivar' : 'Activar'}</button>
                                </form>

                                <form method="post" action="${pageContext.request.contextPath}/clientes"
                                      onsubmit="return confirm('¿Eliminar definitivamente este cliente? Esta acción no se puede deshacer.');">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="${c.idCliente}">
                                    <button type="submit" class="peligro">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty clientes}">
                        <tr><td colspan="5">No hay clientes que coincidan.</td></tr>
                    </c:if>
                </tbody>
            </table>
            <p class="nota">Total: ${clientes.size()} cliente(s). Los clientes con equipos o tickets
                no se eliminan: se desactivan para conservar su historial.</p>
        </main>
    </body>
</html>

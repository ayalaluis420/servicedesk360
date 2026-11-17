<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Guía 7: listado de tickets con datos descriptivos (JOIN), filtros y paginación. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Tickets | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud ancho">
            <p><a href="${pageContext.request.contextPath}/panel">&larr; Volver al panel</a></p>
            <h1>Tickets de soporte</h1>

            <c:if test="${not empty sessionScope.flash}">
                <p class="alerta exito"><c:out value="${sessionScope.flash}"/></p>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <p class="alerta error"><c:out value="${sessionScope.flashError}"/></p>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <div class="barra-crud">
                <div class="acciones">
                    <a class="boton" href="${pageContext.request.contextPath}/tickets?accion=nuevo">Nuevo ticket</a>
                    <a href="${pageContext.request.contextPath}/tickets?accion=productividad">Productividad por técnico</a>
                </div>
                <%-- Filtros por GET: la URL es reproducible y los valores viajan como parámetros --%>
                <form method="get" action="${pageContext.request.contextPath}/tickets" class="form-busqueda">
                    <select name="estado" aria-label="Estado">
                        <option value="">Todos los estados</option>
                        <c:forEach var="e" items="${estados}">
                            <option value="${e}" ${e == estado ? 'selected' : ''}>${e}</option>
                        </c:forEach>
                    </select>
                    <select name="prioridad" aria-label="Prioridad">
                        <option value="">Todas las prioridades</option>
                        <c:forEach var="p" items="${prioridades}">
                            <option value="${p}" ${p == prioridad ? 'selected' : ''}>${p}</option>
                        </c:forEach>
                    </select>
                    <button type="submit">Filtrar</button>
                    <c:if test="${not empty estado or not empty prioridad}">
                        <a href="${pageContext.request.contextPath}/tickets">Limpiar</a>
                    </c:if>
                </form>
            </div>

            <table class="tabla-crud">
                <thead>
                    <tr><th>ID</th><th>Título</th><th>Cliente</th><th>Equipo</th><th>Categoría</th>
                        <th>Técnico</th><th>Prioridad</th><th>Estado</th><th>Creado</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="t" items="${tickets}">
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/tickets?accion=ver&id=${t.idTicket}">#${t.idTicket}</a></td>
                            <td><c:out value="${t.titulo}"/></td>
                            <td><c:out value="${t.cliente}"/></td>
                            <td><c:out value="${empty t.equipo ? '—' : t.equipo}"/></td>
                            <td><c:out value="${t.categoria}"/></td>
                            <td><c:out value="${empty t.tecnico ? 'Sin asignar' : t.tecnico}"/></td>
                            <td><span class="prioridad ${fn:toLowerCase(t.prioridad)}"><c:out value="${t.prioridad}"/></span></td>
                            <td><span class="estado-ticket"><c:out value="${t.estado}"/></span></td>
                            <td class="nowrap"><c:out value="${fn:substring(fn:replace(t.fechaCreacion, 'T', ' '), 0, 16)}"/></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty tickets}">
                        <tr><td colspan="9">No hay tickets con esos filtros.</td></tr>
                    </c:if>
                </tbody>
            </table>

            <%-- Paginación: conserva los filtros en los enlaces --%>
            <nav class="paginacion" aria-label="Paginación">
                <c:url var="urlAnterior" value="/tickets">
                    <c:param name="estado" value="${estado}"/><c:param name="prioridad" value="${prioridad}"/>
                    <c:param name="pagina" value="${pagina - 1}"/>
                </c:url>
                <c:url var="urlSiguiente" value="/tickets">
                    <c:param name="estado" value="${estado}"/><c:param name="prioridad" value="${prioridad}"/>
                    <c:param name="pagina" value="${pagina + 1}"/>
                </c:url>
                <c:choose>
                    <c:when test="${pagina > 1}"><a href="${urlAnterior}">&larr; Anterior</a></c:when>
                    <c:otherwise><span class="deshabilitado">&larr; Anterior</span></c:otherwise>
                </c:choose>
                <span>Página ${pagina} de ${totalPaginas} &middot; ${total} ticket(s)</span>
                <c:choose>
                    <c:when test="${pagina < totalPaginas}"><a href="${urlSiguiente}">Siguiente &rarr;</a></c:when>
                    <c:otherwise><span class="deshabilitado">Siguiente &rarr;</span></c:otherwise>
                </c:choose>
            </nav>
        </main>
    </body>
</html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- Guía 7, Ejercicio 2: detalle del ticket con su historial; cambio de estado (Parte J). --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Ticket #${ticket.idTicket} | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud">
            <p><a href="${pageContext.request.contextPath}/tickets">&larr; Volver al listado</a></p>
            <h1>Ticket #${ticket.idTicket}: <c:out value="${ticket.titulo}"/></h1>

            <c:if test="${not empty sessionScope.flash}">
                <p class="alerta exito"><c:out value="${sessionScope.flash}"/></p>
                <c:remove var="flash" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.flashError}">
                <p class="alerta error"><c:out value="${sessionScope.flashError}"/></p>
                <c:remove var="flashError" scope="session"/>
            </c:if>

            <dl class="ficha">
                <dt>Cliente</dt><dd><c:out value="${ticket.cliente}"/></dd>
                <dt>Equipo</dt><dd><c:out value="${empty ticket.equipo ? '—' : ticket.equipo}"/></dd>
                <dt>Categoría</dt><dd><c:out value="${ticket.categoria}"/></dd>
                <dt>Técnico</dt><dd><c:out value="${empty ticket.tecnico ? 'Sin asignar' : ticket.tecnico}"/></dd>
                <dt>Prioridad</dt><dd><span class="prioridad ${fn:toLowerCase(ticket.prioridad)}"><c:out value="${ticket.prioridad}"/></span></dd>
                <dt>Estado</dt><dd><span class="estado-ticket"><c:out value="${ticket.estado}"/></span></dd>
                <dt>Creado</dt><dd><c:out value="${fn:substring(fn:replace(ticket.fechaCreacion, 'T', ' '), 0, 16)}"/></dd>
                <dt>Descripción</dt><dd><c:out value="${ticket.descripcion}"/></dd>
            </dl>

            <h2>Cambiar estado</h2>
            <c:choose>
                <c:when test="${empty transiciones}">
                    <p class="nota">El ticket está <strong>${ticket.estado}</strong>: es un estado final y no admite cambios.</p>
                </c:when>
                <c:otherwise>
                    <%-- estadoEsperado = estado que se ve en pantalla (control optimista) --%>
                    <form method="post" action="${pageContext.request.contextPath}/tickets" class="form-busqueda">
                        <input type="hidden" name="accion" value="estado">
                        <input type="hidden" name="id" value="${ticket.idTicket}">
                        <input type="hidden" name="estadoEsperado" value="${ticket.estado}">
                        <label for="nuevoEstado">De ${ticket.estado} a</label>
                        <select id="nuevoEstado" name="nuevoEstado">
                            <c:forEach var="e" items="${transiciones}">
                                <option value="${e}">${e}</option>
                            </c:forEach>
                        </select>
                        <input type="text" name="comentario" maxlength="200" placeholder="Comentario (opcional)">
                        <button type="submit">Actualizar</button>
                    </form>
                </c:otherwise>
            </c:choose>

            <h2>Historial de seguimientos</h2>
            <table class="tabla-crud">
                <thead><tr><th>#</th><th>Fecha</th><th>Detalle</th></tr></thead>
                <tbody>
                    <c:forEach var="s" items="${seguimientos}" varStatus="st">
                        <tr>
                            <td>${st.count}</td>
                            <td class="nowrap"><c:out value="${fn:substring(fn:replace(s.fechaRegistro, 'T', ' '), 0, 16)}"/></td>
                            <td><c:out value="${s.detalle}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </main>
    </body>
</html>

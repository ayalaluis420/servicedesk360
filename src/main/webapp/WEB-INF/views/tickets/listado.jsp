<%-- 
    Document   : listado
    Created on : 22 sept 2026, 22:23:39
    Author     : ayala
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tickets</title>
    </head>
    <body>
        <h1>Tickets de soporte</h1>
    <c:if test="${not empty mensajeExito}">
        <p><c:out value="${mensajeExito}" /></p>
</c:if>
<p><a href="${pageContext.request.contextPath}/tickets/nuevo">
        Abrir nuevo ticket</a> |
    <a href="${pageContext.request.contextPath}/tickets/bd">
        Ver tickets guardados en MySQL (Guía 5)</a></p>
<table border="1">
    <thead>
        <tr>
            <th>ID</th><th>Título</th><th>Solicitante</th>
            <th>Prioridad</th><th>Estado</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="ticket" items="${tickets}">
        <tr>
            <td><c:out value="${ticket.id}" /></td>
        <td><c:out value="${ticket.titulo}" /></td>
        <td><c:out value="${ticket.solicitante.nombreCompleto}" /></td>
        <td><c:out value="${ticket.prioridad}" /></td>
        <td><c:out value="${ticket.estado}" /></td>
        </tr>
    </c:forEach>
</tbody>
</table>
</body>
</html>


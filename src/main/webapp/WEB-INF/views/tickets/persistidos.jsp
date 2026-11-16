<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tickets en MySQL</title>
    </head>
    <body>
        <h1>Tickets guardados en MySQL</h1>
        <p>Vista de laboratorio de la Guía 5. Los registros se leen con JDBC y
            se conservan aunque Tomcat se reinicie.</p>

        <c:if test="${not empty mensajeError}">
            <p><strong><c:out value="${mensajeError}" /></strong></p>
        </c:if>

        <c:if test="${empty mensajeError}">
            <table border="1">
                <thead>
                    <tr>
                        <th>ID</th><th>Título</th><th>Cliente</th>
                        <th>Categoría</th><th>Prioridad</th><th>Estado</th>
                        <th>Fecha de creación</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="ticket" items="${tickets}">
                        <tr>
                            <td><c:out value="${ticket.idTicket}" /></td>
                            <td><c:out value="${ticket.titulo}" /></td>
                            <td><c:out value="${ticket.cliente}" /></td>
                            <td><c:out value="${ticket.categoria}" /></td>
                            <td><c:out value="${ticket.prioridad}" /></td>
                            <td><c:out value="${ticket.estado}" /></td>
                            <td><c:out value="${ticket.fechaCreacion}" /></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>

        <p><a href="${pageContext.request.contextPath}/tickets">
                Volver al listado en memoria</a></p>
    </body>
</html>

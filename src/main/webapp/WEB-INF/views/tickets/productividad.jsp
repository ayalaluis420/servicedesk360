<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 7, Ejercicio 1: tickets por técnico y estado, de mayor a menor. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Productividad | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-crud">
            <p><a href="${pageContext.request.contextPath}/tickets">&larr; Volver al listado</a></p>
            <h1>Productividad por técnico</h1>
            <p class="nota">Cantidad de tickets por técnico y estado (GROUP BY), ordenada de mayor a menor.</p>
            <table class="tabla-crud">
                <thead><tr><th>Técnico</th><th>Estado</th><th>Tickets</th></tr></thead>
                <tbody>
                    <c:forEach var="f" items="${filas}">
                        <tr>
                            <td><c:out value="${f.tecnico}"/></td>
                            <td><span class="estado-ticket"><c:out value="${f.estado}"/></span></td>
                            <td><strong>${f.total}</strong></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty filas}"><tr><td colspan="3">Sin datos.</td></tr></c:if>
                </tbody>
            </table>
        </main>
    </body>
</html>

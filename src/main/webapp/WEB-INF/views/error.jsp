<%-- 
    Document   : error
    Created on : 25 oct 2026, 22:40:20
    Author     : ayala
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Error controlado</title>
</head>
<body>
    <h1>No fue posible completar la operación</h1>
    <p><c:out value="${mensajeError}" /></p>
    <a href="${pageContext.request.contextPath}/panel">Volver al panel</a>
</body>
</html>

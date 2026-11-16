<%-- 
    Document   : nuevo
    Created on : 22 sept 2026, 21:29:10
    Author     : ayala
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nuevo ticket | ServiceDesk 360</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-formulario">
            <h1>Registrar ticket de soporte</h1>

            <c:if test="${not empty errores}">
                <div class="alerta error">
                    <ul>
                        <c:forEach var="error" items="${errores}">
                            <li><c:out value="${error}" /></li>
                            </c:forEach>
                    </ul>
                </div>
            </c:if>

            <form method="post"
                  action="${pageContext.request.contextPath}/tickets/nuevo">
                <div class="grupo-campo">
                    <label for="titulo">Título</label>
                    <input id="titulo" type="text" name="titulo" maxlength="100"
                           value="<c:out value='${tituloAnterior}' />" required>
                </div>
                <div class="grupo-campo">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="6" required><c:out
                            value="${descripcionAnterior}" /></textarea>
                </div>
                <div class="grupo-campo">
                    <label for="prioridad">Prioridad</label>
                    <select id="prioridad" name="prioridad" required>
                        <option value="">Seleccione</option>
                        <option value="BAJA" ${prioridadAnterior == 'BAJA' ? 'selected' : ''}>Baja</option>
                        <option value="MEDIA" ${prioridadAnterior == 'MEDIA' ? 'selected' : ''}>Media</option>
                        <option value="ALTA" ${prioridadAnterior == 'ALTA' ? 'selected' : ''}>Alta</option>
                        <option value="CRITICA" ${prioridadAnterior == 'CRITICA' ? 'selected' : ''}>Crítica</option>
                    </select>
                </div>
                <div class="acciones-formulario">
                    <button type="submit">Registrar ticket</button>
                    <a href="${pageContext.request.contextPath}/tickets">Volver al listado</a>
                </div>
            </form>
        </main>
    </body>
</html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 6: alta y edición de clientes. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${empty cliente.idCliente ? 'Nuevo' : 'Editar'} cliente | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-formulario">
            <h1>${empty cliente.idCliente ? 'Nuevo cliente' : 'Editar cliente'}</h1>
            <c:if test="${not empty cliente.idCliente}">
                <p class="nota">ID: <c:out value="${cliente.idCliente}"/> (generado por MySQL, no editable)</p>
            </c:if>

            <c:if test="${not empty error}">
                <p class="alerta error"><c:out value="${error}"/></p>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/clientes">
                <input type="hidden" name="accion" value="guardar">
                <input type="hidden" name="idCliente" value="<c:out value='${cliente.idCliente}'/>">

                <div class="grupo-campo">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" value="<c:out value='${cliente.nombre}'/>"
                           required minlength="3" maxlength="120">
                </div>

                <div class="grupo-campo">
                    <label for="correo">Correo</label>
                    <input type="email" id="correo" name="correo" value="<c:out value='${cliente.correo}'/>"
                           required maxlength="160">
                </div>

                <div class="acciones-formulario">
                    <button type="submit">Guardar</button>
                    <a href="${pageContext.request.contextPath}/clientes">Cancelar</a>
                </div>
            </form>
        </main>
    </body>
</html>

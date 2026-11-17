<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 7: alta y edición de equipos (catálogo de clientes desde MySQL). --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${empty equipo.idEquipo ? 'Nuevo' : 'Editar'} equipo | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-formulario">
            <h1>${empty equipo.idEquipo ? 'Nuevo equipo' : 'Editar equipo'}</h1>
            <c:if test="${not empty error}">
                <p class="alerta error"><c:out value="${error}"/></p>
            </c:if>
            <form method="post" action="${pageContext.request.contextPath}/equipos">
                <input type="hidden" name="accion" value="guardar">
                <input type="hidden" name="idEquipo" value="<c:out value='${equipo.idEquipo}'/>">
                <div class="grupo-campo">
                    <label for="idCliente">Cliente</label>
                    <select id="idCliente" name="idCliente" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="cl" items="${clientes}">
                            <option value="${cl.idCliente}" ${cl.idCliente == equipo.idCliente ? 'selected' : ''}><c:out value="${cl.nombre}${cl.activo ? '' : ' (inactivo)'}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="grupo-campo">
                    <label for="codigoInventario">Código de inventario</label>
                    <input type="text" id="codigoInventario" name="codigoInventario"
                           value="<c:out value='${equipo.codigoInventario}'/>" required minlength="3" maxlength="40">
                </div>
                <div class="grupo-campo">
                    <label for="tipo">Tipo</label>
                    <input type="text" id="tipo" name="tipo" value="<c:out value='${equipo.tipo}'/>"
                           required minlength="2" maxlength="80" placeholder="Laptop, Impresora, Monitor...">
                </div>
                <div class="fila-campos">
                    <div class="grupo-campo">
                        <label for="marca">Marca</label>
                        <input type="text" id="marca" name="marca" value="<c:out value='${equipo.marca}'/>" maxlength="80">
                    </div>
                    <div class="grupo-campo">
                        <label for="modelo">Modelo</label>
                        <input type="text" id="modelo" name="modelo" value="<c:out value='${equipo.modelo}'/>" maxlength="80">
                    </div>
                </div>
                <div class="acciones-formulario">
                    <button type="submit">Guardar</button>
                    <a href="${pageContext.request.contextPath}/equipos">Cancelar</a>
                </div>
            </form>
        </main>
    </body>
</html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 6: alta y edición del personal técnico. --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>${empty tecnico.idTecnico ? 'Nuevo' : 'Editar'} técnico | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-formulario">
            <h1>${empty tecnico.idTecnico ? 'Nuevo técnico' : 'Editar técnico'}</h1>
            <c:if test="${not empty tecnico.idTecnico}">
                <p class="nota">ID: <c:out value="${tecnico.idTecnico}"/> (generado por MySQL, no editable)</p>
            </c:if>

            <c:if test="${not empty error}">
                <p class="alerta error"><c:out value="${error}"/></p>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/tecnicos">
                <input type="hidden" name="accion" value="guardar">
                <input type="hidden" name="idTecnico" value="<c:out value='${tecnico.idTecnico}'/>">

                <div class="grupo-campo">
                    <label for="nombre">Nombre</label>
                    <input type="text" id="nombre" name="nombre" value="<c:out value='${tecnico.nombre}'/>"
                           required minlength="3" maxlength="120">
                </div>
                <div class="grupo-campo">
                    <label for="correo">Correo</label>
                    <input type="email" id="correo" name="correo" value="<c:out value='${tecnico.correo}'/>"
                           required maxlength="160">
                </div>
                <div class="grupo-campo">
                    <label for="especialidad">Especialidad (opcional)</label>
                    <input type="text" id="especialidad" name="especialidad"
                           value="<c:out value='${tecnico.especialidad}'/>" maxlength="100">
                </div>
                <div class="grupo-campo">
                    <label for="nivelSoporte">Nivel de soporte</label>
                    <select id="nivelSoporte" name="nivelSoporte">
                        <option value="1" ${tecnico.nivelSoporte == 1 ? 'selected' : ''}>1 - Básico</option>
                        <option value="2" ${tecnico.nivelSoporte == 2 ? 'selected' : ''}>2 - Intermedio</option>
                        <option value="3" ${tecnico.nivelSoporte == 3 ? 'selected' : ''}>3 - Avanzado (atiende CRITICA)</option>
                    </select>
                </div>

                <div class="acciones-formulario">
                    <button type="submit">Guardar</button>
                    <a href="${pageContext.request.contextPath}/tecnicos">Cancelar</a>
                </div>
            </form>
        </main>
    </body>
</html>

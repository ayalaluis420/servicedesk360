<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Guía 7: registro de ticket + seguimiento inicial con catálogos dinámicos (Partes G y H). --%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Nuevo ticket | ServiceDesk 360</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="contenedor-formulario ancho-formulario">
            <h1>Nuevo ticket</h1>
            <p class="nota">El ticket y su seguimiento inicial se guardan juntos en una sola transacción.</p>

            <c:if test="${not empty error}">
                <p class="alerta error"><c:out value="${error}"/></p>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/tickets">
                <input type="hidden" name="accion" value="guardar">

                <div class="grupo-campo">
                    <label for="idCliente">Cliente</label>
                    <select id="idCliente" name="idCliente" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="cl" items="${clientes}">
                            <option value="${cl.idCliente}" ${cl.idCliente == ticket.idCliente ? 'selected' : ''}><c:out value="${cl.nombre}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="grupo-campo">
                    <label for="idEquipo">Equipo del cliente</label>
                    <select id="idEquipo" name="idEquipo" required>
                        <option value="">${empty equipos ? 'Seleccione primero un cliente' : 'Seleccione...'}</option>
                        <c:forEach var="equipoOpcion" items="${equipos}">
                            <option value="${equipoOpcion.idEquipo}" ${equipoOpcion.idEquipo == ticket.idEquipo ? 'selected' : ''}><c:out value="${equipoOpcion.codigoInventario} - ${equipoOpcion.tipo} ${equipoOpcion.marca} ${equipoOpcion.modelo}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="fila-campos">
                    <div class="grupo-campo">
                        <label for="idCategoria">Categoría</label>
                        <select id="idCategoria" name="idCategoria" required>
                            <option value="">Seleccione...</option>
                            <c:forEach var="cat" items="${categorias}">
                                <option value="${cat.idCategoria}" ${cat.idCategoria == ticket.idCategoria ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="grupo-campo">
                        <label for="prioridad">Prioridad</label>
                        <select id="prioridad" name="prioridad" required>
                            <option value="">Seleccione...</option>
                            <c:forEach var="p" items="${prioridades}">
                                <option value="${p}" ${p == ticket.prioridad ? 'selected' : ''}>${p}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="grupo-campo">
                    <label for="idTecnico">Técnico responsable</label>
                    <select id="idTecnico" name="idTecnico" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="tec" items="${tecnicos}">
                            <option value="${tec.idTecnico}" ${tec.idTecnico == ticket.idTecnico ? 'selected' : ''}><c:out value="${tec.nombre} (nivel ${tec.nivelSoporte})"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="grupo-campo">
                    <label for="titulo">Título</label>
                    <input type="text" id="titulo" name="titulo" value="<c:out value='${ticket.titulo}'/>"
                           required minlength="5" maxlength="120">
                </div>

                <div class="grupo-campo">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="3" required minlength="10"
                              maxlength="1000"><c:out value="${ticket.descripcion}"/></textarea>
                </div>

                <div class="grupo-campo">
                    <label for="detalleInicial">Seguimiento inicial</label>
                    <textarea id="detalleInicial" name="detalleInicial" rows="2" required
                              maxlength="800"><c:out value="${detalleInicial}"/></textarea>
                </div>

                <div class="acciones-formulario">
                    <button type="submit">Registrar ticket</button>
                    <a href="${pageContext.request.contextPath}/tickets">Cancelar</a>
                </div>
            </form>
        </main>

        <script>
            // Paso 19: al cambiar el cliente se piden sus equipos al servidor.
            // La pertenencia equipo-cliente se vuelve a validar en TicketService.
            document.getElementById('idCliente').addEventListener('change', function () {
                var select = document.getElementById('idEquipo');
                select.innerHTML = '<option value="">Cargando...</option>';
                if (!this.value) {
                    select.innerHTML = '<option value="">Seleccione primero un cliente</option>';
                    return;
                }
                fetch('${pageContext.request.contextPath}/tickets?accion=equipos&idCliente=' + encodeURIComponent(this.value))
                    .then(function (r) { return r.json(); })
                    .then(function (equipos) {
                        select.innerHTML = '';
                        var vacio = document.createElement('option');
                        vacio.value = '';
                        vacio.textContent = equipos.length ? 'Seleccione...' : 'El cliente no tiene equipos activos';
                        select.appendChild(vacio);
                        equipos.forEach(function (e) {
                            var op = document.createElement('option');
                            op.value = e.id;
                            op.textContent = e.texto;
                            select.appendChild(op);
                        });
                    })
                    .catch(function () {
                        select.innerHTML = '<option value="">No fue posible cargar los equipos</option>';
                    });
            });
        </script>
    </body>
</html>

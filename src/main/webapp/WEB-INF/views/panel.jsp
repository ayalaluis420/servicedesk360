<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Panel | ServiceDesk 360</title>
        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/resources/css/estilos.css">
    </head>
    <body>
        <main class="panel-usuario">
            <h1>Bienvenido, ${sessionScope.usuarioAutenticado.nombreCompleto}</h1>
            <p>Correo: ${sessionScope.usuarioAutenticado.correo}</p>
            <p>Rol: ${sessionScope.usuarioAutenticado.rol}</p>
            <div class="tarjetas-panel">
                <section class="tarjeta-panel">
                    <h2>Solicitudes</h2>
                    <p>Registra tickets con su seguimiento inicial y consulta su historial.</p>
                    <a href="${pageContext.request.contextPath}/tickets">Gestionar tickets</a>
                </section>
                <section class="tarjeta-panel">
                    <h2>Clientes</h2>
                    <p>Mantenimiento de clientes guardados en MySQL (Guía 6).</p>
                    <a href="${pageContext.request.contextPath}/clientes">Gestionar clientes</a>
                </section>
                <section class="tarjeta-panel">
                    <h2>Técnicos</h2>
                    <p>Catálogo del personal técnico y su nivel de soporte.</p>
                    <a href="${pageContext.request.contextPath}/tecnicos">Gestionar técnicos</a>
                </section>
                <section class="tarjeta-panel">
                    <h2>Equipos</h2>
                    <p>Registro y seguimiento de activos tecnológicos.</p>
                    <a href="${pageContext.request.contextPath}/equipos">Gestionar equipos</a>
                </section>
                <section class="tarjeta-panel">
                    <h2>Versión JSF</h2>
                    <p>Clientes y tickets con JSF y Managed Beans (Guía 8).</p>
                    <a href="${pageContext.request.contextPath}/app/inicio.xhtml">Abrir módulo JSF</a>
                </section>
                <section class="tarjeta-panel">
                    <h2>Reportes</h2>
                    <p>Se incorporarán durante la Unidad 5.</p>
                </section>
            </div>
            <form action="${pageContext.request.contextPath}/cerrar-sesion"
                  method="post" style="margin-top: 24px;">
                <button type="submit">Cerrar sesión</button>
            </form>
        </main>
    </body>
</html>
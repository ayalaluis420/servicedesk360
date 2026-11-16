package sv.edu.itca.servicedesk360.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexiones JDBC.
 *
 * <p>Las credenciales NO están en el código: se leen de un archivo de
 * propiedades ubicado fuera del repositorio, por defecto en
 * {@code ~/.servicedesk360/db.properties} (en Windows
 * {@code %USERPROFILE%\.servicedesk360\db.properties}).
 *
 * <p>Para pruebas puede indicarse otra ruta con la propiedad de sistema
 * {@code -Dservicedesk360.config=/ruta/otro.properties}.
 *
 * <p>Cada llamada a {@link #abrir()} entrega una conexión nueva que el
 * llamador debe cerrar con try-with-resources. No existe una conexión
 * global compartida.
 */
public final class ConexionBD {

    static final String PROPIEDAD_RUTA = "servicedesk360.config";

    private static volatile Properties config;

    static {
        // Fuera de un servidor, DriverManager descubre Connector/J solo.
        // Dentro de Tomcat, el driver está en WEB-INF/lib y DriverManager
        // no lo ve ("No suitable driver found"), por eso se registra aquí.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(
                    "Connector/J no está en el classpath (revise el pom.xml).", ex);
        }
    }

    private ConexionBD() {
    }

    /** Ruta del archivo de configuración que se utilizará. */
    public static Path rutaConfiguracion() {
        String personalizada = System.getProperty(PROPIEDAD_RUTA);
        if (personalizada != null && !personalizada.isBlank()) {
            return Path.of(personalizada);
        }
        return Path.of(System.getProperty("user.home"),
                ".servicedesk360", "db.properties");
    }

    private static Properties configuracion() {
        Properties actual = config;
        if (actual == null) {
            synchronized (ConexionBD.class) {
                actual = config;
                if (actual == null) {
                    actual = cargarConfiguracion();
                    config = actual;
                }
            }
        }
        return actual;
    }

    private static Properties cargarConfiguracion() {
        Path ruta = rutaConfiguracion();
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(ruta)) {
            p.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo cargar la configuración JDBC externa: " + ruta, ex);
        }
        for (String clave : new String[]{"db.url", "db.user", "db.password"}) {
            if (p.getProperty(clave) == null) {
                throw new IllegalStateException(
                        "Falta la propiedad " + clave + " en " + ruta);
            }
        }
        return p;
    }

    /** Abre una conexión nueva. Debe cerrarse con try-with-resources. */
    public static Connection abrir() throws SQLException {
        Properties p = configuracion();
        return DriverManager.getConnection(
                p.getProperty("db.url").trim(),
                p.getProperty("db.user").trim(),
                p.getProperty("db.password"));
    }

    /** URL sin parámetros, útil para bitácora sin exponer secretos. */
    public static String urlSinParametros() {
        String url = configuracion().getProperty("db.url").trim();
        int q = url.indexOf('?');
        return q < 0 ? url : url.substring(0, q);
    }
}

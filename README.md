# PermutApp Backend

Backend modular de PermutApp construido con Java 21 y Spring Boot. El repositorio contiene servicios independientes que se comunican por HTTP y usan bases PostgreSQL en Supabase.

## Estructura solicitada

```text
DOCUMENTACIÓN/
  Informe TPY1101 - PermutApp.docx
  MER/
  Presentacion PermutApp.pptx        Pendiente

GESTION/
  1.1.2 Documento de registro de definicion e identificacion del proyecto.docx
  Integrantes.txt

PRODUCTO/
  Codigo fuente/
    ServicioUsuarios/
    ServicioPublicaciones/
    ServicioProducto/
    ServicioMensajeria/
    ServicioLocalizacion/
    ServicioNotificaciones/
  Scripts SQL/
  Datos de prueba/
  Librerias y configuracion/
```

La presentacion queda pendiente porque aun no esta lista. Los scripts SQL y datos de prueba quedan preparados como carpetas de entrega para incorporar los archivos finales cuando el equipo los tenga listos.

## Servicios

- `ServicioUsuarios`: registro, login, JWT, verificacion de identidad y desactivacion logica de cuenta.
- `ServicioPublicaciones`: publicaciones creadas por usuarios.
- `ServicioProducto`: productos asociados a publicaciones, fotos y ubicacion aproximada.
- `ServicioMensajeria`: conversaciones, ofertas, mensajes y valoraciones de permutas.
- `ServicioLocalizacion`: geografia, estaciones de Metro y sugerencia de punto medio seguro.
- `ServicioNotificaciones`: historial, conteo no leido y envio push mediante Expo y Web Push.

Todos los servicios estan en `PRODUCTO/Codigo fuente`.

## Requisitos

- Java 21.
- Maven Wrapper incluido en cada servicio (`./mvnw`).
- Conexion a las bases de datos de Supabase.
- Credenciales AWS para Rekognition y Textract en `ServicioUsuarios`.

## Ejecucion local

Entrar al servicio que se quiere levantar y usar su Maven Wrapper. Ejemplo:

```bash
cd PRODUCTO/Codigo\ fuente/ServicioUsuarios
./mvnw spring-boot:run
```

Puertos usados en desarrollo:

| Servicio | Puerto local |
| --- | ---: |
| ServicioUsuarios | 5001 |
| ServicioLocalizacion | 5002 |
| ServicioProducto | 5050 |
| ServicioPublicaciones | 6001 |
| ServicioMensajeria | 7001 |
| ServicioNotificaciones | 7002 |

## Variables de entorno

Cada servicio lee un archivo `.env` dentro de su propia carpeta. Los archivos `.env.example` versionados sirven como referencia y no deben contener secretos reales.

## Validacion

Ejecutar desde la carpeta de cada servicio:

```bash
./mvnw test
```

Para empaquetar sin pruebas:

```bash
./mvnw -DskipTests package
```

## Consideraciones

- No subir secretos al repositorio.
- Mantener las variables reales en `.env` locales o en el panel del proveedor de despliegue.
- Las carpetas `target/` se ignoran porque son artefactos generados por Maven.

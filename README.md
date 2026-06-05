# PermutApp Backend

Backend modular de PermutApp construido con Java 21 y Spring Boot. El repo contiene servicios independientes que se comunican por HTTP y usan bases PostgreSQL en Supabase.

- `ServicioUsuarios`: registro, login, JWT, verificacion de identidad con AWS y desactivacion logica de cuenta.
- `ServicioPublicaciones`: publicaciones creadas por usuarios.
- `ServicioProducto`: productos asociados a publicaciones, fotos y ubicacion aproximada.
- `ServicioMensajeria`: conversaciones y mensajes para coordinar permutas.
- `ServicioLocalizacion`: geografia, estaciones de Metro y sugerencia de punto medio seguro.

## Requisitos

- Java 21
- Maven Wrapper incluido en cada servicio (`./mvnw`)
- Conexion a las bases de datos de Supabase
- Credenciales AWS para Rekognition y Textract en `ServicioUsuarios`
- SQL de soporte en `Documentacion/sql` ejecutado en Supabase cuando corresponda

## Variables de entorno

Cada servicio lee un archivo `.env` dentro de su propia carpeta.

### ServicioUsuarios

Archivo: `ServicioUsuarios/.env`

```env
SUPABASE_DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require
SUPABASE_DB_USER=usuario
SUPABASE_DB_PASSWORD=password
DB_MAX_POOL_SIZE=5
JWT_SECRET=clave-larga-para-firmar-jwt
JWT_EXPIRATION_MS=86400000
APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:19006,http://127.0.0.1:8081,http://127.0.0.1:19006
AWS_REGION=us-east-2
AWS_ACCESS_KEY_ID=access-key
AWS_SECRET_ACCESS_KEY=secret-key
IDENTITY_FACE_THRESHOLD=85
IDENTITY_REVIEWER_EMAILS=se.pinob@duocuc.cl,alf.alvarezr@duocuc.cl,jos.conchaa@duocuc.cl
```

### ServicioPublicaciones

Archivo: `ServicioPublicaciones/.env`

```env
SUPABASE_DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require
SUPABASE_DB_USER=usuario
SUPABASE_DB_PASSWORD=password
DB_MAX_POOL_SIZE=5
SERVICIO_USUARIOS_BASE_URL=http://127.0.0.1:5001
JWT_SECRET=misma-clave-usada-en-servicio-usuarios
APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:19006,http://127.0.0.1:8081,http://127.0.0.1:19006
```

### ServicioProducto

Archivo: `ServicioProducto/.env`

```env
SUPABASE_DB_URL=jdbc:postgresql://host:5432/postgres
SUPABASE_DB_USER=usuario
SUPABASE_DB_PASSWORD=password
DB_MAX_POOL_SIZE=5
SERVICIO_PUBLICACIONES_BASE_URL=http://127.0.0.1:6001
JWT_SECRET=misma-clave-usada-en-servicio-usuarios
APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:19006,http://127.0.0.1:8081,http://127.0.0.1:19006
```

### ServicioMensajeria

Archivo: `ServicioMensajeria/.env`

```env
SUPABASE_DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require
SUPABASE_DB_USER=usuario
SUPABASE_DB_PASSWORD=password
DB_MAX_POOL_SIZE=5
SERVICIO_USUARIOS_BASE_URL=http://127.0.0.1:5001
SERVICIO_PUBLICACIONES_BASE_URL=http://127.0.0.1:6001
JWT_SECRET=misma-clave-usada-en-servicio-usuarios
APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:19006,http://127.0.0.1:8081,http://127.0.0.1:19006
```

### ServicioLocalizacion

Archivo: `ServicioLocalizacion/.env`

```env
SUPABASE_DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require
SUPABASE_DB_USER=usuario
SUPABASE_DB_PASSWORD=password
DB_MAX_POOL_SIZE=2
SERVER_PORT=5002
```

## SQL de Supabase

Los scripts estan en `Documentacion/sql`.

- `supabase_localizacion_metro.sql`: crea tablas de geografia/localizacion y carga estaciones de Metro de Santiago con coordenadas para la primera fase de puntos seguros.
- `supabase_producto_ubicacion.sql`: agrega ubicacion aproximada a la tabla de productos.

Ejecutar cada script en el SQL Editor del proyecto Supabase correspondiente:

- `supabase_localizacion_metro.sql` en la base usada por `ServicioLocalizacion`.
- `supabase_producto_ubicacion.sql` en la base usada por `ServicioProducto`.

## Como correr el proyecto local

Abrir una terminal por servicio.

### 1. ServicioUsuarios

Puerto: `5001`

```bash
cd ServicioUsuarios
./mvnw spring-boot:run
```

### 2. ServicioPublicaciones

Puerto: `6001`

```bash
cd ServicioPublicaciones
./mvnw spring-boot:run
```

### 3. ServicioProducto

Puerto: `5050`

```bash
cd ServicioProducto
./mvnw spring-boot:run
```

### 4. ServicioMensajeria

Puerto: `7001`

```bash
cd ServicioMensajeria
./mvnw spring-boot:run
```

### 5. ServicioLocalizacion

Puerto: `5002`

```bash
cd ServicioLocalizacion
./mvnw spring-boot:run
```

Orden recomendado para levantar todo:

1. `ServicioUsuarios`
2. `ServicioPublicaciones`
3. `ServicioProducto`
4. `ServicioMensajeria`
5. `ServicioLocalizacion`

## Como correr pruebas

```bash
cd ServicioUsuarios
./mvnw test
```

```bash
cd ServicioPublicaciones
./mvnw test
```

```bash
cd ServicioProducto
./mvnw test
```

```bash
cd ServicioMensajeria
./mvnw test
```

```bash
cd ServicioLocalizacion
./mvnw test
```

## Autenticacion

El login devuelve un JWT. Para usar endpoints protegidos se debe enviar:

```http
Authorization: Bearer TU_TOKEN
```

Login:

```http
POST http://localhost:5001/auth/login
```

Registro:

```http
POST http://localhost:5001/auth/register
```

## Endpoints principales

### ServicioUsuarios - `http://localhost:5001`

Usuarios:

```http
GET    /usuario
GET    /usuario/{idUsuario}
POST   /usuario
PUT    /usuario/{id}
DELETE /usuario/{id}
```

`DELETE /usuario/{id}` no borra fisicamente el registro: deja `usu_activo=false`, cierra el acceso futuro y solo permite eliminar la cuenta propia autenticada.

Auth:

```http
POST /auth/register
POST /auth/login
```

Identidad:

```http
POST /identidad/verificar
GET  /identidad/estado/{usuarioId}
GET  /identidad/revision-manual
POST /identidad/revision-manual/{verificacionId}/resolver
```

`POST /identidad/verificar` usa `multipart/form-data`:

- `usuarioId`
- `carnet`
- `selfie`

Reglas actuales de verificacion:

- Rostro coincide + RUN coincide + nombre coincide: `APROBADA`
- Rostro coincide + RUN coincide + nombre no confirmado por OCR: `REVISION_MANUAL`
- Rostro coincide + OCR no detecta RUN: `REVISION_MANUAL`
- Rostro no coincide o RUN no coincide: `RECHAZADA`

Para listar o resolver revisiones manuales, el correo del usuario logueado debe estar en `IDENTITY_REVIEWER_EMAILS`.

Ejemplo para aprobar revision manual:

```http
POST /identidad/revision-manual/{verificacionId}/resolver
```

```json
{
  "estado": "APROBADA",
  "observacion": "Datos revisados manualmente."
}
```

Ejemplo para rechazar:

```json
{
  "estado": "RECHAZADA",
  "observacion": "Los datos del carnet no coinciden."
}
```

### ServicioPublicaciones - `http://localhost:6001`

```http
POST   /publicacion
GET    /publicacion
GET    /publicacion/{idPublicacion}
PUT    /publicacion/{idPublicacion}
DELETE /publicacion/{idPublicacion}
```

Crear publicacion requiere JWT y un body como:

```json
{
  "publ_titulo": "Bicicleta aro 26",
  "publ_descripcion": "Buen estado, lista para permutar",
  "usu_id": 1
}
```

### ServicioProducto - `http://localhost:5050`

```http
POST   /producto
GET    /producto
GET    /producto/{idProducto}
PUT    /producto/{idProducto}
DELETE /producto/{idProducto}
```

Crear producto requiere JWT y una publicacion existente. El producto puede incluir hasta 5 imagenes y ubicacion aproximada:

```json
{
  "prod_nombre": "Bicicleta",
  "prod_est": "Como nuevo",
  "prod_precio": 50000,
  "publ_id": 1,
  "prod_imagenes": ["data:image/jpeg;base64,..."],
  "prod_ubicacion_comuna": "Providencia, RM",
  "prod_ubicacion_referencia": "cerca de Metro Los Leones (L1)",
  "prod_latitud_aprox": -33.422637924811,
  "prod_longitud_aprox": -70.6089251611236
}
```

### ServicioMensajeria - `http://localhost:7001`

```http
POST /chat/conversaciones
GET  /chat/conversaciones/usuario/{usuarioId}
GET  /chat/conversaciones/{conversacionId}?usuarioId={usuarioId}
GET  /chat/conversaciones/{conversacionId}/mensajes?usuarioId={usuarioId}
POST /chat/conversaciones/{conversacionId}/mensajes
```

Ejemplo para iniciar conversacion:

```json
{
  "publ_id": 1,
  "interesado_id": 2,
  "mensaje_inicial": "Hola, me interesa coordinar una permuta."
}
```

### ServicioLocalizacion - `http://localhost:5002`

Consultas geograficas:

```http
GET /localizacion/paises
GET /localizacion/paises-con-regiones
GET /localizacion/regiones/pais/{paisId}
GET /localizacion/ciudades/region/{regionId}
GET /localizacion/comunas/ciudad/{ciudadId}
```

Metro:

```http
GET  /localizacion/metro/estaciones
GET  /localizacion/metro/lineas/{linea}
POST /localizacion/metro/punto-medio
```

`POST /localizacion/metro/punto-medio` calcula el punto medio geografico entre dos ubicaciones y devuelve la estacion de Metro mas cercana para coordinar en un lugar seguro.

```json
{
  "latitudOrigen": -33.422637924811,
  "longitudOrigen": -70.6089251611236,
  "latitudDestino": -33.4462814285225,
  "longitudDestino": -70.6604447474867
}
```

Respuesta esperada:

```json
{
  "puntoMedioLatitud": -33.43445967666605,
  "puntoMedioLongitud": -70.63468583450465,
  "estacionSugerida": {
    "id": 1,
    "nombre": "Baquedano",
    "linea": "L1",
    "latitud": -33.4373094107511,
    "longitud": -70.633288271414
  },
  "distanciaPuntoMedioKm": 0.34,
  "distanciaOrigenKm": 3.12,
  "distanciaDestinoKm": 2.91,
  "criterio": "Estacion de Metro de Santiago mas cercana al punto medio geografico entre ambos usuarios."
}
```

Tambien mantiene CRUD para paises, regiones, ciudades, comunas, direcciones, puntos de encuentro y estaciones de Metro.

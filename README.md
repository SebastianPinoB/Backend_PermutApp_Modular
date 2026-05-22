# PermutApp Backend

Backend modular de PermutApp construido con Java 21 y Spring Boot. El repo contiene tres servicios independientes:

- `ServicioUsuarios`: registro, login, JWT y verificacion de identidad.
- `ServicioPublicaciones`: publicaciones creadas por usuarios.
- `ServicioProducto`: productos asociados a publicaciones.

## Requisitos

- Java 21
- Conexion a las bases de datos de Supabase
- Credenciales AWS para usar Rekognition y Textract en `ServicioUsuarios`
- Maven Wrapper incluido en cada servicio (`./mvnw`)

## Variables de entorno

Cada servicio lee un archivo `.env` dentro de su propia carpeta.

### ServicioUsuarios

Archivo: `ServicioUsuarios/.env`

Variables principales:

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
SERVICIO_PUBLICACIONES_BASE_URL=http://127.0.0.1:6000
JWT_SECRET=misma-clave-usada-en-servicio-usuarios
APP_CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:19006,http://127.0.0.1:8081,http://127.0.0.1:19006
```

## Como correr el proyecto local

Abrir una terminal por servicio.

### 1. ServicioUsuarios

Puerto: `5001`

```bash
cd ServicioUsuarios
./mvnw spring-boot:run
```

### 2. ServicioPublicaciones

Puerto: `6000`

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

Orden recomendado para levantar todo:

1. `ServicioUsuarios`
2. `ServicioPublicaciones`
3. `ServicioProducto`

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

### ServicioPublicaciones - `http://localhost:6000`

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

Crear producto requiere JWT y una publicacion existente:

```json
{
  "prod_nombre": "Bicicleta",
  "prod_est": "Usado",
  "prod_precio": 50000,
  "publ_id": 1
}
```


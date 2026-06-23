# Conexion de ServicioPublicaciones a Supabase

El servicio lee la conexion desde variables de entorno:

```bash
export SUPABASE_DB_URL='jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require'
export SUPABASE_DB_USER='postgres.<project-ref>'
export SUPABASE_DB_PASSWORD='<your-database-password>'
export DB_MAX_POOL_SIZE='5'
```

Para este proyecto, usa el pooler de Supabase porque la conexion directa no es compatible con redes IPv4:

```bash
export SUPABASE_DB_URL='jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require'
export SUPABASE_DB_USER='postgres.fvvjuqaygnlzjnzvanmh'
export SUPABASE_DB_PASSWORD='<your-database-password>'
```

Luego inicia el servicio:

```bash
./mvnw spring-boot:run
```

Hibernate esta configurado con `spring.jpa.hibernate.ddl-auto=update`, por lo que creara o actualizara la tabla `publicacion` al conectar si el usuario tiene permisos.

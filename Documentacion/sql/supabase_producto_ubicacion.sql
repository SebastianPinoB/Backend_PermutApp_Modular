-- PermutApp - ServicioProducto: ubicacion aproximada del producto.
-- Ejecutar en el SQL Editor del proyecto Supabase usado por ServicioProducto.

alter table producto
  add column if not exists prod_ubicacion_comuna varchar(120),
  add column if not exists prod_ubicacion_referencia varchar(180),
  add column if not exists prod_latitud_aprox double precision,
  add column if not exists prod_longitud_aprox double precision;

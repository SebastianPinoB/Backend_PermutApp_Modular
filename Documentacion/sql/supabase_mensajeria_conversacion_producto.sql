-- PermutApp - Conversaciones asociadas a un producto
-- Ejecutar en el SQL Editor del proyecto Supabase usado por ServicioMensajeria.
-- Los chats historicos mantienen prod_id null y no se reutilizan para productos nuevos.

alter table mensajeria_conversacion
  add column if not exists prod_id integer;

alter table mensajeria_conversacion
  add column if not exists conv_oculta_autor boolean not null default false,
  add column if not exists conv_oculta_interesado boolean not null default false;

create index if not exists mensajeria_conversacion_producto_idx
  on mensajeria_conversacion (prod_id);

create unique index if not exists mensajeria_conversacion_producto_interesado_activa_uidx
  on mensajeria_conversacion (prod_id, interesado_id)
  where conv_activa = true and prod_id is not null;

select conv_id, publ_id, prod_id, publ_autor_id, interesado_id, conv_activa,
       conv_oculta_autor, conv_oculta_interesado
  from mensajeria_conversacion
 order by conv_id;

-- PermutApp - Comunas de la Region Metropolitana
-- Ejecutar en el SQL Editor del proyecto Supabase usado por ServicioLocalizacion.
-- Es idempotente: puede ejecutarse nuevamente sin duplicar comunas.

do $$
declare
  ciudad_santiago_id integer;
begin
  select c.ciudad_id
    into ciudad_santiago_id
    from ciudad c
    join region r on r.regi_id = c.regi_id
    join pais p on p.pais_id = r.pais_id
   where lower(p.pais_nombre) = lower('Chile')
     and lower(r.regi_nombre) in (
       lower('Region Metropolitana'),
       lower('Región Metropolitana')
     )
     and lower(c.ciudad_nombre) = lower('Santiago')
   limit 1;

  if ciudad_santiago_id is null then
    raise exception 'No existe la cadena Chile > Region Metropolitana > Santiago';
  end if;

  insert into comuna (comu_nombre, ciu_id)
  select nombre, ciudad_santiago_id
    from (values
      ('Alhué'),
      ('Buin'),
      ('Calera de Tango'),
      ('Cerrillos'),
      ('Cerro Navia'),
      ('Colina'),
      ('Conchalí'),
      ('Curacaví'),
      ('El Bosque'),
      ('El Monte'),
      ('Estación Central'),
      ('Huechuraba'),
      ('Independencia'),
      ('Isla de Maipo'),
      ('La Cisterna'),
      ('La Florida'),
      ('La Granja'),
      ('La Pintana'),
      ('La Reina'),
      ('Lampa'),
      ('Las Condes'),
      ('Lo Barnechea'),
      ('Lo Espejo'),
      ('Lo Prado'),
      ('Macul'),
      ('Maipú'),
      ('María Pinto'),
      ('Melipilla'),
      ('Ñuñoa'),
      ('Padre Hurtado'),
      ('Paine'),
      ('Pedro Aguirre Cerda'),
      ('Peñaflor'),
      ('Peñalolén'),
      ('Pirque'),
      ('Providencia'),
      ('Pudahuel'),
      ('Puente Alto'),
      ('Quilicura'),
      ('Quinta Normal'),
      ('Recoleta'),
      ('Renca'),
      ('San Bernardo'),
      ('San Joaquín'),
      ('San José de Maipo'),
      ('San Miguel'),
      ('San Pedro'),
      ('San Ramón'),
      ('Santiago'),
      ('Talagante'),
      ('Tiltil'),
      ('Vitacura')
    ) as comunas(nombre)
   where not exists (
     select 1
       from comuna existente
      where existente.ciu_id = ciudad_santiago_id
        and lower(existente.comu_nombre) = lower(comunas.nombre)
   );
end $$;

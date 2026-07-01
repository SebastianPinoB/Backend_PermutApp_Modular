package com.example.PermutApp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.PermutApp.model.entities.Publicacion;
import com.example.PermutApp.repository.PublicacionRepository;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {
   @Mock private WebClient usuarioWebClient;
   @Mock private PublicacionRepository repository;

   @Test
   void finalizarPermutaDesactivaAmbasPublicacionesAunqueYaEstuvieranInactivas() {
      Publicacion original = publicacion(1, true);
      Publicacion ofrecida = publicacion(2, false);
      when(repository.findById(1)).thenReturn(Optional.of(original));
      when(repository.findById(2)).thenReturn(Optional.of(ofrecida));
      PublicacionService service = new PublicacionService(usuarioWebClient, repository);

      service.finalizarPermuta(1, 2);

      assertFalse(original.getPubl_activo());
      assertFalse(ofrecida.getPubl_activo());
      verify(repository).saveAll(anyList());
   }

   @Test
   void desactivarPorUsuarioRetiraTodasSusPublicacionesActivas() {
      when(repository.desactivarPorAutor(7)).thenReturn(3);
      PublicacionService service = new PublicacionService(usuarioWebClient, repository);

      int desactivadas = service.desactivarPorUsuario(7);

      verify(repository).desactivarPorAutor(7);
      org.junit.jupiter.api.Assertions.assertEquals(3, desactivadas);
   }

   private Publicacion publicacion(int id, boolean activa) {
      Publicacion publicacion = new Publicacion();
      publicacion.setPubl_id(id);
      publicacion.setPubl_activo(activa);
      return publicacion;
   }
}

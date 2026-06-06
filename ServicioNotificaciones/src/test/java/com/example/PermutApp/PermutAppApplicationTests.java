package com.example.PermutApp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PermutAppApplicationTests {
   @Autowired
   private MockMvc mockMvc;

   @Test
   void contextLoads() { }

   @Test
   void creaEventoInternoConClaveValida() throws Exception {
      mockMvc.perform(post("/internal/notificaciones/eventos")
                  .header("X-Internal-Api-Key", "change-me-internal-notification-key")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
                        {
                          "usuarioId": 7,
                          "tipo": "PROPUESTA_PERMUTA",
                          "conversacionId": 11,
                          "publicacionId": 13,
                          "publicacionTitulo": "Bicicleta"
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notif_tipo").value("PROPUESTA_PERMUTA"))
            .andExpect(jsonPath("$.notif_datos.ruta").value("/chat/11"));
   }

   @Test
   void rechazaEventoInternoSinClave() throws Exception {
      mockMvc.perform(post("/internal/notificaciones/eventos")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("""
                        { "usuarioId": 7, "tipo": "MENSAJE_NUEVO", "conversacionId": 11 }
                        """))
            .andExpect(status().isUnauthorized());
   }
}

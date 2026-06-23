package com.example.PermutApp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.PermutApp.model.dto.VerificacionIdentidadDto;
import com.example.PermutApp.model.request.ResolverRevisionIdentidadRequest;
import com.example.PermutApp.service.IdentidadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("identidad")
public class IdentidadController {

   private final IdentidadService identidadService;

   public IdentidadController(IdentidadService identidadService) {
      this.identidadService = identidadService;
   }

   @PostMapping("verificar")
   public VerificacionIdentidadDto verificarIdentidad(
         @RequestParam("usuarioId") Integer usuarioId,
         @RequestParam("carnet") MultipartFile carnet,
         @RequestParam("selfie") MultipartFile selfie,
         Authentication authentication) {
      identidadService.validarUsuarioAutenticado(usuarioId, authentication.getName());
      return identidadService.verificar(usuarioId, carnet, selfie);
   }

   @GetMapping("estado/{usuarioId}")
   public VerificacionIdentidadDto obtenerEstado(@PathVariable Integer usuarioId, Authentication authentication) {
      identidadService.validarUsuarioAutenticado(usuarioId, authentication.getName());
      return identidadService.obtenerUltimaVerificacion(usuarioId);
   }

   @GetMapping("revision-manual")
   public List<VerificacionIdentidadDto> listarRevisionManual(Authentication authentication) {
      return identidadService.listarRevisionManual(authentication.getName());
   }

   @PostMapping("revision-manual/{verificacionId}/resolver")
   public VerificacionIdentidadDto resolverRevisionManual(
         @PathVariable Integer verificacionId,
         @Valid @RequestBody ResolverRevisionIdentidadRequest request,
         Authentication authentication) {
      return identidadService.resolverRevisionManual(
            verificacionId,
            request.estado(),
            request.observacion(),
            authentication.getName());
   }
}

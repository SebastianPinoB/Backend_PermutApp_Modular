package com.example.PermutApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.PermutApp.model.Entities.PasswordRecoveryToken;

import jakarta.persistence.LockModeType;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Long> {

   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("select t from PasswordRecoveryToken t where t.prt_token_hash = :tokenHash")
   Optional<PasswordRecoveryToken> findByTokenHash(@Param("tokenHash") String tokenHash);

   @Query("""
         select t from PasswordRecoveryToken t
         where t.usu_id = :usuarioId and t.prt_used = false
         """)
   List<PasswordRecoveryToken> findActiveByUsuarioId(@Param("usuarioId") int usuarioId);

   @Modifying
   @Query(value = "delete from password_recovery_token where usu_id = :usuarioId", nativeQuery = true)
   int eliminarPorUsuario(@Param("usuarioId") int usuarioId);
}

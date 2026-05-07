package com.diveconnect.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.diveconnect.entity.TipoUsuario;
import com.diveconnect.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);
    List<Usuario> findByActivoTrue();

    // CORREGIDO: era findEmpresasActivos, el service llama findEmpresasActivas
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario = 'USUARIO_EMPRESA' AND u.activo = true")
    List<Usuario> findEmpresasActivas();

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.nombreEmpresa) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Usuario> buscarPorNombre(@Param("keyword") String keyword);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM seguidores", nativeQuery = true)
    void clearSeguidores();

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO seguidores (seguidor_id, seguido_id) VALUES (:seguidorId, :seguidoId)",
           nativeQuery = true)
    void addSeguidor(@Param("seguidorId") Long seguidorId,
                     @Param("seguidoId")  Long seguidoId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM seguidores WHERE seguidor_id = :seguidorId AND seguido_id = :seguidoId",
           nativeQuery = true)
    void removeSeguidor(@Param("seguidorId") Long seguidorId,
                        @Param("seguidoId")  Long seguidoId);

    /** True si :seguidorId sigue actualmente a :seguidoId. */
    @Query(value = "SELECT COUNT(*) FROM seguidores WHERE seguidor_id = :seguidorId AND seguido_id = :seguidoId",
           nativeQuery = true)
    long countSeguimiento(@Param("seguidorId") Long seguidorId,
                          @Param("seguidoId")  Long seguidoId);

    default boolean existsSeguimiento(Long seguidorId, Long seguidoId) {
        return countSeguimiento(seguidorId, seguidoId) > 0;
    }
}
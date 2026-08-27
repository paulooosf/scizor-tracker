package app.vercel.paulooosf.scizor_tracker.repository;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @Query("SELECT c FROM Comentario c LEFT JOIN FETCH c.usuario LEFT JOIN FETCH c.bug WHERE c.id = :id")
    Optional<Comentario> findByIdComRelacionamentos(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT c FROM Comentario c LEFT JOIN FETCH c.usuario LEFT JOIN FETCH c.bug",
           countQuery = "SELECT COUNT(c) FROM Comentario c")
    Page<Comentario> findAllComRelacionamentos(Pageable pageable);

    @Query("SELECT c FROM Comentario c LEFT JOIN FETCH c.usuario WHERE c.bug.id = :bugId ORDER BY c.dataComentario DESC")
    List<Comentario> buscarComentariosPorBugOrdenadosPorDataComRelacionamentos(@Param("bugId") Long bugId);

    @Query("SELECT c FROM Comentario c LEFT JOIN FETCH c.bug WHERE c.usuario.id = :usuarioId")
    List<Comentario> findByUsuarioIdComRelacionamentos(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.bug.id = :bugId")
    Long contarComentariosPorBug(@Param("bugId") Long bugId);
}

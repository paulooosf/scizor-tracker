package app.vercel.paulooosf.scizor_tracker.repository;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByBugId(Long bugId);

    List<Comentario> findByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM Comentario c WHERE c.bug.id = :bugId ORDER BY c.dataComentario DESC")
    List<Comentario> buscarComentariosPorBugOrdenadosPorData(@Param("bugId") Long bugId);

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.bug.id = :bugId")
    Long contarComentariosPorBug(@Param("bugId") Long bugId);
}

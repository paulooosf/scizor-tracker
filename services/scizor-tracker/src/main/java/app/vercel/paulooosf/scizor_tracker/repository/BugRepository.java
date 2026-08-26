package app.vercel.paulooosf.scizor_tracker.repository;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    List<Bug> findByProjetoId(Long projetoId);

    List<Bug> findByStatus(StatusBug status);

    List<Bug> findByPrioridade(Prioridade prioridade);

    List<Bug> findByUsuarioResponsavelId(Long usuarioId);

    List<Bug> findByProjetoIdAndStatus(Long projetoId, StatusBug status);

    @Query("SELECT b FROM Bug b WHERE b.projeto.id = :projetoId AND b.status = :status AND b.prioridade = :prioridade")
    List<Bug> buscarPorProjetoStatusEPrioridade(
        @Param("projetoId") Long projetoId,
        @Param("status") StatusBug status,
        @Param("prioridade") Prioridade prioridade
    );

    @Query("SELECT b FROM Bug b LEFT JOIN FETCH b.comentarios WHERE b.id = :id")
    Bug findByIdComComentarios(Long id);

    @Query("SELECT b FROM Bug b WHERE b.usuarioResponsavel IS NULL")
    List<Bug> buscarBugsSemResponsavel();

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.projeto.id = :projetoId AND b.status = :status")
    Long contarBugsPorProjetoEStatus(@Param("projetoId") Long projetoId, @Param("status") StatusBug status);

    @Query("SELECT b FROM Bug b WHERE LOWER(b.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(b.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Bug> buscarPorTermo(@Param("termo") String termo);
}

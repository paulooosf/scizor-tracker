package app.vercel.paulooosf.scizor_tracker.repository;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    @Query("SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.id = :id")
    Optional<Bug> findByIdComRelacionamentos(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel",
           countQuery = "SELECT COUNT(b) FROM Bug b")
    Page<Bug> findAllComRelacionamentos(Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.projeto.id = :projetoId",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE b.projeto.id = :projetoId")
    Page<Bug> findByProjetoId(@Param("projetoId") Long projetoId, Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.status = :status",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE b.status = :status")
    Page<Bug> findByStatus(@Param("status") StatusBug status, Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.prioridade = :prioridade",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE b.prioridade = :prioridade")
    Page<Bug> findByPrioridade(@Param("prioridade") Prioridade prioridade, Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.usuarioResponsavel.id = :usuarioId",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE b.usuarioResponsavel.id = :usuarioId")
    Page<Bug> findByUsuarioResponsavelId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE b.usuarioResponsavel IS NULL",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE b.usuarioResponsavel IS NULL")
    Page<Bug> findByUsuarioResponsavelIsNull(Pageable pageable);

    @Query(value = "SELECT b FROM Bug b LEFT JOIN FETCH b.projeto LEFT JOIN FETCH b.usuarioResponsavel WHERE LOWER(b.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(b.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))",
           countQuery = "SELECT COUNT(b) FROM Bug b WHERE LOWER(b.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(b.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Bug> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    @Query("SELECT b FROM Bug b LEFT JOIN FETCH b.comentarios WHERE b.id = :id")
    Bug findByIdComComentarios(Long id);
}

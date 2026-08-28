package app.vercel.paulooosf.scizor_tracker.repository;

import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByNomeContainingIgnoreCase(String nome);

    Page<Projeto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.bugs WHERE p.id = :id")
    Projeto findByIdComBugs(Long id);

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.projeto.id = :projetoId")
    Long contarBugsPorProjeto(Long projetoId);
}

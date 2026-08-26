package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.exception.ProjetoNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    @Transactional(readOnly = true)
    public List<Projeto> listarTodos() {
        return projetoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Projeto buscarPorId(Long id) {
        return projetoRepository.findById(id)
            .orElseThrow(() -> new ProjetoNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Projeto buscarPorIdComBugs(Long id) {
        Projeto projeto = projetoRepository.findByIdComBugs(id);
        if (projeto == null) {
            throw new ProjetoNaoEncontradoException(id);
        }
        return projeto;
    }

    @Transactional(readOnly = true)
    public List<Projeto> buscarPorNome(String nome) {
        return projetoRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional
    public Projeto criar(Projeto projeto) {
        return projetoRepository.save(projeto);
    }

    @Transactional
    public Projeto atualizar(Long id, Projeto projetoAtualizado) {
        Projeto projeto = buscarPorId(id);

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setDescricao(projetoAtualizado.getDescricao());

        return projetoRepository.save(projeto);
    }

    @Transactional
    public void deletar(Long id) {
        Projeto projeto = buscarPorId(id);
        projetoRepository.delete(projeto);
    }

    @Transactional(readOnly = true)
    public Long contarBugs(Long projetoId) {
        buscarPorId(projetoId);
        return projetoRepository.contarBugsPorProjeto(projetoId);
    }
}

package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.exception.BugNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.exception.StatusInvalidoException;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BugService {

    private final BugRepository bugRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public BugService(BugRepository bugRepository, ProjetoService projetoService, UsuarioService usuarioService) {
        this.bugRepository = bugRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Bug> listarTodos() {
        return bugRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Bug buscarPorId(Long id) {
        return bugRepository.findById(id)
            .orElseThrow(() -> new BugNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Bug buscarPorIdComComentarios(Long id) {
        Bug bug = bugRepository.findByIdComComentarios(id);
        if (bug == null) {
            throw new BugNaoEncontradoException(id);
        }
        return bug;
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarPorProjeto(Long projetoId) {
        projetoService.buscarPorId(projetoId);
        return bugRepository.findByProjetoId(projetoId);
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarPorStatus(StatusBug status) {
        return bugRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarPorPrioridade(Prioridade prioridade) {
        return bugRepository.findByPrioridade(prioridade);
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarPorResponsavel(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return bugRepository.findByUsuarioResponsavelId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarSemResponsavel() {
        return bugRepository.buscarBugsSemResponsavel();
    }

    @Transactional(readOnly = true)
    public List<Bug> buscarPorTermo(String termo) {
        return bugRepository.buscarPorTermo(termo);
    }

    @Transactional
    public Bug criar(Bug bug, Long projetoId) {
        Projeto projeto = projetoService.buscarPorId(projetoId);
        bug.setProjeto(projeto);
        bug.setStatus(StatusBug.ABERTO);
        return bugRepository.save(bug);
    }

    @Transactional
    public Bug atualizar(Long id, Bug bugAtualizado) {
        Bug bug = buscarPorId(id);

        bug.setTitulo(bugAtualizado.getTitulo());
        bug.setDescricao(bugAtualizado.getDescricao());
        bug.setPrioridade(bugAtualizado.getPrioridade());

        return bugRepository.save(bug);
    }

    @Transactional
    public Bug atualizarStatus(Long id, StatusBug novoStatus) {
        Bug bug = buscarPorId(id);
        validarTransicaoStatus(bug.getStatus(), novoStatus);
        bug.atualizarStatus(novoStatus);
        return bugRepository.save(bug);
    }

    @Transactional
    public Bug atribuirResponsavel(Long bugId, Long usuarioId) {
        Bug bug = buscarPorId(bugId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        bug.setUsuarioResponsavel(usuario);
        return bugRepository.save(bug);
    }

    @Transactional
    public Bug removerResponsavel(Long bugId) {
        Bug bug = buscarPorId(bugId);
        bug.setUsuarioResponsavel(null);
        return bugRepository.save(bug);
    }

    @Transactional
    public void deletar(Long id) {
        Bug bug = buscarPorId(id);
        bugRepository.delete(bug);
    }

    private void validarTransicaoStatus(StatusBug statusAtual, StatusBug novoStatus) {
        if (statusAtual == StatusBug.FECHADO && novoStatus != StatusBug.REABERTO) {
            throw new StatusInvalidoException("Bug fechado só pode ser reaberto");
        }

        if (statusAtual == StatusBug.ABERTO && novoStatus == StatusBug.REABERTO) {
            throw new StatusInvalidoException("Bug aberto não pode ser reaberto");
        }
    }
}

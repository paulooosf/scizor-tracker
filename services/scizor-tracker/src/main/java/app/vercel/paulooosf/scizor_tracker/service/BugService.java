package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugResponsavelAtribuidoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.exception.BugNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.exception.StatusInvalidoException;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.messaging.publicador.PublicadorEvento;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BugService {

    private final BugRepository bugRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;
    private final PublicadorEvento publicadorEvento;

    public BugService(
        BugRepository bugRepository,
        ProjetoService projetoService,
        UsuarioService usuarioService,
        PublicadorEvento publicadorEvento
    ) {
        this.bugRepository = bugRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
        this.publicadorEvento = publicadorEvento;
    }

    @Transactional(readOnly = true)
    public Page<Bug> listarTodos(Pageable pageable) {
        return bugRepository.findAllComRelacionamentos(pageable);
    }

    @Transactional(readOnly = true)
    public Bug buscarPorId(Long id) {
        return bugRepository.findByIdComRelacionamentos(id)
            .orElseThrow(() -> new BugNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarPorProjeto(Long projetoId, Pageable pageable) {
        projetoService.buscarPorId(projetoId);
        return bugRepository.findByProjetoId(projetoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarPorStatus(StatusBug status, Pageable pageable) {
        return bugRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarPorPrioridade(Prioridade prioridade, Pageable pageable) {
        return bugRepository.findByPrioridade(prioridade, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarPorResponsavel(Long usuarioId, Pageable pageable) {
        usuarioService.buscarPorId(usuarioId);
        return bugRepository.findByUsuarioResponsavelId(usuarioId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarSemResponsavel(Pageable pageable) {
        return bugRepository.findByUsuarioResponsavelIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Bug> buscarPorTermo(String termo, Pageable pageable) {
        return bugRepository.buscarPorTermo(termo, pageable);
    }

    @Transactional
    public Bug criar(Bug bug, Long projetoId) {
        Projeto projeto = projetoService.buscarPorId(projetoId);
        LocalDateTime agora = LocalDateTime.now();
        bug.setProjeto(projeto);
        bug.setStatus(StatusBug.ABERTO);
        bug.setDataCriacao(agora);
        bug.setDataAtualizacao(agora);

        Bug bugSalvo = bugRepository.save(bug);
        publicadorEvento.publicar(
            TopicosKafka.BUG_CRIADO,
            String.valueOf(bugSalvo.getId()),
            new BugCriadoEvento(
                bugSalvo.getId(),
                bugSalvo.getTitulo(),
                bugSalvo.getPrioridade(),
                projeto.getId(),
                projeto.getNome(),
                null,
                bugSalvo.getDataCriacao()
            )
        );
        return bugSalvo;
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
        StatusBug statusAnterior = bug.getStatus();
        validarTransicaoStatus(statusAnterior, novoStatus);
        bug.atualizarStatus(novoStatus);

        Bug bugSalvo = bugRepository.save(bug);
        publicadorEvento.publicar(
            TopicosKafka.BUG_STATUS_ALTERADO,
            String.valueOf(bugSalvo.getId()),
            new BugStatusAlteradoEvento(
                bugSalvo.getId(),
                statusAnterior,
                bugSalvo.getStatus(),
                emailResponsavel(bugSalvo),
                bugSalvo.getProjeto().getId(),
                bugSalvo.getDataAtualizacao()
            )
        );
        return bugSalvo;
    }

    @Transactional
    public Bug atribuirResponsavel(Long bugId, Long usuarioId) {
        Bug bug = buscarPorId(bugId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        bug.setUsuarioResponsavel(usuario);

        Bug bugSalvo = bugRepository.save(bug);
        publicadorEvento.publicar(
            TopicosKafka.BUG_RESPONSAVEL_ATRIBUIDO,
            String.valueOf(bugSalvo.getId()),
            new BugResponsavelAtribuidoEvento(
                bugSalvo.getId(),
                bugSalvo.getTitulo(),
                usuario.getId(),
                usuario.getEmail(),
                null
            )
        );
        return bugSalvo;
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

    private String emailResponsavel(Bug bug) {
        return bug.getUsuarioResponsavel() != null ? bug.getUsuarioResponsavel().getEmail() : null;
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

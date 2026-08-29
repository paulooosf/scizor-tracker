package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.exception.ComentarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.ComentarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final BugService bugService;
    private final UsuarioService usuarioService;

    public ComentarioService(
        ComentarioRepository comentarioRepository,
        BugService bugService,
        UsuarioService usuarioService
    ) {
        this.comentarioRepository = comentarioRepository;
        this.bugService = bugService;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public Page<Comentario> listarTodos(Pageable pageable) {
        return comentarioRepository.findAllComRelacionamentos(pageable);
    }

    @Transactional(readOnly = true)
    public Comentario buscarPorId(Long id) {
        return comentarioRepository.findByIdComRelacionamentos(id)
            .orElseThrow(() -> new ComentarioNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<Comentario> buscarPorBug(Long bugId, Pageable pageable) {
        bugService.buscarPorId(bugId);
        return comentarioRepository.findByBugId(bugId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Comentario> buscarPorUsuario(Long usuarioId, Pageable pageable) {
        usuarioService.buscarPorId(usuarioId);
        return comentarioRepository.findByUsuarioId(usuarioId, pageable);
    }

    @Transactional
    public Comentario criar(Comentario comentario, Long bugId, Long usuarioId) {
        Bug bug = bugService.buscarPorId(bugId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        comentario.setBug(bug);
        comentario.setUsuario(usuario);
        comentario.setDataComentario(LocalDateTime.now());

        return comentarioRepository.save(comentario);
    }

    @Transactional
    public Comentario atualizar(Long id, Comentario comentarioAtualizado) {
        Comentario comentario = buscarPorId(id);
        comentario.setTexto(comentarioAtualizado.getTexto());
        return comentarioRepository.save(comentario);
    }

    @Transactional
    public void deletar(Long id) {
        Comentario comentario = buscarPorId(id);
        comentarioRepository.delete(comentario);
    }

    @Transactional(readOnly = true)
    public Long contarComentariosDoBug(Long bugId) {
        bugService.buscarPorId(bugId);
        return comentarioRepository.contarComentariosPorBug(bugId);
    }
}

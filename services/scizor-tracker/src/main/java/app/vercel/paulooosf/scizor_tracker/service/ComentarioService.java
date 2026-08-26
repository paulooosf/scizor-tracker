package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.exception.ComentarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.ComentarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final BugService bugService;
    private final UsuarioService usuarioService;

    public ComentarioService(ComentarioRepository comentarioRepository, BugService bugService, UsuarioService usuarioService) {
        this.comentarioRepository = comentarioRepository;
        this.bugService = bugService;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Comentario> listarTodos() {
        return comentarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Comentario buscarPorId(Long id) {
        return comentarioRepository.findById(id)
            .orElseThrow(() -> new ComentarioNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<Comentario> buscarPorBug(Long bugId) {
        bugService.buscarPorId(bugId);
        return comentarioRepository.buscarComentariosPorBugOrdenadosPorData(bugId);
    }

    @Transactional(readOnly = true)
    public List<Comentario> buscarPorUsuario(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return comentarioRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Comentario criar(Comentario comentario, Long bugId, Long usuarioId) {
        Bug bug = bugService.buscarPorId(bugId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        comentario.setBug(bug);
        comentario.setUsuario(usuario);

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

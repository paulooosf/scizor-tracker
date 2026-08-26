package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ComentarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.ComentarioSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<Page<ComentarioSaidaDto>> listarTodos(Pageable pageable) {
        Page<ComentarioSaidaDto> comentarios = comentarioService.listarTodos(pageable)
            .map(ComentarioSaidaDto::new);
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioSaidaDto> buscarPorId(@PathVariable Long id) {
        Comentario comentario = comentarioService.buscarPorId(id);
        return ResponseEntity.ok(new ComentarioSaidaDto(comentario));
    }

    @GetMapping("/bug/{bugId}")
    public ResponseEntity<List<ComentarioSaidaDto>> buscarPorBug(@PathVariable Long bugId) {
        List<ComentarioSaidaDto> comentarios = comentarioService.buscarPorBug(bugId)
            .stream()
            .map(ComentarioSaidaDto::new)
            .toList();
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ComentarioSaidaDto>> buscarPorUsuario(@PathVariable Long usuarioId) {
        List<ComentarioSaidaDto> comentarios = comentarioService.buscarPorUsuario(usuarioId)
            .stream()
            .map(ComentarioSaidaDto::new)
            .toList();
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping
    public ResponseEntity<ComentarioSaidaDto> criar(
        @Valid @RequestBody ComentarioEntradaDto dto,
        @RequestParam Long bugId,
        @RequestParam Long usuarioId
    ) {
        Comentario comentario = comentarioService.criar(dto.converter(), bugId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ComentarioSaidaDto(comentario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioSaidaDto> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ComentarioEntradaDto dto
    ) {
        Comentario comentario = comentarioService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new ComentarioSaidaDto(comentario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        comentarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

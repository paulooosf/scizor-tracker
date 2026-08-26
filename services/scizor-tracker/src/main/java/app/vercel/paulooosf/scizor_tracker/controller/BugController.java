package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.AtualizarStatusDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.BugEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.BugSaidaDto;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.service.BugService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping
    public ResponseEntity<Page<BugSaidaDto>> listarTodos(Pageable pageable) {
        Page<BugSaidaDto> bugs = bugService.listarTodos(pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BugSaidaDto> buscarPorId(@PathVariable Long id) {
        Bug bug = bugService.buscarPorId(id);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @GetMapping("/projeto/{projetoId}")
    public ResponseEntity<List<BugSaidaDto>> buscarPorProjeto(@PathVariable Long projetoId) {
        List<BugSaidaDto> bugs = bugService.buscarPorProjeto(projetoId)
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BugSaidaDto>> buscarPorStatus(@PathVariable StatusBug status) {
        List<BugSaidaDto> bugs = bugService.buscarPorStatus(status)
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<List<BugSaidaDto>> buscarPorPrioridade(@PathVariable Prioridade prioridade) {
        List<BugSaidaDto> bugs = bugService.buscarPorPrioridade(prioridade)
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/responsavel/{usuarioId}")
    public ResponseEntity<List<BugSaidaDto>> buscarPorResponsavel(@PathVariable Long usuarioId) {
        List<BugSaidaDto> bugs = bugService.buscarPorResponsavel(usuarioId)
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/sem-responsavel")
    public ResponseEntity<List<BugSaidaDto>> buscarSemResponsavel() {
        List<BugSaidaDto> bugs = bugService.buscarSemResponsavel()
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<BugSaidaDto>> buscarPorTermo(@RequestParam String termo) {
        List<BugSaidaDto> bugs = bugService.buscarPorTermo(termo)
            .stream()
            .map(BugSaidaDto::new)
            .toList();
        return ResponseEntity.ok(bugs);
    }

    @PostMapping
    public ResponseEntity<BugSaidaDto> criar(
        @Valid @RequestBody BugEntradaDto dto,
        @RequestParam Long projetoId
    ) {
        Bug bug = bugService.criar(dto.converter(), projetoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BugSaidaDto(bug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BugSaidaDto> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody BugEntradaDto dto
    ) {
        Bug bug = bugService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BugSaidaDto> atualizarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AtualizarStatusDto dto
    ) {
        Bug bug = bugService.atualizarStatus(id, dto.status());
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @PatchMapping("/{bugId}/responsavel/{usuarioId}")
    public ResponseEntity<BugSaidaDto> atribuirResponsavel(
        @PathVariable Long bugId,
        @PathVariable Long usuarioId
    ) {
        Bug bug = bugService.atribuirResponsavel(bugId, usuarioId);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @DeleteMapping("/{bugId}/responsavel")
    public ResponseEntity<BugSaidaDto> removerResponsavel(@PathVariable Long bugId) {
        Bug bug = bugService.removerResponsavel(bugId);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        bugService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ProjetoEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.ProjetoSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.service.ProjetoService;
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
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @GetMapping
    public ResponseEntity<Page<ProjetoSaidaDto>> listarTodos(Pageable pageable) {
        Page<ProjetoSaidaDto> projetos = projetoService.listarTodos(pageable)
            .map(ProjetoSaidaDto::new);
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetoSaidaDto> buscarPorId(@PathVariable Long id) {
        Projeto projeto = projetoService.buscarPorId(id);
        return ResponseEntity.ok(new ProjetoSaidaDto(projeto));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProjetoSaidaDto>> buscarPorNome(@RequestParam String nome) {
        List<ProjetoSaidaDto> projetos = projetoService.buscarPorNome(nome)
            .stream()
            .map(ProjetoSaidaDto::new)
            .toList();
        return ResponseEntity.ok(projetos);
    }

    @PostMapping
    public ResponseEntity<ProjetoSaidaDto> criar(@Valid @RequestBody ProjetoEntradaDto dto) {
        Projeto projeto = projetoService.criar(dto.converter());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProjetoSaidaDto(projeto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetoSaidaDto> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ProjetoEntradaDto dto
    ) {
        Projeto projeto = projetoService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new ProjetoSaidaDto(projeto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        projetoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.UsuarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.UsuarioSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.service.UsuarioService;
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

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioSaidaDto>> listarTodos(Pageable pageable) {
        Page<UsuarioSaidaDto> usuarios = usuarioService.listarTodos(pageable)
            .map(UsuarioSaidaDto::new);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @GetMapping("/email")
    public ResponseEntity<UsuarioSaidaDto> buscarPorEmail(@RequestParam String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @PostMapping
    public ResponseEntity<UsuarioSaidaDto> criar(@Valid @RequestBody UsuarioEntradaDto dto) {
        Usuario usuario = usuarioService.criar(dto.converter());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioSaidaDto(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody UsuarioEntradaDto dto
    ) {
        Usuario usuario = usuarioService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

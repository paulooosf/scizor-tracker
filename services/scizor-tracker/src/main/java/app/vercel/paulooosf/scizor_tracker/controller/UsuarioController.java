package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.UsuarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.UsuarioSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
        summary = "Listar todos os usuários",
        description = "Retorna uma lista paginada de todos os usuários cadastrados. **Requer autenticação**. Senhas não são retornadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<Page<UsuarioSaidaDto>> listarTodos(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=nome,asc")
        Pageable pageable
    ) {
        Page<UsuarioSaidaDto> usuarios = usuarioService.listarTodos(pageable)
            .map(UsuarioSaidaDto::new);
        return ResponseEntity.ok(usuarios);
    }

    @Operation(
        summary = "Buscar usuário por ID",
        description = "Retorna os detalhes de um usuário específico pelo seu identificador único. **Requer autenticação**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> buscarPorId(
        @Parameter(description = "ID do usuário", required = true, example = "1")
        @PathVariable Long id
    ) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @Operation(
        summary = "Buscar usuário por email",
        description = "Busca um usuário pelo seu endereço de email. **Requer autenticação**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/email")
    public ResponseEntity<UsuarioSaidaDto> buscarPorEmail(
        @Parameter(description = "Email do usuário", required = true, example = "joao.silva@example.com")
        @RequestParam String email
    ) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @Operation(
        summary = "Criar novo usuário (cadastro público)",
        description = "Cria uma nova conta de usuário. **Endpoint público** (não requer autenticação). Usuário criado com role USER por padrão. Senha é criptografada automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já cadastrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UsuarioSaidaDto> criar(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo usuário",
            required = true
        )
        @Valid @RequestBody UsuarioEntradaDto dto
    ) {
        Usuario usuario = usuarioService.criar(dto.converter());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioSaidaDto(usuario));
    }

    @Operation(
        summary = "Atualizar usuário",
        description = "Atualiza as informações de um usuário existente. **Requer permissão ADMIN**. Se uma nova senha for fornecida, será criptografada automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já em uso", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioSaidaDto> atualizar(
        @Parameter(description = "ID do usuário a ser atualizado", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do usuário",
            required = true
        )
        @Valid @RequestBody UsuarioEntradaDto dto
    ) {
        Usuario usuario = usuarioService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new UsuarioSaidaDto(usuario));
    }

    @Operation(
        summary = "Deletar usuário",
        description = "Remove permanentemente um usuário do sistema. **Requer permissão ADMIN**. **Atenção:** Bugs atribuídos ao usuário terão o responsável removido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID do usuário a ser deletado", required = true, example = "1")
        @PathVariable Long id
    ) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

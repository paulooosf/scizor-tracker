package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ComentarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.ComentarioSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.service.ComentarioService;
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

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentários", description = "Gerenciamento de comentários em bugs")
@SecurityRequirement(name = "Bearer Authentication")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @Operation(
        summary = "Listar todos os comentários",
        description = "Retorna uma lista paginada de todos os comentários cadastrados no sistema. Requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comentários retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<ComentarioSaidaDto>> listarTodos(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=dataComentario,desc")
        Pageable pageable
    ) {
        Page<ComentarioSaidaDto> comentarios = comentarioService.listarTodos(pageable)
            .map(ComentarioSaidaDto::new);
        return ResponseEntity.ok(comentarios);
    }

    @Operation(
        summary = "Buscar comentário por ID",
        description = "Retorna os detalhes de um comentário específico pelo seu identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comentário encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comentário não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ComentarioSaidaDto> buscarPorId(
        @Parameter(description = "ID do comentário", required = true, example = "1")
        @PathVariable Long id
    ) {
        Comentario comentario = comentarioService.buscarPorId(id);
        return ResponseEntity.ok(new ComentarioSaidaDto(comentario));
    }

    @Operation(
        summary = "Buscar comentários por bug",
        description = "Retorna todos os comentários associados a um bug específico, ordenados cronologicamente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comentários do bug"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @GetMapping("/bug/{bugId}")
    public ResponseEntity<List<ComentarioSaidaDto>> buscarPorBug(
        @Parameter(description = "ID do bug", required = true, example = "1")
        @PathVariable Long bugId
    ) {
        List<ComentarioSaidaDto> comentarios = comentarioService.buscarPorBug(bugId)
            .stream()
            .map(ComentarioSaidaDto::new)
            .toList();
        return ResponseEntity.ok(comentarios);
    }

    @Operation(
        summary = "Buscar comentários por usuário",
        description = "Retorna todos os comentários feitos por um usuário específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comentários do usuário"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ComentarioSaidaDto>> buscarPorUsuario(
        @Parameter(description = "ID do usuário", required = true, example = "1")
        @PathVariable Long usuarioId
    ) {
        List<ComentarioSaidaDto> comentarios = comentarioService.buscarPorUsuario(usuarioId)
            .stream()
            .map(ComentarioSaidaDto::new)
            .toList();
        return ResponseEntity.ok(comentarios);
    }

    @Operation(
        summary = "Criar novo comentário",
        description = "Adiciona um novo comentário a um bug. **Requer permissão ADMIN**. Publica evento `comentario.adicionado` no Kafka."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comentário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug ou usuário não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ComentarioSaidaDto> criar(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo comentário",
            required = true
        )
        @Valid @RequestBody ComentarioEntradaDto dto,
        @Parameter(description = "ID do bug ao qual o comentário pertence", required = true, example = "1")
        @RequestParam Long bugId,
        @Parameter(description = "ID do usuário autor do comentário", required = true, example = "1")
        @RequestParam Long usuarioId
    ) {
        Comentario comentario = comentarioService.criar(dto.converter(), bugId, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ComentarioSaidaDto(comentario));
    }

    @Operation(
        summary = "Atualizar comentário",
        description = "Atualiza o texto de um comentário existente. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comentário não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ComentarioSaidaDto> atualizar(
        @Parameter(description = "ID do comentário a ser atualizado", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do comentário",
            required = true
        )
        @Valid @RequestBody ComentarioEntradaDto dto
    ) {
        Comentario comentario = comentarioService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new ComentarioSaidaDto(comentario));
    }

    @Operation(
        summary = "Deletar comentário",
        description = "Remove permanentemente um comentário do sistema. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comentário deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Comentário não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID do comentário a ser deletado", required = true, example = "1")
        @PathVariable Long id
    ) {
        comentarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.AtualizarStatusDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.BugEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.BugSaidaDto;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.service.BugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Bugs", description = "Gerenciamento de bugs e rastreamento de problemas")
@SecurityRequirement(name = "Bearer Authentication")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @Operation(
        summary = "Listar todos os bugs",
        description = "Retorna uma lista paginada de todos os bugs cadastrados no sistema. Requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<BugSaidaDto>> listarTodos(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.listarTodos(pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bug por ID",
        description = "Retorna os detalhes de um bug específico pelo seu identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bug encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BugSaidaDto> buscarPorId(
        @Parameter(description = "ID do bug", required = true, example = "1")
        @PathVariable Long id
    ) {
        Bug bug = bugService.buscarPorId(id);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Buscar bugs por projeto",
        description = "Retorna todos os bugs associados a um projeto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs do projeto"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content)
    })
    @GetMapping("/projeto/{projetoId}")
    public ResponseEntity<Page<BugSaidaDto>> buscarPorProjeto(
        @Parameter(description = "ID do projeto", required = true, example = "1")
        @PathVariable Long projetoId,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarPorProjeto(projetoId, pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bugs por status",
        description = "Filtra bugs pelo seu status atual (ABERTO, EM_ANDAMENTO, RESOLVIDO, FECHADO)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs com o status especificado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Status inválido", content = @Content)
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BugSaidaDto>> buscarPorStatus(
        @Parameter(description = "Status do bug", required = true, 
                   schema = @Schema(allowableValues = {"ABERTO", "EM_ANDAMENTO", "RESOLVIDO", "FECHADO"}),
                   example = "ABERTO")
        @PathVariable StatusBug status,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarPorStatus(status, pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bugs por prioridade",
        description = "Filtra bugs pela sua prioridade (BAIXA, MEDIA, ALTA, CRITICA)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs com a prioridade especificada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Prioridade inválida", content = @Content)
    })
    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<Page<BugSaidaDto>> buscarPorPrioridade(
        @Parameter(description = "Prioridade do bug", required = true,
                   schema = @Schema(allowableValues = {"BAIXA", "MEDIA", "ALTA", "CRITICA"}),
                   example = "CRITICA")
        @PathVariable Prioridade prioridade,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarPorPrioridade(prioridade, pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bugs por responsável",
        description = "Retorna todos os bugs atribuídos a um usuário específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs do responsável"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/responsavel/{usuarioId}")
    public ResponseEntity<Page<BugSaidaDto>> buscarPorResponsavel(
        @Parameter(description = "ID do usuário responsável", required = true, example = "1")
        @PathVariable Long usuarioId,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarPorResponsavel(usuarioId, pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bugs sem responsável",
        description = "Retorna todos os bugs que ainda não possuem um responsável atribuído"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs sem responsável"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping("/sem-responsavel")
    public ResponseEntity<Page<BugSaidaDto>> buscarSemResponsavel(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarSemResponsavel(pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Buscar bugs por termo",
        description = "Realiza busca textual no título e descrição dos bugs"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de bugs encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping("/buscar")
    public ResponseEntity<Page<BugSaidaDto>> buscarPorTermo(
        @Parameter(description = "Termo de busca", required = true, example = "pagamento")
        @RequestParam String termo,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=id,desc")
        Pageable pageable
    ) {
        Page<BugSaidaDto> bugs = bugService.buscarPorTermo(termo, pageable)
            .map(BugSaidaDto::new);
        return ResponseEntity.ok(bugs);
    }

    @Operation(
        summary = "Criar novo bug",
        description = "Cria um novo bug no sistema. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bug criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BugSaidaDto> criar(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo bug",
            required = true
        )
        @Valid @RequestBody BugEntradaDto dto,
        @Parameter(description = "ID do projeto ao qual o bug pertence", required = true, example = "1")
        @RequestParam Long projetoId
    ) {
        Bug bug = bugService.criar(dto.converter(), projetoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Atualizar bug",
        description = "Atualiza as informações de um bug existente. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bug atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BugSaidaDto> atualizar(
        @Parameter(description = "ID do bug a ser atualizado", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do bug",
            required = true
        )
        @Valid @RequestBody BugEntradaDto dto
    ) {
        Bug bug = bugService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Atualizar status do bug",
        description = "Altera o status de um bug. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Status inválido", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<BugSaidaDto> atualizarStatus(
        @Parameter(description = "ID do bug", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novo status do bug",
            required = true
        )
        @Valid @RequestBody AtualizarStatusDto dto
    ) {
        Bug bug = bugService.atualizarStatus(id, dto.status());
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Atribuir responsável ao bug",
        description = "Atribui um usuário como responsável pelo bug. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Responsável atribuído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug ou usuário não encontrado", content = @Content)
    })
    @PatchMapping("/{bugId}/responsavel/{usuarioId}")
    public ResponseEntity<BugSaidaDto> atribuirResponsavel(
        @Parameter(description = "ID do bug", required = true, example = "1")
        @PathVariable Long bugId,
        @Parameter(description = "ID do usuário responsável", required = true, example = "1")
        @PathVariable Long usuarioId
    ) {
        Bug bug = bugService.atribuirResponsavel(bugId, usuarioId);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Remover responsável do bug",
        description = "Remove o responsável atribuído ao bug. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Responsável removido com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @DeleteMapping("/{bugId}/responsavel")
    public ResponseEntity<BugSaidaDto> removerResponsavel(
        @Parameter(description = "ID do bug", required = true, example = "1")
        @PathVariable Long bugId
    ) {
        Bug bug = bugService.removerResponsavel(bugId);
        return ResponseEntity.ok(new BugSaidaDto(bug));
    }

    @Operation(
        summary = "Deletar bug",
        description = "Remove permanentemente um bug do sistema. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Bug deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bug não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID do bug a ser deletado", required = true, example = "1")
        @PathVariable Long id
    ) {
        bugService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

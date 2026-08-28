package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ProjetoEntradaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.ProjetoSaidaDto;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.service.ProjetoService;
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
@RequestMapping("/api/projetos")
@Tag(name = "Projetos", description = "Gerenciamento de projetos de software")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @Operation(
        summary = "Listar todos os projetos",
        description = "Retorna uma lista paginada de todos os projetos cadastrados no sistema. Requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<ProjetoSaidaDto>> listarTodos(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=nome,asc")
        Pageable pageable
    ) {
        Page<ProjetoSaidaDto> projetos = projetoService.listarTodos(pageable)
            .map(ProjetoSaidaDto::new);
        return ResponseEntity.ok(projetos);
    }

    @Operation(
        summary = "Buscar projeto por ID",
        description = "Retorna os detalhes de um projeto específico pelo seu identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projeto encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoSaidaDto> buscarPorId(
        @Parameter(description = "ID do projeto", required = true, example = "1")
        @PathVariable Long id
    ) {
        Projeto projeto = projetoService.buscarPorId(id);
        return ResponseEntity.ok(new ProjetoSaidaDto(projeto));
    }

    @Operation(
        summary = "Buscar projetos por nome",
        description = "Realiza busca textual parcial no nome do projeto (case-insensitive)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos encontrados"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping("/buscar")
    public ResponseEntity<Page<ProjetoSaidaDto>> buscarPorNome(
        @Parameter(description = "Nome ou parte do nome do projeto", required = true, example = "Sistema")
        @RequestParam String nome,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)", example = "page=0&size=10&sort=nome,asc")
        Pageable pageable
    ) {
        Page<ProjetoSaidaDto> projetos = projetoService.buscarPorNome(nome, pageable)
            .map(ProjetoSaidaDto::new);
        return ResponseEntity.ok(projetos);
    }

    @Operation(
        summary = "Criar novo projeto",
        description = "Cria um novo projeto no sistema. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProjetoSaidaDto> criar(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo projeto",
            required = true
        )
        @Valid @RequestBody ProjetoEntradaDto dto
    ) {
        Projeto projeto = projetoService.criar(dto.converter());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProjetoSaidaDto(projeto));
    }

    @Operation(
        summary = "Atualizar projeto",
        description = "Atualiza as informações de um projeto existente. **Requer permissão ADMIN**."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoSaidaDto> atualizar(
        @Parameter(description = "ID do projeto a ser atualizado", required = true, example = "1")
        @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Novos dados do projeto",
            required = true
        )
        @Valid @RequestBody ProjetoEntradaDto dto
    ) {
        Projeto projeto = projetoService.atualizar(id, dto.converter());
        return ResponseEntity.ok(new ProjetoSaidaDto(projeto));
    }

    @Operation(
        summary = "Deletar projeto",
        description = "Remove permanentemente um projeto do sistema. **Requer permissão ADMIN**. **Atenção:** Todos os bugs associados ao projeto serão deletados em cascata."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Projeto deletado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão (requer ADMIN)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID do projeto a ser deletado", required = true, example = "1")
        @PathVariable Long id
    ) {
        projetoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

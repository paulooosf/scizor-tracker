package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados de saída de um bug")
public record BugSaidaDto(
    @Schema(description = "ID único do bug", example = "1")
    @NotNull(message = "ID não preenchido!")
    Long id,

    @Schema(description = "Título do bug", example = "Erro ao salvar usuário no banco de dados")
    @NotBlank(message = "Título não preenchido!")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres!")
    String titulo,

    @Schema(description = "Descrição detalhada do bug", example = "Ao tentar salvar um novo usuário, a aplicação lança NullPointerException")
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

    @Schema(description = "Prioridade do bug", example = "ALTA")
    @NotNull(message = "Prioridade não preenchida!")
    Prioridade prioridade,

    @Schema(description = "Status atual do bug", example = "ABERTO")
    @NotNull(message = "Status não preenchido!")
    StatusBug status,

    @Schema(description = "ID do projeto ao qual o bug pertence", example = "1")
    @NotNull(message = "ID do projeto não preenchido!")
    Long projetoId,

    @Schema(description = "Nome do projeto ao qual o bug pertence", example = "Sistema de Gestão de Estoque")
    @NotBlank(message = "Nome do projeto não preenchido!")
    String projetoNome,

    @Schema(description = "Usuário responsável pelo bug (pode ser null se não atribuído)")
    UsuarioSaidaDto usuarioResponsavel,

    @Schema(description = "Data e hora de criação do bug", example = "2024-03-15T10:30:00")
    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao,

    @Schema(description = "Data e hora da última atualização", example = "2024-03-16T14:20:00")
    @NotNull(message = "Data de atualização não preenchida!")
    LocalDateTime dataAtualizacao
) {
    public BugSaidaDto(Bug bug) {
        this(
            bug.getId(),
            bug.getTitulo(),
            bug.getDescricao(),
            bug.getPrioridade(),
            bug.getStatus(),
            bug.getProjeto().getId(),
            bug.getProjeto().getNome(),
            bug.getUsuarioResponsavel() != null ? new UsuarioSaidaDto(bug.getUsuarioResponsavel()) : null,
            bug.getDataCriacao(),
            bug.getDataAtualizacao()
        );
    }
}

package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BugSaidaDto(
    @NotNull(message = "ID não preenchido!")
    Long id,

    @NotBlank(message = "Título não preenchido!")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres!")
    String titulo,

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

    @NotNull(message = "Prioridade não preenchida!")
    Prioridade prioridade,

    @NotNull(message = "Status não preenchido!")
    StatusBug status,

    @NotNull(message = "ID do projeto não preenchido!")
    Long projetoId,

    @NotBlank(message = "Nome do projeto não preenchido!")
    String projetoNome,

    UsuarioSaidaDto usuarioResponsavel,

    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao,

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

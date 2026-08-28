package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados de saída de um projeto")
public record ProjetoSaidaDto(
    @Schema(description = "ID único do projeto", example = "1")
    @NotNull(message = "ID não preenchido!")
    Long id,

    @Schema(description = "Nome do projeto", example = "Sistema de Gestão de Estoque")
    @NotBlank(message = "Nome não preenchido!")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres!")
    String nome,

    @Schema(description = "Descrição do projeto", example = "Sistema para controle de entrada e saída de produtos no estoque")
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

    @Schema(description = "Data e hora de criação do projeto", example = "2024-03-01T09:00:00")
    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao
) {
    public ProjetoSaidaDto(Projeto projeto) {
        this(projeto.getId(), projeto.getNome(), projeto.getDescricao(), projeto.getDataCriacao());
    }
}

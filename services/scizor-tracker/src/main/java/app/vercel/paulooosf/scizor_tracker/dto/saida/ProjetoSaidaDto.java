package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProjetoSaidaDto(
    @NotNull(message = "ID não preenchido!")
    Long id,

    @NotBlank(message = "Nome não preenchido!")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres!")
    String nome,

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao
) {
    public ProjetoSaidaDto(Projeto projeto) {
        this(projeto.getId(), projeto.getNome(), projeto.getDescricao(), projeto.getDataCriacao());
    }
}

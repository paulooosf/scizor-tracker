package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de entrada para criação ou atualização de um bug")
public record BugEntradaDto(
    @Schema(description = "Título do bug", example = "Erro ao salvar usuário no banco de dados", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha o título!")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres!")
    String titulo,

    @Schema(description = "Descrição detalhada do bug", example = "Ao tentar salvar um novo usuário, a aplicação lança NullPointerException na linha 42 do UserService", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

    @Schema(description = "Prioridade do bug", example = "ALTA", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"BAIXA", "MEDIA", "ALTA", "CRITICA"})
    @NotNull(message = "Preencha a prioridade!")
    Prioridade prioridade
) {
    public BugEntradaDto(Bug bug) {
        this(bug.getTitulo(), bug.getDescricao(), bug.getPrioridade());
    }

    public Bug converter() {
        Bug bug = new Bug();
        bug.setTitulo(titulo);
        bug.setDescricao(descricao);
        bug.setPrioridade(prioridade);
        return bug;
    }
}

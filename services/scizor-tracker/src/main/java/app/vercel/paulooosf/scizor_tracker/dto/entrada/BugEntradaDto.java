package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BugEntradaDto(
    @NotBlank(message = "Preencha o título!")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres!")
    String titulo,

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao,

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

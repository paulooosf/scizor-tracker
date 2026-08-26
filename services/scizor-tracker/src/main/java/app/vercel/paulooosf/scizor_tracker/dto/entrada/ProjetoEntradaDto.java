package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjetoEntradaDto(
    @NotBlank(message = "Preencha o nome!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres!")
    String nome,

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres!")
    String descricao
) {
    public ProjetoEntradaDto(Projeto projeto) {
        this(projeto.getNome(), projeto.getDescricao());
    }

    public Projeto converter() {
        return new Projeto(nome, descricao);
    }
}

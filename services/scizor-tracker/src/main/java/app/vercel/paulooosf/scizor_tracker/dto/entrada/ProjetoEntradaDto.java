package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de entrada para criação ou atualização de um projeto")
public record ProjetoEntradaDto(
    @Schema(description = "Nome do projeto", example = "Sistema de Gestão de Estoque", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha o nome!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres!")
    String nome,

    @Schema(description = "Descrição do projeto", example = "Sistema para controle de entrada e saída de produtos no estoque", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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

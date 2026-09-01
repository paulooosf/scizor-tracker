package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de entrada para criação ou atualização de um comentário")
public record ComentarioEntradaDto(
    @Schema(description = "Texto do comentário", example = "O erro foi corrigido aplicando validação no campo CPF", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha o texto!")
    @Size(min = 1, max = 5000, message = "Texto deve ter entre 1 e 5000 caracteres!")
    String texto
) {

    public Comentario converter() {
        Comentario comentario = new Comentario();
        comentario.setTexto(texto);
        return comentario;
    }
}

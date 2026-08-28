package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados de saída de um comentário")
public record ComentarioSaidaDto(
    @Schema(description = "ID único do comentário", example = "1")
    @NotNull(message = "ID não preenchido!")
    Long id,

    @Schema(description = "Texto do comentário", example = "O erro foi corrigido aplicando validação no campo CPF")
    @NotBlank(message = "Texto não preenchido!")
    @Size(max = 5000, message = "Texto deve ter no máximo 5000 caracteres!")
    String texto,

    @Schema(description = "ID do bug ao qual o comentário pertence", example = "1")
    @NotNull(message = "ID do bug não preenchido!")
    Long bugId,

    @Schema(description = "Usuário autor do comentário")
    @NotNull(message = "Usuário não preenchido!")
    UsuarioSaidaDto usuario,

    @Schema(description = "Data e hora do comentário", example = "2024-03-15T15:30:00")
    @NotNull(message = "Data do comentário não preenchida!")
    LocalDateTime dataComentario
) {
    public ComentarioSaidaDto(Comentario comentario) {
        this(
            comentario.getId(),
            comentario.getTexto(),
            comentario.getBug().getId(),
            new UsuarioSaidaDto(comentario.getUsuario()),
            comentario.getDataComentario()
        );
    }
}

package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ComentarioSaidaDto(
    @NotNull(message = "ID não preenchido!")
    Long id,

    @NotBlank(message = "Texto não preenchido!")
    @Size(max = 5000, message = "Texto deve ter no máximo 5000 caracteres!")
    String texto,

    @NotNull(message = "ID do bug não preenchido!")
    Long bugId,

    @NotNull(message = "Usuário não preenchido!")
    UsuarioSaidaDto usuario,

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

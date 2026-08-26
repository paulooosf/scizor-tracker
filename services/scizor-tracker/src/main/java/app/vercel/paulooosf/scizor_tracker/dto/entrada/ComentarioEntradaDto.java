package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioEntradaDto(
    @NotBlank(message = "Preencha o texto!")
    @Size(min = 1, max = 5000, message = "Texto deve ter entre 1 e 5000 caracteres!")
    String texto
) {
    public ComentarioEntradaDto(Comentario comentario) {
        this(comentario.getTexto());
    }

    public Comentario converter() {
        Comentario comentario = new Comentario();
        comentario.setTexto(texto);
        return comentario;
    }
}

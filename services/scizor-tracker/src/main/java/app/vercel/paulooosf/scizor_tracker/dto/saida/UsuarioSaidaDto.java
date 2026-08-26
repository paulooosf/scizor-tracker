package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UsuarioSaidaDto(
    @NotNull(message = "ID não preenchido!")
    Long id,

    @NotBlank(message = "Nome não preenchido!")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres!")
    String nome,

    @NotBlank(message = "Email não preenchido!")
    @Email(message = "Email inválido!")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres!")
    String email,

    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao
) {
    public UsuarioSaidaDto(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataCriacao());
    }
}

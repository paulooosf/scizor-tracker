package app.vercel.paulooosf.scizor_tracker.dto.saida;

import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados de saída de um usuário (senha não é exposta)")
public record UsuarioSaidaDto(
    @Schema(description = "ID único do usuário", example = "1")
    @NotNull(message = "ID não preenchido!")
    Long id,

    @Schema(description = "Nome completo do usuário", example = "João da Silva")
    @NotBlank(message = "Nome não preenchido!")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres!")
    String nome,

    @Schema(description = "Email do usuário", example = "joao.silva@example.com")
    @NotBlank(message = "Email não preenchido!")
    @Email(message = "Email inválido!")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres!")
    String email,

    @Schema(description = "Data e hora de criação do usuário", example = "2024-02-20T08:00:00")
    @NotNull(message = "Data de criação não preenchida!")
    LocalDateTime dataCriacao
) {
    public UsuarioSaidaDto(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataCriacao());
    }
}

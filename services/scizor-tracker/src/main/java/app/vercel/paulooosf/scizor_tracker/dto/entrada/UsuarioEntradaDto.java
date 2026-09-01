package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de entrada para criação ou atualização de um usuário")
public record UsuarioEntradaDto(
    @Schema(description = "Nome completo do usuário", example = "João da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha o nome!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres!")
    String nome,

    @Schema(description = "Email do usuário (será usado para login)", example = "joao.silva@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha o email!")
    @Email(message = "Email inválido!")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres!")
    String email,

    @Schema(description = "Senha do usuário (será criptografada automaticamente com BCrypt)", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Preencha a senha!")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres!")
    String senha
) {

    public Usuario converter() {
        return new Usuario(nome, email, senha);
    }
}

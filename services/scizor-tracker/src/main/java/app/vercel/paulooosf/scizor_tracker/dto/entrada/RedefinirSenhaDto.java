package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para redefinição de senha usando token")
public record RedefinirSenhaDto(
    @Schema(description = "Token de redefinição de senha enviado por email (válido por 15 minutos)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Token é obrigatório")
    String token,
    
    @Schema(description = "Nova senha do usuário (será criptografada com BCrypt)", example = "novaSenha123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    String senha
) {}

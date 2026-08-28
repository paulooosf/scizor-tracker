package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de autenticação do usuário")
public record LoginDto(
    @Schema(description = "Email do usuário", example = "admin@scizor.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,
    
    @Schema(description = "Senha do usuário", example = "admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Senha é obrigatória")
    String senha
) {}

package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para solicitar redefinição de senha")
public record SolicitarRedefinicaoSenhaDto(
    @Schema(description = "Email do usuário que esqueceu a senha", example = "joao.silva@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email
) {}

package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualização de status de um bug")
public record AtualizarStatusDto(
    @Schema(description = "Novo status do bug", example = "EM_PROGRESSO", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ABERTO", "EM_PROGRESSO", "RESOLVIDO", "FECHADO"})
    @NotNull(message = "Preencha o status!")
    StatusBug status
) {
}

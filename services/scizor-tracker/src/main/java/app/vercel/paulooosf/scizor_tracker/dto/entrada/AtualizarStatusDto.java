package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusDto(
    @NotNull(message = "Preencha o status!")
    StatusBug status
) {
}

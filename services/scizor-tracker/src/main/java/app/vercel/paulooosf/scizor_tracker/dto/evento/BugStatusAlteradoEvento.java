package app.vercel.paulooosf.scizor_tracker.dto.evento;

import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;

import java.time.LocalDateTime;

public record BugStatusAlteradoEvento(
    Long bugId,
    StatusBug statusAnterior,
    StatusBug statusNovo,
    String responsavelEmail,
    Long projetoId,
    LocalDateTime dataAlteracao
) {
}

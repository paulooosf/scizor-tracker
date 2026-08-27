package app.vercel.paulooosf.scizor_tracker.dto.evento;

import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;

import java.time.LocalDateTime;

public record BugCriadoEvento(
    Long bugId,
    String titulo,
    Prioridade prioridade,
    Long projetoId,
    String projetoNome,
    String criadoPor,
    LocalDateTime dataCriacao
) {
}

package app.vercel.paulooosf.scizor_tracker.dto.evento;

public record ComentarioAdicionadoEvento(
    Long bugId,
    Long comentarioId,
    String texto,
    String autorEmail,
    String responsavelBugEmail
) {
}

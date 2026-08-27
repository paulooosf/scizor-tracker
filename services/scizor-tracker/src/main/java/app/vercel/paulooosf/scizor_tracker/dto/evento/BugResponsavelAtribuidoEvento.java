package app.vercel.paulooosf.scizor_tracker.dto.evento;

public record BugResponsavelAtribuidoEvento(
    Long bugId,
    String titulo,
    Long responsavelId,
    String responsavelEmail,
    String atribuidoPor
) {
}

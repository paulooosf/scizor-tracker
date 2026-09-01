package app.vercel.paulooosf.scizor_tracker.dto.evento;

import java.time.LocalDateTime;

public record SenhaRedefinicaoSolicitadaEvento(
    String email,
    String token,
    LocalDateTime dataSolicitacao
) {
}

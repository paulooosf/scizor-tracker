package app.vercel.paulooosf.scizor_tracker.dto.saida;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação contendo o token JWT")
public record LoginRespostaDto(
    @Schema(description = "Token JWT para autenticação (válido por 2 horas)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBzY2l6b3IuY29tIiwiaWF0IjoxNzA5NTU2MDAwLCJleHAiOjE3MDk1NjMyMDB9.abcd1234")
    String token
) {}

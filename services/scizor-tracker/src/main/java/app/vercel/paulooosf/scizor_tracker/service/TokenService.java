package app.vercel.paulooosf.scizor_tracker.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String chave;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(chave);
            return JWT.create()
                    .withIssuer("ScizorTracker")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token: " + exception.getMessage());
        }
    }

    public String gerarTokenSenha(String email) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(chave);
            return JWT.create()
                    .withIssuer("ScizorTracker-redefinir-senha")
                    .withSubject(email)
                    .withExpiresAt(gerarDataExpiracaoCurta())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token de senha: " + exception.getMessage());
        }
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(chave);
            return JWT.require(algorithm)
                    .withIssuer("ScizorTracker")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String validarTokenSenha(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(chave);
            return JWT.require(algorithm)
                    .withIssuer("ScizorTracker-redefinir-senha")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token de redefinição de senha inválido ou expirado");
        }
    }

    private Instant gerarDataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    private Instant gerarDataExpiracaoCurta() {
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00"));
    }
}

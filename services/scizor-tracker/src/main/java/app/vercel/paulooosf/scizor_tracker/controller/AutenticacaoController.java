package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.LoginDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.RedefinirSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.SolicitarRedefinicaoSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.LoginRespostaDto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.service.SenhaService;
import app.vercel.paulooosf.scizor_tracker.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/autenticar")
@Tag(name = "Autenticação", description = "Endpoints públicos de autenticação e recuperação de senha (não requerem JWT)")
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final SenhaService senhaService;

    public AutenticacaoController(AuthenticationManager authenticationManager, 
                                   TokenService tokenService, 
                                   SenhaService senhaService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.senhaService = senhaService;
    }

    @Operation(
        summary = "Autenticar usuário", 
        description = "**Endpoint público** - Autentica o usuário com email e senha, retornando um token JWT válido por 2 horas. " +
                     "Credenciais de teste: admin@scizor.com/admin123 (ADMIN) ou joao.silva@example.com/senha123 (USER)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso, token JWT retornado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (validação falhou)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Email e/ou senha incorretos", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDto> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciais de autenticação (email e senha)",
            required = true
        )
        @Valid @RequestBody LoginDto dto
    ) {
        try {
            UsernamePasswordAuthenticationToken usuarioSenha = 
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
            
            Authentication autenticacao = authenticationManager.authenticate(usuarioSenha);
            String token = tokenService.gerarToken((Usuario) autenticacao.getPrincipal());

            return ResponseEntity.ok(new LoginRespostaDto(token));
        } catch (AuthenticationException e) {
            throw new RuntimeException("Email e/ou senha incorretos");
        }
    }

    @Operation(
        summary = "Solicitar redefinição de senha", 
        description = "**Endpoint público** - Gera um token de redefinição de senha válido por 15 minutos. " +
                     "Em produção, o token seria enviado por email. Em desenvolvimento, retorna o token na resposta."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso (retornado apenas em desenvolvimento)"),
            @ApiResponse(responseCode = "400", description = "Email inválido (validação falhou)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado com este email", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor", content = @Content)
    })
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> solicitarRedefinicaoSenha(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email do usuário que esqueceu a senha",
            required = true
        )
        @Valid @RequestBody SolicitarRedefinicaoSenhaDto dto
    ) {
        String token = senhaService.solicitarRedefinicaoSenha(dto);

        return ResponseEntity.ok(Map.of("token", token, 
            "mensagem", "Token gerado. Em produção será enviado por email."));
    }

    @Operation(
        summary = "Redefinir senha", 
        description = "**Endpoint público** - Redefine a senha do usuário usando o token gerado em /esqueci-senha. " +
                     "O token tem validade de 15 minutos. A nova senha será automaticamente criptografada com BCrypt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso, sem conteúdo no corpo da resposta"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado ou dados de validação incorretos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor", content = @Content)
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token de redefinição e nova senha",
            required = true
        )
        @Valid @RequestBody RedefinirSenhaDto dto
    ) {
        senhaService.redefinirSenha(dto);
        return ResponseEntity.noContent().build();
    }
}

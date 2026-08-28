package app.vercel.paulooosf.scizor_tracker.controller;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.LoginDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.RedefinirSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.SolicitarRedefinicaoSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.saida.LoginRespostaDto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.service.SenhaService;
import app.vercel.paulooosf.scizor_tracker.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Autenticação", description = "Endpoints relacionados a autenticação e recuperação de senha")
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

    @Operation(summary = "Autenticar usuário", description = "Autentica o usuário e retorna um token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginRespostaDto> login(@Valid @RequestBody LoginDto dto) {
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

    @Operation(summary = "Solicitar redefinição de senha", 
               description = "Gera um token de redefinição de senha e envia por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token gerado com sucesso (apenas desenvolvimento)"),
            @ApiResponse(responseCode = "400", description = "Email inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> solicitarRedefinicaoSenha(
            @Valid @RequestBody SolicitarRedefinicaoSenhaDto dto) {
        String token = senhaService.solicitarRedefinicaoSenha(dto);

        return ResponseEntity.ok(Map.of("token", token, 
            "mensagem", "Token gerado. Em produção será enviado por email."));
    }

    @Operation(summary = "Redefinir senha", 
               description = "Redefine a senha do usuário usando o token enviado por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor")
    })
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaDto dto) {
        senhaService.redefinirSenha(dto);
        return ResponseEntity.noContent().build();
    }
}

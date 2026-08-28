package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.RedefinirSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.SolicitarRedefinicaoSenhaDto;
import app.vercel.paulooosf.scizor_tracker.exception.UsuarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - SenhaService")
class SenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SenhaService senhaService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("João Silva", "joao@example.com", "senhaAntiga");
        usuario.setId(1L);
    }

    @Test
    @DisplayName("Deve solicitar redefinição de senha com sucesso")
    void deveSolicitarRedefinicaoSenhaComSucesso() {
        SolicitarRedefinicaoSenhaDto dto = new SolicitarRedefinicaoSenhaDto("joao@example.com");
        String tokenGerado = "token.jwt.gerado";

        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(tokenService.gerarTokenSenha("joao@example.com")).thenReturn(tokenGerado);

        String resultado = senhaService.solicitarRedefinicaoSenha(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado).isEqualTo(tokenGerado);
        verify(usuarioRepository).findByEmail("joao@example.com");
        verify(tokenService).gerarTokenSenha("joao@example.com");
    }

    @Test
    @DisplayName("Deve lançar exceção ao solicitar redefinição para email inexistente")
    void deveLancarExcecaoAoSolicitarRedefinicaoParaEmailInexistente() {
        SolicitarRedefinicaoSenhaDto dto = new SolicitarRedefinicaoSenhaDto("inexistente@example.com");
        when(usuarioRepository.findByEmail("inexistente@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaService.solicitarRedefinicaoSenha(dto))
            .isInstanceOf(UsuarioNaoEncontradoException.class)
            .hasMessageContaining("inexistente@example.com");
        
        verify(usuarioRepository).findByEmail("inexistente@example.com");
        verify(tokenService, never()).gerarTokenSenha(anyString());
    }

    @Test
    @DisplayName("Deve redefinir senha com token válido")
    void deveRedefinirSenhaComTokenValido() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("token.jwt.valido", "novaSenha123");
        
        when(tokenService.validarTokenSenha("token.jwt.valido")).thenReturn("joao@example.com");
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novaSenhaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        senhaService.redefinirSenha(dto);

        assertThat(usuario.getSenha()).isEqualTo("$2a$10$novaSenhaCriptografada");
        verify(tokenService).validarTokenSenha("token.jwt.valido");
        verify(usuarioRepository).findByEmail("joao@example.com");
        verify(passwordEncoder).encode("novaSenha123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha com token inválido")
    void deveLancarExcecaoAoRedefinirSenhaComTokenInvalido() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("token.jwt.invalido", "novaSenha123");
        
        when(tokenService.validarTokenSenha("token.jwt.invalido"))
            .thenThrow(new RuntimeException("Token de redefinição de senha inválido ou expirado"));

        assertThatThrownBy(() -> senhaService.redefinirSenha(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Token de redefinição de senha inválido ou expirado");
        
        verify(tokenService).validarTokenSenha("token.jwt.invalido");
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao redefinir senha se usuário não for encontrado")
    void deveLancarExcecaoAoRedefinirSenhaSeusuarioNaoEncontrado() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("token.jwt.valido", "novaSenha123");
        
        when(tokenService.validarTokenSenha("token.jwt.valido")).thenReturn("usuario@inexistente.com");
        when(usuarioRepository.findByEmail("usuario@inexistente.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> senhaService.redefinirSenha(dto))
            .isInstanceOf(UsuarioNaoEncontradoException.class)
            .hasMessageContaining("Usuário não encontrado");
        
        verify(tokenService).validarTokenSenha("token.jwt.valido");
        verify(usuarioRepository).findByEmail("usuario@inexistente.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve criptografar nova senha ao redefinir")
    void deveCriptografarNovaSenhaAoRedefinir() {
        RedefinirSenhaDto dto = new RedefinirSenhaDto("token.jwt.valido", "senhaNova456");
        String senhaOriginal = usuario.getSenha();
        
        when(tokenService.validarTokenSenha("token.jwt.valido")).thenReturn("joao@example.com");
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("senhaNova456")).thenReturn("$2a$10$senhaNovaCriptografada");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        senhaService.redefinirSenha(dto);

        assertThat(usuario.getSenha()).isNotEqualTo(senhaOriginal);
        assertThat(usuario.getSenha()).isEqualTo("$2a$10$senhaNovaCriptografada");
        verify(passwordEncoder).encode("senhaNova456");
    }

    @Test
    @DisplayName("Deve aceitar token com formato JWT válido")
    void deveAceitarTokenComFormatoJWTValido() {
        String tokenJWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIn0.signature";
        RedefinirSenhaDto dto = new RedefinirSenhaDto(tokenJWT, "novaSenha");
        
        when(tokenService.validarTokenSenha(tokenJWT)).thenReturn("joao@example.com");
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha")).thenReturn("$2a$10$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        senhaService.redefinirSenha(dto);

        verify(tokenService).validarTokenSenha(tokenJWT);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Deve gerar token diferente para cada solicitação")
    void deveGerarTokenDiferenteParaCadaSolicitacao() {
        SolicitarRedefinicaoSenhaDto dto1 = new SolicitarRedefinicaoSenhaDto("joao@example.com");
        SolicitarRedefinicaoSenhaDto dto2 = new SolicitarRedefinicaoSenhaDto("joao@example.com");
        
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));
        when(tokenService.gerarTokenSenha("joao@example.com"))
            .thenReturn("token.primeira.solicitacao")
            .thenReturn("token.segunda.solicitacao");

        String token1 = senhaService.solicitarRedefinicaoSenha(dto1);
        String token2 = senhaService.solicitarRedefinicaoSenha(dto2);

        assertThat(token1).isNotEqualTo(token2);
        verify(tokenService, times(2)).gerarTokenSenha("joao@example.com");
    }
}

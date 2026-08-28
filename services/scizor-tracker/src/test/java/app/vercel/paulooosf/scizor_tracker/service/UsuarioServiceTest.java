package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.exception.UsuarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("João Silva", "joao@example.com", "senha123");
        usuario.setId(1L);
        usuario.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve listar todos os usuários com paginação")
    void deveListarTodosUsuariosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> usuarioPage = new PageImpl<>(List.of(usuario));
        when(usuarioRepository.findAll(pageable)).thenReturn(usuarioPage);

        Page<Usuario> resultado = usuarioService.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("João Silva");
        verify(usuarioRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("João Silva");
        assertThat(resultado.getEmail()).isEqualTo("joao@example.com");
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por ID")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorId() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(999L))
            .isInstanceOf(UsuarioNaoEncontradoException.class)
            .hasMessageContaining("999");
        verify(usuarioRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void deveBuscarUsuarioPorEmailComSucesso() {
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorEmail("joao@example.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("joao@example.com");
        verify(usuarioRepository).findByEmail("joao@example.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por email")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoPorEmail() {
        when(usuarioRepository.findByEmail("invalido@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorEmail("invalido@example.com"))
            .isInstanceOf(UsuarioNaoEncontradoException.class)
            .hasMessageContaining("invalido@example.com");
        verify(usuarioRepository).findByEmail("invalido@example.com");
    }

    @Test
    @DisplayName("Deve criar usuário com senha criptografada")
    void deveCriarUsuarioComSenhaCriptografada() {
        Usuario novoUsuario = new Usuario("Maria Santos", "maria@example.com", "senha123");
        when(usuarioRepository.existsByEmail("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$hashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioSalvo = invocation.getArgument(0);
            usuarioSalvo.setId(2L);
            usuarioSalvo.setDataCriacao(LocalDateTime.now());
            return usuarioSalvo;
        });

        Usuario resultado = usuarioService.criar(novoUsuario);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNome()).isEqualTo("Maria Santos");
        assertThat(resultado.getEmail()).isEqualTo("maria@example.com");
        assertThat(resultado.getSenha()).isEqualTo("$2a$10$hashedPassword");

        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(novoUsuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void deveLancarExcecaoAoCriarUsuarioComEmailDuplicado() {
        Usuario novoUsuario = new Usuario("Outro Usuario", "joao@example.com", "senha123");
        when(usuarioRepository.existsByEmail("joao@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(novoUsuario))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Já existe um usuário com o email joao@example.com");
        
        verify(usuarioRepository).existsByEmail("joao@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void deveAtualizarUsuarioComSucesso() {
        Usuario usuarioAtualizado = new Usuario("João Silva Atualizado", "joao@example.com", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(1L, usuarioAtualizado);

        assertThat(resultado.getNome()).isEqualTo("João Silva Atualizado");
        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).save(usuario);
        verify(passwordEncoder, never()).encode(anyString()); // Não deve criptografar se senha não fornecida
    }

    @Test
    @DisplayName("Deve atualizar usuário com nova senha criptografada")
    void deveAtualizarUsuarioComNovaSenhaCriptografada() {
        Usuario usuarioAtualizado = new Usuario("João Silva", "joao@example.com", "novaSenha456");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha456")).thenReturn("$2a$10$newHashedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(1L, usuarioAtualizado);

        assertThat(resultado.getSenha()).isEqualTo("$2a$10$newHashedPassword");
        verify(passwordEncoder).encode("novaSenha456");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Não deve atualizar senha se fornecida em branco")
    void naoDeveAtualizarSenhaSeForBranco() {
        Usuario usuarioAtualizado = new Usuario("João Silva", "joao@example.com", "   ");
        String senhaOriginal = usuario.getSenha();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(1L, usuarioAtualizado);

        assertThat(resultado.getSenha()).isEqualTo(senhaOriginal); // Senha não deve mudar
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve atualizar email do usuário se não estiver duplicado")
    void deveAtualizarEmailSeNaoDuplicado() {
        Usuario usuarioAtualizado = new Usuario("João Silva", "novo@example.com", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("novo@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(1L, usuarioAtualizado);

        assertThat(resultado.getEmail()).isEqualTo("novo@example.com");
        verify(usuarioRepository).existsByEmail("novo@example.com");
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com email duplicado")
    void deveLancarExcecaoAoAtualizarComEmailDuplicado() {
        Usuario usuarioAtualizado = new Usuario("João Silva", "duplicado@example.com", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("duplicado@example.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, usuarioAtualizado))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Já existe um usuário com o email duplicado@example.com");
        
        verify(usuarioRepository).existsByEmail("duplicado@example.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve verificar duplicação de email se email não mudou")
    void naoDeveVerificarDuplicacaoSeEmailNaoMudou() {
        Usuario usuarioAtualizado = new Usuario("João Silva Atualizado", "joao@example.com", null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.atualizar(1L, usuarioAtualizado);

        assertThat(resultado.getNome()).isEqualTo("João Silva Atualizado");
        assertThat(resultado.getEmail()).isEqualTo("joao@example.com");
        verify(usuarioRepository, never()).existsByEmail(anyString()); // Não deve verificar duplicação
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deveDeletarUsuarioComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).delete(usuario);

        usuarioService.deletar(1L);

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    @DisplayName("Deve verificar se usuário existe por email")
    void deveVerificarSeUsuarioExistePorEmail() {
        when(usuarioRepository.existsByEmail("joao@example.com")).thenReturn(true);

        boolean existe = usuarioService.existePorEmail("joao@example.com");

        assertThat(existe).isTrue();
        verify(usuarioRepository).existsByEmail("joao@example.com");
    }

    @Test
    @DisplayName("Deve retornar false quando usuário não existe por email")
    void deveRetornarFalseQuandoUsuarioNaoExiste() {
        when(usuarioRepository.existsByEmail("inexistente@example.com")).thenReturn(false);

        boolean existe = usuarioService.existePorEmail("inexistente@example.com");

        assertThat(existe).isFalse();
        verify(usuarioRepository).existsByEmail("inexistente@example.com");
    }
}

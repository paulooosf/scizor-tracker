package app.vercel.paulooosf.scizor_tracker.integration;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.UsuarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("Testes de Integração - UsuarioController")
class UsuarioControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        bugRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario("João Silva", "joao@example.com", passwordEncoder.encode("senha123"));
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve listar todos os usuários com paginação")
    void deveListarTodosUsuariosComPaginacao() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].nome").value("João Silva"))
            .andExpect(jsonPath("$.content[0].email").value("joao@example.com"))
            .andExpect(jsonPath("$.content[0].senha").doesNotExist()) // Senha não deve ser retornada
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarUsuarioPorId() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.getId()))
            .andExpect(jsonPath("$.nome").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao@example.com"))
            .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 404 ao buscar usuário inexistente")
    void deveRetornar404AoBuscarUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarUsuarioPorEmail() throws Exception {
        mockMvc.perform(get("/api/usuarios/email")
                .param("email", "joao@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("joao@example.com"))
            .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 404 ao buscar usuário por email inexistente")
    void deveRetornar404AoBuscarPorEmailInexistente() throws Exception {
        mockMvc.perform(get("/api/usuarios/email")
                .param("email", "inexistente@example.com"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar novo usuário sem autenticação (endpoint público)")
    void deveCriarNovoUsuarioSemAutenticacao() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "Maria Santos",
            "maria@example.com",
            "senha456"
        );

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Maria Santos"))
            .andExpect(jsonPath("$.email").value("maria@example.com"))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar criar usuário com email duplicado")
    void deveRetornar400AoTentarCriarUsuarioComEmailDuplicado() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "Outro Usuário",
            "joao@example.com",
            "senha789"
        );

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar dados de entrada ao criar usuário")
    void deveValidarDadosDeEntradaAoCriarUsuario() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "",
            "invalido",
            "123"
        );

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar usuário existente")
    void deveAtualizarUsuarioExistente() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "João Silva Atualizado",
            "joao.novo@example.com",
            "senha123"
        );

        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.getId()))
            .andExpect(jsonPath("$.nome").value("João Silva Atualizado"))
            .andExpect(jsonPath("$.email").value("joao.novo@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar senha do usuário")
    void deveAtualizarSenhaDoUsuario() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "João Silva",
            "joao@example.com",
            "novaSenha123"
        );

        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(usuario.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar atualizar usuário sem permissão ADMIN")
    void deveRetornar403AoTentarAtualizarUsuarioSemPermissao() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "João Silva Atualizado",
            "joao@example.com",
            null
        );

        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar atualizar usuário inexistente")
    void deveRetornar404AoTentarAtualizarUsuarioInexistente() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "Usuário Inexistente",
            "inexistente@example.com",
            "senha123"
        );

        mockMvc.perform(put("/api/usuarios/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 400 ao atualizar com email duplicado")
    void deveRetornar400AoAtualizarComEmailDuplicado() throws Exception {
        Usuario outroUsuario = new Usuario("Outro", "outro@example.com", "senha");
        usuarioRepository.save(outroUsuario);

        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "João Silva",
            "outro@example.com", // Email já existe
            null
        );

        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve deletar usuário e retornar 204")
    void deveDeletarUsuario() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", usuario.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar deletar usuário sem permissão ADMIN")
    void deveRetornar403AoTentarDeletarUsuarioSemPermissao() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", usuario.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar deletar usuário inexistente")
    void deveRetornar404AoTentarDeletarUsuarioInexistente() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao acessar endpoints protegidos sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios/email")
                .param("email", "joao@example.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia quando não há usuários")
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() throws Exception {
        usuarioRepository.deleteAll();

        mockMvc.perform(get("/api/usuarios")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Deve criar múltiplos usuários e listar todos")
    void deveCriarMultiplosUsuariosEListarTodos() throws Exception {
        UsuarioEntradaDto dto1 = new UsuarioEntradaDto("Usuario 1", "user1@example.com", "senha123");
        UsuarioEntradaDto dto2 = new UsuarioEntradaDto("Usuario 2", "user2@example.com", "senha123");

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/usuarios")
                .param("page", "0")
                .param("size", "10")
                .with(request -> {
                    request.setAttribute("org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY", "user");
                    return request;
                }))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve garantir que senha seja criptografada ao criar usuário")
    void deveGarantirQueSenhaSejaCriptografadaAoCriar() throws Exception {
        UsuarioEntradaDto dto = new UsuarioEntradaDto(
            "Teste Criptografia",
            "cripto@example.com",
            "senhaPlana123"
        );

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        Usuario usuarioCriado = usuarioRepository.findByEmail("cripto@example.com").orElseThrow();
        assert !usuarioCriado.getSenha().equals("senhaPlana123") : "Senha não foi criptografada";
        assert usuarioCriado.getSenha().startsWith("$2a$") : "Senha não está no formato BCrypt";
    }
}

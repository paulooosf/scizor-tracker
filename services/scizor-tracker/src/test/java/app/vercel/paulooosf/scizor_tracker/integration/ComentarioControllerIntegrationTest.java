package app.vercel.paulooosf.scizor_tracker.integration;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ComentarioEntradaDto;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
import app.vercel.paulooosf.scizor_tracker.repository.ComentarioRepository;
import app.vercel.paulooosf.scizor_tracker.repository.ProjetoRepository;
import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@DisplayName("Testes de Integração - ComentarioController")
class ComentarioControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Comentario comentario;
    private Bug bug;
    private Projeto projeto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        comentarioRepository.deleteAll();
        bugRepository.deleteAll();
        usuarioRepository.deleteAll();
        projetoRepository.deleteAll();

        projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto = projetoRepository.save(projeto);

        usuario = new Usuario("João Silva", "joao@example.com", "senha123");
        usuario = usuarioRepository.save(usuario);

        bug = new Bug(
            "Erro ao salvar venda",
            "Sistema lança NullPointerException",
            Prioridade.ALTA,
            projeto
        );
        bug = bugRepository.save(bug);

        comentario = new Comentario(
            "Este bug foi analisado e precisa de correção",
            bug,
            usuario
        );
        comentario = comentarioRepository.save(comentario);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve listar todos os comentários com paginação")
    void deveListarTodosComentariosComPaginacao() throws Exception {
        mockMvc.perform(get("/api/comentarios")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].texto").value("Este bug foi analisado e precisa de correção"))
            .andExpect(jsonPath("$.content[0].bugId").value(bug.getId()))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar comentário por ID")
    void deveBuscarComentarioPorId() throws Exception {
        mockMvc.perform(get("/api/comentarios/{id}", comentario.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(comentario.getId()))
            .andExpect(jsonPath("$.texto").value("Este bug foi analisado e precisa de correção"))
            .andExpect(jsonPath("$.bugId").value(bug.getId()))
            .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
            .andExpect(jsonPath("$.usuario.nome").value("João Silva"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 404 ao buscar comentário inexistente")
    void deveRetornar404AoBuscarComentarioInexistente() throws Exception {
        mockMvc.perform(get("/api/comentarios/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar comentários por bug")
    void deveBuscarComentariosPorBug() throws Exception {
        Comentario comentario2 = new Comentario(
            "Segundo comentário",
            bug,
            usuario
        );
        comentarioRepository.save(comentario2);

        mockMvc.perform(get("/api/comentarios/bug/{bugId}", bug.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[*].bugId", everyItem(is(bug.getId().intValue()))));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia ao buscar comentários de bug sem comentários")
    void deveRetornarListaVaziaAoBuscarComentariosDeBugSemComentarios() throws Exception {
        Bug bugSemComentarios = new Bug(
            "Bug sem comentários",
            "Descrição",
            Prioridade.BAIXA,
            projeto
        );
        bugSemComentarios = bugRepository.save(bugSemComentarios);

        mockMvc.perform(get("/api/comentarios/bug/{bugId}", bugSemComentarios.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar comentários por usuário")
    void deveBuscarComentariosPorUsuario() throws Exception {
        mockMvc.perform(get("/api/comentarios/usuario/{usuarioId}", usuario.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].usuario.id").value(usuario.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia ao buscar comentários de usuário sem comentários")
    void deveRetornarListaVaziaAoBuscarComentariosDeUsuarioSemComentarios() throws Exception {
        Usuario usuarioSemComentarios = new Usuario("Maria", "maria@example.com", "senha");
        usuarioSemComentarios = usuarioRepository.save(usuarioSemComentarios);

        mockMvc.perform(get("/api/comentarios/usuario/{usuarioId}", usuarioSemComentarios.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar novo comentário e retornar 201")
    void deveCriarNovoComentario() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Novo comentário de teste"
        );

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.texto").value("Novo comentário de teste"))
            .andExpect(jsonPath("$.bugId").value(bug.getId()))
            .andExpect(jsonPath("$.usuario.id").value(usuario.getId()))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.dataComentario").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar criar comentário sem permissão ADMIN")
    void deveRetornar403AoTentarCriarComentarioSemPermissao() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Comentário sem permissão"
        );

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve validar dados de entrada ao criar comentário")
    void deveValidarDadosDeEntradaAoCriarComentario() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            ""
        );

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao criar comentário com bug inexistente")
    void deveRetornar404AoCriarComentarioComBugInexistente() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Comentário para bug inexistente"
        );

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", "999")
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao criar comentário com usuário inexistente")
    void deveRetornar404AoCriarComentarioComUsuarioInexistente() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Comentário de usuário inexistente"
        );

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar comentário existente")
    void deveAtualizarComentarioExistente() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Texto atualizado do comentário"
        );

        mockMvc.perform(put("/api/comentarios/{id}", comentario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(comentario.getId()))
            .andExpect(jsonPath("$.texto").value("Texto atualizado do comentário"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve permitir atualizar comentário mesmo sem permissão ADMIN (endpoint não protegido)")
    void devePermitirAtualizarComentarioSemRestricao() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Atualização realizada por USER"
        );

        mockMvc.perform(put("/api/comentarios/{id}", comentario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.texto").value("Atualização realizada por USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar atualizar comentário inexistente")
    void deveRetornar404AoTentarAtualizarComentarioInexistente() throws Exception {
        ComentarioEntradaDto dto = new ComentarioEntradaDto(
            "Atualização de comentário inexistente"
        );

        mockMvc.perform(put("/api/comentarios/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve deletar comentário e retornar 204")
    void deveDeletarComentario() throws Exception {
        mockMvc.perform(delete("/api/comentarios/{id}", comentario.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/comentarios/{id}", comentario.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar deletar comentário sem permissão ADMIN")
    void deveRetornar403AoTentarDeletarComentarioSemPermissao() throws Exception {
        mockMvc.perform(delete("/api/comentarios/{id}", comentario.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar deletar comentário inexistente")
    void deveRetornar404AoTentarDeletarComentarioInexistente() throws Exception {
        mockMvc.perform(delete("/api/comentarios/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao acessar endpoint sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/comentarios"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia quando não há comentários")
    void deveRetornarListaVaziaQuandoNaoHaComentarios() throws Exception {
        comentarioRepository.deleteAll();

        mockMvc.perform(get("/api/comentarios")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar múltiplos comentários para o mesmo bug")
    void deveCriarMultiplosComentariosParaMesmoBug() throws Exception {
        ComentarioEntradaDto dto1 = new ComentarioEntradaDto("Comentário 1");
        ComentarioEntradaDto dto2 = new ComentarioEntradaDto("Comentário 2");

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/comentarios")
                .param("bugId", bug.getId().toString())
                .param("usuarioId", usuario.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/comentarios/bug/{bugId}", bug.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(3)));
    }
}

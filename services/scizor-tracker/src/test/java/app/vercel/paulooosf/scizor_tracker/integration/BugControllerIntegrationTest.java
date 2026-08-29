package app.vercel.paulooosf.scizor_tracker.integration;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.AtualizarStatusDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.BugEntradaDto;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
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
@DisplayName("Testes de Integração - BugController")
class BugControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BugRepository bugRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Projeto projeto;
    private Usuario usuario;
    private Bug bug;

    @BeforeEach
    void setUp() {
        bugRepository.deleteAll();
        usuarioRepository.deleteAll();
        projetoRepository.deleteAll();

        projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto = projetoRepository.save(projeto);

        usuario = new Usuario("João Silva", "joao@example.com", "$2a$10$hashedPassword");
        usuario = usuarioRepository.save(usuario);

        bug = new Bug(
            "Erro ao salvar venda",
            "Sistema lança NullPointerException ao salvar",
            Prioridade.ALTA,
            projeto
        );
        bug = bugRepository.save(bug);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve listar todos os bugs com paginação")
    void deveListarTodosBugsComPaginacao() throws Exception {
        mockMvc.perform(get("/api/bugs")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].titulo").value("Erro ao salvar venda"))
            .andExpect(jsonPath("$.content[0].prioridade").value("ALTA"))
            .andExpect(jsonPath("$.content[0].status").value("ABERTO"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bug por ID")
    void deveBuscarBugPorId() throws Exception {
        mockMvc.perform(get("/api/bugs/{id}", bug.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bug.getId()))
            .andExpect(jsonPath("$.titulo").value("Erro ao salvar venda"))
            .andExpect(jsonPath("$.descricao").value("Sistema lança NullPointerException ao salvar"))
            .andExpect(jsonPath("$.prioridade").value("ALTA"))
            .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 404 ao buscar bug inexistente")
    void deveRetornar404AoBuscarBugInexistente() throws Exception {
        mockMvc.perform(get("/api/bugs/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs por projeto")
    void deveBuscarBugsPorProjeto() throws Exception {
        mockMvc.perform(get("/api/bugs/projeto/{projetoId}", projeto.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].projetoId").value(projeto.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs por status")
    void deveBuscarBugsPorStatus() throws Exception {
        mockMvc.perform(get("/api/bugs/status/{status}", "ABERTO")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].status").value("ABERTO"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs por prioridade")
    void deveBuscarBugsPorPrioridade() throws Exception {
        mockMvc.perform(get("/api/bugs/prioridade/{prioridade}", "ALTA")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].prioridade").value("ALTA"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs sem responsável")
    void deveBuscarBugsSemResponsavel() throws Exception {
        mockMvc.perform(get("/api/bugs/sem-responsavel")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].usuarioResponsavel").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs por termo")
    void deveBuscarBugsPorTermo() throws Exception {
        mockMvc.perform(get("/api/bugs/buscar")
                .param("termo", "salvar")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].titulo").value(containsString("salvar")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar novo bug e retornar 201")
    void deveCriarNovoBug() throws Exception {
        BugEntradaDto dto = new BugEntradaDto(
            "Bug novo",
            "Descrição do bug novo",
            Prioridade.MEDIA
        );

        mockMvc.perform(post("/api/bugs")
                .param("projetoId", projeto.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Bug novo"))
            .andExpect(jsonPath("$.descricao").value("Descrição do bug novo"))
            .andExpect(jsonPath("$.prioridade").value("MEDIA"))
            .andExpect(jsonPath("$.status").value("ABERTO"))
            .andExpect(jsonPath("$.projetoId").value(projeto.getId()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar criar bug sem permissão ADMIN")
    void deveRetornar403AoTentarCriarBugSemPermissao() throws Exception {
        BugEntradaDto dto = new BugEntradaDto(
            "Bug novo",
            "Descrição do bug novo",
            Prioridade.MEDIA
        );

        mockMvc.perform(post("/api/bugs")
                .param("projetoId", projeto.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar bug existente")
    void deveAtualizarBugExistente() throws Exception {
        BugEntradaDto dto = new BugEntradaDto(
            "Título atualizado",
            "Descrição atualizada",
            Prioridade.CRITICA
        );

        mockMvc.perform(put("/api/bugs/{id}", bug.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Título atualizado"))
            .andExpect(jsonPath("$.descricao").value("Descrição atualizada"))
            .andExpect(jsonPath("$.prioridade").value("CRITICA"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar status do bug")
    void deveAtualizarStatusDoBug() throws Exception {
        AtualizarStatusDto dto = new AtualizarStatusDto(StatusBug.EM_ANDAMENTO);

        mockMvc.perform(patch("/api/bugs/{id}/status", bug.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atribuir responsável ao bug")
    void deveAtribuirResponsavelAoBug() throws Exception {
        mockMvc.perform(patch("/api/bugs/{bugId}/responsavel/{usuarioId}", 
                bug.getId(), usuario.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuarioResponsavel").exists())
            .andExpect(jsonPath("$.usuarioResponsavel.id").value(usuario.getId()))
            .andExpect(jsonPath("$.usuarioResponsavel.nome").value("João Silva"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve remover responsável do bug")
    void deveRemoverResponsavelDoBug() throws Exception {
        bug.setUsuarioResponsavel(usuario);
        bugRepository.save(bug);

        mockMvc.perform(delete("/api/bugs/{bugId}/responsavel", bug.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuarioResponsavel").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve deletar bug e retornar 204")
    void deveDeletarBug() throws Exception {
        mockMvc.perform(delete("/api/bugs/{id}", bug.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bugs/{id}", bug.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao acessar endpoint sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/bugs"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve validar dados de entrada ao criar bug")
    void deveValidarDadosDeEntradaAoCriarBug() throws Exception {
        BugEntradaDto dto = new BugEntradaDto(
            "",
            "Descrição",
            Prioridade.MEDIA
        );

        mockMvc.perform(post("/api/bugs")
                .param("projetoId", projeto.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar bugs por responsável")
    void deveBuscarBugsPorResponsavel() throws Exception {
        bug.setUsuarioResponsavel(usuario);
        bugRepository.save(bug);

        mockMvc.perform(get("/api/bugs/responsavel/{usuarioId}", usuario.getId())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].usuarioResponsavel.id").value(usuario.getId()));
    }
}

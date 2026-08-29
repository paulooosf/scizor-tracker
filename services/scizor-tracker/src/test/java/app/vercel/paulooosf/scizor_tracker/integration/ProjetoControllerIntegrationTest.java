package app.vercel.paulooosf.scizor_tracker.integration;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.ProjetoEntradaDto;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
import app.vercel.paulooosf.scizor_tracker.repository.ProjetoRepository;
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
@DisplayName("Testes de Integração - ProjetoController")
class ProjetoControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private BugRepository bugRepository;

    private Projeto projeto;

    @BeforeEach
    void setUp() {
        bugRepository.deleteAll();
        projetoRepository.deleteAll();

        projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto = projetoRepository.save(projeto);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve listar todos os projetos com paginação")
    void deveListarTodosProjetosComPaginacao() throws Exception {
        mockMvc.perform(get("/api/projetos")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].nome").value("Sistema de Vendas"))
            .andExpect(jsonPath("$.content[0].descricao").value("Sistema para gestão de vendas"))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar projeto por ID")
    void deveBuscarProjetoPorId() throws Exception {
        mockMvc.perform(get("/api/projetos/{id}", projeto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(projeto.getId()))
            .andExpect(jsonPath("$.nome").value("Sistema de Vendas"))
            .andExpect(jsonPath("$.descricao").value("Sistema para gestão de vendas"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 404 ao buscar projeto inexistente")
    void deveRetornar404AoBuscarProjetoInexistente() throws Exception {
        mockMvc.perform(get("/api/projetos/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve buscar projetos por nome")
    void deveBuscarProjetosPorNome() throws Exception {
        projetoRepository.save(new Projeto("Sistema de Estoque", "Controle de estoque"));
        projetoRepository.save(new Projeto("Portal Web", "Portal institucional"));

        mockMvc.perform(get("/api/projetos/buscar")
                .param("nome", "Sistema")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[*].nome", 
                hasItems(containsString("Sistema"))));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia ao buscar projeto por nome inexistente")
    void deveRetornarListaVaziaAoBuscarNomeInexistente() throws Exception {
        mockMvc.perform(get("/api/projetos/buscar")
                .param("nome", "Inexistente")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar novo projeto e retornar 201")
    void deveCriarNovoProjeto() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "Novo Projeto",
            "Descrição do novo projeto"
        );

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Novo Projeto"))
            .andExpect(jsonPath("$.descricao").value("Descrição do novo projeto"))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar criar projeto sem permissão ADMIN")
    void deveRetornar403AoTentarCriarProjetoSemPermissao() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "Novo Projeto",
            "Descrição do novo projeto"
        );

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve validar dados de entrada ao criar projeto")
    void deveValidarDadosDeEntradaAoCriarProjeto() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "",
            "Descrição"
        );

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar projeto existente")
    void deveAtualizarProjetoExistente() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "Nome Atualizado",
            "Descrição Atualizada"
        );

        mockMvc.perform(put("/api/projetos/{id}", projeto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(projeto.getId()))
            .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
            .andExpect(jsonPath("$.descricao").value("Descrição Atualizada"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar atualizar projeto sem permissão ADMIN")
    void deveRetornar403AoTentarAtualizarProjetoSemPermissao() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "Nome Atualizado",
            "Descrição Atualizada"
        );

        mockMvc.perform(put("/api/projetos/{id}", projeto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar atualizar projeto inexistente")
    void deveRetornar404AoTentarAtualizarProjetoInexistente() throws Exception {
        ProjetoEntradaDto dto = new ProjetoEntradaDto(
            "Nome Atualizado",
            "Descrição Atualizada"
        );

        mockMvc.perform(put("/api/projetos/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve deletar projeto e retornar 204")
    void deveDeletarProjeto() throws Exception {
        mockMvc.perform(delete("/api/projetos/{id}", projeto.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projetos/{id}", projeto.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 ao tentar deletar projeto sem permissão ADMIN")
    void deveRetornar403AoTentarDeletarProjetoSemPermissao() throws Exception {
        mockMvc.perform(delete("/api/projetos/{id}", projeto.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 404 ao tentar deletar projeto inexistente")
    void deveRetornar404AoTentarDeletarProjetoInexistente() throws Exception {
        mockMvc.perform(delete("/api/projetos/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar 403 ao acessar endpoint sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/projetos"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar lista vazia quando não há projetos")
    void deveRetornarListaVaziaQuandoNaoHaProjetos() throws Exception {
        projetoRepository.deleteAll();

        mockMvc.perform(get("/api/projetos")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar múltiplos projetos e listar todos")
    void deveCriarMultiplosProjetosEListarTodos() throws Exception {
        ProjetoEntradaDto dto1 = new ProjetoEntradaDto("Projeto 1", "Descrição 1");
        ProjetoEntradaDto dto2 = new ProjetoEntradaDto("Projeto 2", "Descrição 2");

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projetos")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(3)))
            .andExpect(jsonPath("$.totalElements").value(3));
    }
}

package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.exception.ProjetoNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.repository.ProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - ProjetoService")
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private Projeto projeto;

    @BeforeEach
    void setUp() {
        projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto.setId(1L);
        projeto.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve listar todos os projetos com paginação")
    void deveListarTodosProjetosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projeto> projetoPage = new PageImpl<>(List.of(projeto));
        when(projetoRepository.findAll(pageable)).thenReturn(projetoPage);

        Page<Projeto> resultado = projetoService.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Sistema de Vendas");
        verify(projetoRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar projeto por ID com sucesso")
    void deveBuscarProjetoPorIdComSucesso() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        Projeto resultado = projetoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Sistema de Vendas");
        assertThat(resultado.getDescricao()).isEqualTo("Sistema para gestão de vendas");
        verify(projetoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando projeto não for encontrado")
    void deveLancarExcecaoQuandoProjetoNaoEncontrado() {
        when(projetoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetoService.buscarPorId(999L))
            .isInstanceOf(ProjetoNaoEncontradoException.class)
            .hasMessageContaining("999");
        verify(projetoRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar projeto por ID com bugs")
    void deveBuscarProjetoPorIdComBugs() {
        when(projetoRepository.findByIdComBugs(1L)).thenReturn(projeto);

        Projeto resultado = projetoService.buscarPorIdComBugs(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        verify(projetoRepository).findByIdComBugs(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando projeto com bugs não for encontrado")
    void deveLancarExcecaoQuandoProjetoComBugsNaoEncontrado() {
        when(projetoRepository.findByIdComBugs(999L)).thenReturn(null);

        assertThatThrownBy(() -> projetoService.buscarPorIdComBugs(999L))
            .isInstanceOf(ProjetoNaoEncontradoException.class)
            .hasMessageContaining("999");
        verify(projetoRepository).findByIdComBugs(999L);
    }

    @Test
    @DisplayName("Deve buscar projetos por nome")
    void deveBuscarProjetosPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Projeto> projetoPage = new PageImpl<>(List.of(projeto));
        when(projetoRepository.findByNomeContainingIgnoreCase("vendas", pageable)).thenReturn(projetoPage);

        Page<Projeto> resultado = projetoService.buscarPorNome("vendas", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).containsIgnoringCase("vendas");
        verify(projetoRepository).findByNomeContainingIgnoreCase("vendas", pageable);
    }

    @Test
    @DisplayName("Deve criar projeto com sucesso")
    void deveCriarProjetoComSucesso() {
        Projeto novoProjeto = new Projeto("Novo Projeto", "Descrição do projeto");
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocation -> {
            Projeto projetoSalvo = invocation.getArgument(0);
            projetoSalvo.setId(2L);
            projetoSalvo.setDataCriacao(LocalDateTime.now());
            return projetoSalvo;
        });

        Projeto resultado = projetoService.criar(novoProjeto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(2L);
        assertThat(resultado.getNome()).isEqualTo("Novo Projeto");
        assertThat(resultado.getDescricao()).isEqualTo("Descrição do projeto");
        assertThat(resultado.getDataCriacao()).isNotNull();
        verify(projetoRepository).save(novoProjeto);
    }

    @Test
    @DisplayName("Deve atualizar projeto com sucesso")
    void deveAtualizarProjetoComSucesso() {
        Projeto projetoAtualizado = new Projeto("Nome Atualizado", "Descrição Atualizada");
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoRepository.save(any(Projeto.class))).thenReturn(projeto);

        Projeto resultado = projetoService.atualizar(1L, projetoAtualizado);

        assertThat(resultado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(resultado.getDescricao()).isEqualTo("Descrição Atualizada");
        verify(projetoRepository).findById(1L);
        verify(projetoRepository).save(projeto);
    }

    @Test
    @DisplayName("Deve deletar projeto com sucesso")
    void deveDeletarProjetoComSucesso() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        doNothing().when(projetoRepository).delete(projeto);

        projetoService.deletar(1L);

        verify(projetoRepository).findById(1L);
        verify(projetoRepository).delete(projeto);
    }

    @Test
    @DisplayName("Deve contar bugs do projeto")
    void deveContarBugsDoProjeto() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoRepository.contarBugsPorProjeto(1L)).thenReturn(5L);

        Long quantidade = projetoService.contarBugs(1L);

        assertThat(quantidade).isEqualTo(5L);
        verify(projetoRepository).findById(1L);
        verify(projetoRepository).contarBugsPorProjeto(1L);
    }

    @Test
    @DisplayName("Deve retornar zero quando projeto não tem bugs")
    void deveRetornarZeroQuandoProjetoNaoTemBugs() {
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(projetoRepository.contarBugsPorProjeto(1L)).thenReturn(0L);

        Long quantidade = projetoService.contarBugs(1L);

        assertThat(quantidade).isZero();
        verify(projetoRepository).contarBugsPorProjeto(1L);
    }
}

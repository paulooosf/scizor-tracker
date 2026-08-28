package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugResponsavelAtribuidoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.exception.BugNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.exception.StatusInvalidoException;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.messaging.publicador.PublicadorEvento;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.BugRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - BugService")
class BugServiceTest {

    @Mock
    private BugRepository bugRepository;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PublicadorEvento publicadorEvento;

    @InjectMocks
    private BugService bugService;

    private Bug bug;
    private Projeto projeto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto.setId(1L);

        usuario = new Usuario("João Silva", "joao@example.com", "senha123");
        usuario.setId(1L);

        bug = new Bug();
        bug.setId(1L);
        bug.setTitulo("Erro ao salvar venda");
        bug.setDescricao("Sistema lança NullPointerException ao salvar");
        bug.setPrioridade(Prioridade.ALTA);
        bug.setStatus(StatusBug.ABERTO);
        bug.setProjeto(projeto);
        bug.setDataCriacao(LocalDateTime.now());
        bug.setDataAtualizacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve listar todos os bugs com paginação")
    void deveListarTodosBugsComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(bugRepository.findAllComRelacionamentos(pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitulo()).isEqualTo("Erro ao salvar venda");
        verify(bugRepository).findAllComRelacionamentos(pageable);
    }

    @Test
    @DisplayName("Deve buscar bug por ID com sucesso")
    void deveBuscarBugPorIdComSucesso() {
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));

        Bug resultado = bugService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTitulo()).isEqualTo("Erro ao salvar venda");
        verify(bugRepository).findByIdComRelacionamentos(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando bug não for encontrado")
    void deveLancarExcecaoQuandoBugNaoEncontrado() {
        when(bugRepository.findByIdComRelacionamentos(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bugService.buscarPorId(999L))
            .isInstanceOf(BugNaoEncontradoException.class)
            .hasMessageContaining("999");
        verify(bugRepository).findByIdComRelacionamentos(999L);
    }

    @Test
    @DisplayName("Deve criar bug com sucesso e publicar evento")
    void deveCriarBugComSucessoEPublicarEvento() {
        Bug novoBug = new Bug();
        novoBug.setTitulo("Novo bug");
        novoBug.setDescricao("Descrição do bug");
        novoBug.setPrioridade(Prioridade.MEDIA);

        when(projetoService.buscarPorId(1L)).thenReturn(projeto);
        when(bugRepository.save(any(Bug.class))).thenAnswer(invocation -> {
            Bug bugSalvo = invocation.getArgument(0);
            bugSalvo.setId(2L);
            return bugSalvo;
        });

        Bug resultado = bugService.criar(novoBug, 1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getProjeto()).isEqualTo(projeto);
        assertThat(resultado.getStatus()).isEqualTo(StatusBug.ABERTO);
        assertThat(resultado.getDataCriacao()).isNotNull();
        assertThat(resultado.getDataAtualizacao()).isNotNull();

        ArgumentCaptor<BugCriadoEvento> eventoCaptor = ArgumentCaptor.forClass(BugCriadoEvento.class);
        verify(publicadorEvento).publicar(
            eq(TopicosKafka.BUG_CRIADO),
            eq("2"),
            eventoCaptor.capture()
        );

        BugCriadoEvento evento = eventoCaptor.getValue();
        assertThat(evento.bugId()).isEqualTo(2L);
        assertThat(evento.titulo()).isEqualTo("Novo bug");
        assertThat(evento.prioridade()).isEqualTo(Prioridade.MEDIA);
        assertThat(evento.projetoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve atualizar bug com sucesso")
    void deveAtualizarBugComSucesso() {
        Bug bugAtualizado = new Bug();
        bugAtualizado.setTitulo("Título atualizado");
        bugAtualizado.setDescricao("Descrição atualizada");
        bugAtualizado.setPrioridade(Prioridade.CRITICA);

        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);

        Bug resultado = bugService.atualizar(1L, bugAtualizado);

        assertThat(resultado.getTitulo()).isEqualTo("Título atualizado");
        assertThat(resultado.getDescricao()).isEqualTo("Descrição atualizada");
        assertThat(resultado.getPrioridade()).isEqualTo(Prioridade.CRITICA);
        verify(bugRepository).save(bug);
    }

    @Test
    @DisplayName("Deve atualizar status do bug e publicar evento")
    void deveAtualizarStatusEPublicarEvento() {
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);

        Bug resultado = bugService.atualizarStatus(1L, StatusBug.EM_ANDAMENTO);

        assertThat(resultado.getStatus()).isEqualTo(StatusBug.EM_ANDAMENTO);

        ArgumentCaptor<BugStatusAlteradoEvento> eventoCaptor = ArgumentCaptor.forClass(BugStatusAlteradoEvento.class);
        verify(publicadorEvento).publicar(
            eq(TopicosKafka.BUG_STATUS_ALTERADO),
            eq("1"),
            eventoCaptor.capture()
        );

        BugStatusAlteradoEvento evento = eventoCaptor.getValue();
        assertThat(evento.bugId()).isEqualTo(1L);
        assertThat(evento.statusAnterior()).isEqualTo(StatusBug.ABERTO);
        assertThat(evento.statusNovo()).isEqualTo(StatusBug.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar bug fechado para status diferente de REABERTO")
    void deveLancarExcecaoAoAlterarBugFechadoParaStatusInvalido() {
        bug.setStatus(StatusBug.FECHADO);
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));

        assertThatThrownBy(() -> bugService.atualizarStatus(1L, StatusBug.EM_ANDAMENTO))
            .isInstanceOf(StatusInvalidoException.class)
            .hasMessageContaining("fechado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar reabrir bug que já está aberto")
    void deveLancarExcecaoAoReabrirBugAberto() {
        bug.setStatus(StatusBug.ABERTO);
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));

        assertThatThrownBy(() -> bugService.atualizarStatus(1L, StatusBug.REABERTO))
            .isInstanceOf(StatusInvalidoException.class)
            .hasMessageContaining("aberto");
    }

    @Test
    @DisplayName("Deve atribuir responsável ao bug e publicar evento")
    void deveAtribuirResponsavelEPublicarEvento() {
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);

        Bug resultado = bugService.atribuirResponsavel(1L, 1L);

        assertThat(resultado.getUsuarioResponsavel()).isEqualTo(usuario);

        ArgumentCaptor<BugResponsavelAtribuidoEvento> eventoCaptor = ArgumentCaptor.forClass(BugResponsavelAtribuidoEvento.class);
        verify(publicadorEvento).publicar(
            eq(TopicosKafka.BUG_RESPONSAVEL_ATRIBUIDO),
            eq("1"),
            eventoCaptor.capture()
        );

        BugResponsavelAtribuidoEvento evento = eventoCaptor.getValue();
        assertThat(evento.bugId()).isEqualTo(1L);
        assertThat(evento.responsavelId()).isEqualTo(1L);
        assertThat(evento.responsavelEmail()).isEqualTo("joao@example.com");
    }

    @Test
    @DisplayName("Deve remover responsável do bug")
    void deveRemoverResponsavel() {
        bug.setUsuarioResponsavel(usuario);
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);

        Bug resultado = bugService.removerResponsavel(1L);

        assertThat(resultado.getUsuarioResponsavel()).isNull();
        verify(bugRepository).save(bug);
    }

    @Test
    @DisplayName("Deve deletar bug com sucesso")
    void deveDeletarBugComSucesso() {
        when(bugRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(bug));
        doNothing().when(bugRepository).delete(bug);

        bugService.deletar(1L);

        verify(bugRepository).delete(bug);
    }

    @Test
    @DisplayName("Deve buscar bugs por projeto")
    void deveBuscarBugsPorProjeto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(projetoService.buscarPorId(1L)).thenReturn(projeto);
        when(bugRepository.findByProjetoId(1L, pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.buscarPorProjeto(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getProjeto()).isEqualTo(projeto);
    }

    @Test
    @DisplayName("Deve buscar bugs por status")
    void deveBuscarBugsPorStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(bugRepository.findByStatus(StatusBug.ABERTO, pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.buscarPorStatus(StatusBug.ABERTO, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getStatus()).isEqualTo(StatusBug.ABERTO);
    }

    @Test
    @DisplayName("Deve buscar bugs por prioridade")
    void deveBuscarBugsPorPrioridade() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(bugRepository.findByPrioridade(Prioridade.ALTA, pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.buscarPorPrioridade(Prioridade.ALTA, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getPrioridade()).isEqualTo(Prioridade.ALTA);
    }

    @Test
    @DisplayName("Deve buscar bugs sem responsável")
    void deveBuscarBugsSemResponsavel() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(bugRepository.findByUsuarioResponsavelIsNull(pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.buscarSemResponsavel(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(bugRepository).findByUsuarioResponsavelIsNull(pageable);
    }

    @Test
    @DisplayName("Deve buscar bugs por termo de busca")
    void deveBuscarBugsPorTermo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Bug> bugPage = new PageImpl<>(List.of(bug));
        when(bugRepository.buscarPorTermo("salvar", pageable)).thenReturn(bugPage);

        Page<Bug> resultado = bugService.buscarPorTermo("salvar", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTitulo()).contains("salvar");
    }
}

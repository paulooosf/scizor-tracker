package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.dto.evento.ComentarioAdicionadoEvento;
import app.vercel.paulooosf.scizor_tracker.exception.ComentarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.messaging.publicador.PublicadorEvento;
import app.vercel.paulooosf.scizor_tracker.model.Bug;
import app.vercel.paulooosf.scizor_tracker.model.Comentario;
import app.vercel.paulooosf.scizor_tracker.model.Projeto;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.ComentarioRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários - ComentarioService")
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private BugService bugService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PublicadorEvento publicadorEvento;

    @InjectMocks
    private ComentarioService comentarioService;

    private Comentario comentario;
    private Bug bug;
    private Usuario usuario;
    private Usuario responsavel;

    @BeforeEach
    void setUp() {
        Projeto projeto = new Projeto("Sistema de Vendas", "Sistema para gestão de vendas");
        projeto.setId(1L);

        bug = new Bug();
        bug.setId(1L);
        bug.setTitulo("Erro ao salvar venda");
        bug.setProjeto(projeto);

        usuario = new Usuario("João Silva", "joao@example.com", "senha123");
        usuario.setId(1L);

        responsavel = new Usuario("Maria Santos", "maria@example.com", "senha456");
        responsavel.setId(2L);

        comentario = new Comentario();
        comentario.setId(1L);
        comentario.setTexto("Este bug foi corrigido na versão 2.0");
        comentario.setBug(bug);
        comentario.setUsuario(usuario);
        comentario.setDataComentario(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve listar todos os comentários com paginação")
    void deveListarTodosComentariosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comentario> comentarioPage = new PageImpl<>(List.of(comentario));
        when(comentarioRepository.findAllComRelacionamentos(pageable)).thenReturn(comentarioPage);

        Page<Comentario> resultado = comentarioService.listarTodos(pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getTexto()).contains("corrigido");
        verify(comentarioRepository).findAllComRelacionamentos(pageable);
    }

    @Test
    @DisplayName("Deve buscar comentário por ID com sucesso")
    void deveBuscarComentarioPorIdComSucesso() {
        when(comentarioRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(comentario));

        Comentario resultado = comentarioService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTexto()).isEqualTo("Este bug foi corrigido na versão 2.0");
        verify(comentarioRepository).findByIdComRelacionamentos(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando comentário não for encontrado")
    void deveLancarExcecaoQuandoComentarioNaoEncontrado() {
        when(comentarioRepository.findByIdComRelacionamentos(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comentarioService.buscarPorId(999L))
            .isInstanceOf(ComentarioNaoEncontradoException.class)
            .hasMessageContaining("999");
        verify(comentarioRepository).findByIdComRelacionamentos(999L);
    }

    @Test
    @DisplayName("Deve buscar comentários por bug")
    void deveBuscarComentariosPorBug() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comentario> comentarioPage = new PageImpl<>(List.of(comentario));
        when(bugService.buscarPorId(1L)).thenReturn(bug);
        when(comentarioRepository.findByBugId(1L, pageable)).thenReturn(comentarioPage);

        Page<Comentario> resultado = comentarioService.buscarPorBug(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getBug()).isEqualTo(bug);
        verify(bugService).buscarPorId(1L);
        verify(comentarioRepository).findByBugId(1L, pageable);
    }

    @Test
    @DisplayName("Deve buscar comentários por usuário")
    void deveBuscarComentariosPorUsuario() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comentario> comentarioPage = new PageImpl<>(List.of(comentario));
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);
        when(comentarioRepository.findByUsuarioId(1L, pageable)).thenReturn(comentarioPage);

        Page<Comentario> resultado = comentarioService.buscarPorUsuario(1L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getUsuario()).isEqualTo(usuario);
        verify(usuarioService).buscarPorId(1L);
        verify(comentarioRepository).findByUsuarioId(1L, pageable);
    }

    @Test
    @DisplayName("Deve criar comentário e publicar evento quando bug não tem responsável")
    void deveCriarComentarioEPublicarEventoSemResponsavel() {
        Comentario novoComentario = new Comentario();
        novoComentario.setTexto("Novo comentário de teste");

        when(bugService.buscarPorId(1L)).thenReturn(bug);
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> {
            Comentario comentarioSalvo = invocation.getArgument(0);
            comentarioSalvo.setId(2L);
            return comentarioSalvo;
        });

        Comentario resultado = comentarioService.criar(novoComentario, 1L, 1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getBug()).isEqualTo(bug);
        assertThat(resultado.getUsuario()).isEqualTo(usuario);
        assertThat(resultado.getDataComentario()).isNotNull();

        ArgumentCaptor<ComentarioAdicionadoEvento> eventoCaptor = ArgumentCaptor.forClass(ComentarioAdicionadoEvento.class);
        verify(publicadorEvento).publicar(
            eq(TopicosKafka.COMENTARIO_ADICIONADO),
            eq("1"),
            eventoCaptor.capture()
        );

        ComentarioAdicionadoEvento evento = eventoCaptor.getValue();
        assertThat(evento.bugId()).isEqualTo(1L);
        assertThat(evento.comentarioId()).isEqualTo(2L);
        assertThat(evento.autorEmail()).isEqualTo("joao@example.com");
        assertThat(evento.responsavelBugEmail()).isNull(); // Bug sem responsável
    }

    @Test
    @DisplayName("Deve criar comentário e publicar evento quando bug tem responsável")
    void deveCriarComentarioEPublicarEventoComResponsavel() {
        bug.setUsuarioResponsavel(responsavel); // Bug agora tem responsável
        Comentario novoComentario = new Comentario();
        novoComentario.setTexto("Comentário para bug com responsável");

        when(bugService.buscarPorId(1L)).thenReturn(bug);
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> {
            Comentario comentarioSalvo = invocation.getArgument(0);
            comentarioSalvo.setId(3L);
            return comentarioSalvo;
        });

        Comentario resultado = comentarioService.criar(novoComentario, 1L, 1L);

        assertThat(resultado).isNotNull();

        ArgumentCaptor<ComentarioAdicionadoEvento> eventoCaptor = ArgumentCaptor.forClass(ComentarioAdicionadoEvento.class);
        verify(publicadorEvento).publicar(
            eq(TopicosKafka.COMENTARIO_ADICIONADO),
            eq("1"),
            eventoCaptor.capture()
        );

        ComentarioAdicionadoEvento evento = eventoCaptor.getValue();
        assertThat(evento.responsavelBugEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Deve atualizar comentário com sucesso")
    void deveAtualizarComentarioComSucesso() {
        Comentario comentarioAtualizado = new Comentario();
        comentarioAtualizado.setTexto("Texto atualizado do comentário");

        when(comentarioRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(comentario));
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

        Comentario resultado = comentarioService.atualizar(1L, comentarioAtualizado);

        assertThat(resultado.getTexto()).isEqualTo("Texto atualizado do comentário");
        verify(comentarioRepository).save(comentario);
    }

    @Test
    @DisplayName("Deve deletar comentário com sucesso")
    void deveDeletarComentarioComSucesso() {
        when(comentarioRepository.findByIdComRelacionamentos(1L)).thenReturn(Optional.of(comentario));
        doNothing().when(comentarioRepository).delete(comentario);

        comentarioService.deletar(1L);

        verify(comentarioRepository).findByIdComRelacionamentos(1L);
        verify(comentarioRepository).delete(comentario);
    }

    @Test
    @DisplayName("Deve contar comentários de um bug")
    void deveContarComentariosDoBug() {
        when(bugService.buscarPorId(1L)).thenReturn(bug);
        when(comentarioRepository.contarComentariosPorBug(1L)).thenReturn(10L);

        Long quantidade = comentarioService.contarComentariosDoBug(1L);

        assertThat(quantidade).isEqualTo(10L);
        verify(bugService).buscarPorId(1L);
        verify(comentarioRepository).contarComentariosPorBug(1L);
    }

    @Test
    @DisplayName("Deve retornar zero quando bug não tem comentários")
    void deveRetornarZeroQuandoBugNaoTemComentarios() {
        when(bugService.buscarPorId(1L)).thenReturn(bug);
        when(comentarioRepository.contarComentariosPorBug(1L)).thenReturn(0L);

        Long quantidade = comentarioService.contarComentariosDoBug(1L);

        assertThat(quantidade).isZero();
        verify(comentarioRepository).contarComentariosPorBug(1L);
    }
}

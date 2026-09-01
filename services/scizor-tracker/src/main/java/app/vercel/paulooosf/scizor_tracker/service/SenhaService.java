package app.vercel.paulooosf.scizor_tracker.service;

import app.vercel.paulooosf.scizor_tracker.dto.entrada.RedefinirSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.entrada.SolicitarRedefinicaoSenhaDto;
import app.vercel.paulooosf.scizor_tracker.dto.evento.SenhaRedefinicaoSolicitadaEvento;
import app.vercel.paulooosf.scizor_tracker.exception.UsuarioNaoEncontradoException;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.messaging.publicador.PublicadorEvento;
import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final PublicadorEvento publicadorEvento;

    public SenhaService(UsuarioRepository usuarioRepository, TokenService tokenService, 
                       PasswordEncoder passwordEncoder, PublicadorEvento publicadorEvento) {
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.publicadorEvento = publicadorEvento;
    }

    @Transactional(readOnly = true)
    public String solicitarRedefinicaoSenha(SolicitarRedefinicaoSenhaDto dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado com o email: " + dto.email()));

        String token = tokenService.gerarTokenSenha(usuario.getEmail());

        SenhaRedefinicaoSolicitadaEvento evento = new SenhaRedefinicaoSolicitadaEvento(
            usuario.getEmail(),
            token,
            LocalDateTime.now()
        );

        publicadorEvento.publicar(TopicosKafka.SENHA_REDEFINICAO_SOLICITADA, usuario.getEmail(), evento);

        return token;
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaDto dto) {
        String email = tokenService.validarTokenSenha(dto.token());
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuarioRepository.save(usuario);
    }
}

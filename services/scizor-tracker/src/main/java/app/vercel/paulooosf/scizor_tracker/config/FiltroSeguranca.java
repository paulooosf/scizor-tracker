package app.vercel.paulooosf.scizor_tracker.config;

import app.vercel.paulooosf.scizor_tracker.repository.UsuarioRepository;
import app.vercel.paulooosf.scizor_tracker.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroSeguranca extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public FiltroSeguranca(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = recuperarToken(request);
        if (token != null) {
            String email = tokenService.validarToken(token);
            if (email != null) {
                UserDetails usuario = usuarioRepository.findByEmail(email).orElse(null);
                if (usuario != null) {
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String cabecalhoAuth = request.getHeader("Authorization");
        if (cabecalhoAuth == null || !cabecalhoAuth.startsWith("Bearer ")) {
            return null;
        }
        return cabecalhoAuth.replace("Bearer ", "");
    }
}

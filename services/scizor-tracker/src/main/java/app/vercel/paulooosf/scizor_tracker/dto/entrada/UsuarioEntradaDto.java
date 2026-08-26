package app.vercel.paulooosf.scizor_tracker.dto.entrada;

import app.vercel.paulooosf.scizor_tracker.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioEntradaDto(
    @NotBlank(message = "Preencha o nome!")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres!")
    String nome,

    @NotBlank(message = "Preencha o email!")
    @Email(message = "Email inválido!")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres!")
    String email,

    @NotBlank(message = "Preencha a senha!")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres!")
    String senha
) {
    public UsuarioEntradaDto(Usuario usuario) {
        this(usuario.getNome(), usuario.getEmail(), usuario.getSenha());
    }

    public Usuario converter() {
        return new Usuario(nome, email, senha);
    }
}

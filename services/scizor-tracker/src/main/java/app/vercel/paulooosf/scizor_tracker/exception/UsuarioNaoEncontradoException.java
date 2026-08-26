package app.vercel.paulooosf.scizor_tracker.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário com ID " + id + " não encontrado");
    }
}

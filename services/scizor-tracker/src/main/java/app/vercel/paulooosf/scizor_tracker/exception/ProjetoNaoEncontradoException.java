package app.vercel.paulooosf.scizor_tracker.exception;

public class ProjetoNaoEncontradoException extends RuntimeException {
    public ProjetoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public ProjetoNaoEncontradoException(Long id) {
        super("Projeto com ID " + id + " não encontrado");
    }
}

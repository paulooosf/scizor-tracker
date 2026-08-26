package app.vercel.paulooosf.scizor_tracker.exception;

public class ComentarioNaoEncontradoException extends RuntimeException {
    public ComentarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public ComentarioNaoEncontradoException(Long id) {
        super("Comentário com ID " + id + " não encontrado");
    }
}

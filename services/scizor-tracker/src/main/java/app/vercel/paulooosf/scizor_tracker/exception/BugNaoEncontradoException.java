package app.vercel.paulooosf.scizor_tracker.exception;

public class BugNaoEncontradoException extends RuntimeException {

    public BugNaoEncontradoException(Long id) {
        super("Bug com ID " + id + " não encontrado");
    }
}

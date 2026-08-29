package app.vercel.paulooosf.scizor_tracker.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BugNaoEncontradoException.class)
    public ResponseEntity<?> handleBugNaoEncontrado(BugNaoEncontradoException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, "Bug não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ProjetoNaoEncontradoException.class)
    public ResponseEntity<?> handleProjetoNaoEncontrado(ProjetoNaoEncontradoException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, "Projeto não encontrado", ex.getMessage());
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<?> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, "Usuário não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ComentarioNaoEncontradoException.class)
    public ResponseEntity<?> handleComentarioNaoEncontrado(ComentarioNaoEncontradoException ex) {
        return construirResposta(HttpStatus.NOT_FOUND, "Comentário não encontrado", ex.getMessage());
    }

    @ExceptionHandler(StatusInvalidoException.class)
    public ResponseEntity<?> handleStatusInvalido(StatusInvalidoException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, "Status inválido", ex.getMessage());
    }

    @ExceptionHandler(SemPermissaoException.class)
    public ResponseEntity<?> handleSemPermissao(SemPermissaoException ex) {
        return construirResposta(HttpStatus.FORBIDDEN, "Sem permissão", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String mensagem = "Violação de integridade de dados";

        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            mensagem = "Email já cadastrado no sistema";
        }
        
        return construirResposta(HttpStatus.BAD_REQUEST, "Dados inválidos", mensagem);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return construirResposta(HttpStatus.BAD_REQUEST, "Argumento inválido", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String mensagens = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return construirResposta(HttpStatus.BAD_REQUEST, "Argumentos inválidos", mensagens);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExcecaoGenerica(Exception ex) {
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> construirResposta(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("Status", status.value());
        body.put("Erro", error);
        body.put("Mensagem", message);

        return ResponseEntity.status(status).body(body);
    }
}

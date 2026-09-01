package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugResponsavelAtribuidoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.ComentarioAdicionadoEvento;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.service.NotificacaoSnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoConsumer.class);
    
    private final NotificacaoSnsService notificacaoSnsService;

    public NotificacaoConsumer(NotificacaoSnsService notificacaoSnsService) {
        this.notificacaoSnsService = notificacaoSnsService;
    }

    @KafkaListener(topics = TopicosKafka.BUG_CRIADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarBugCriado(BugCriadoEvento evento) {
        log.info("Kafka recebeu bug.criado: Bug #{}", evento.bugId());
        
        try {
            notificacaoSnsService.publicarEvento(evento, "Bug Criado");
            log.info("Evento publicado no SNS com sucesso");
        } catch (Exception e) {
            log.error("Erro ao publicar no SNS: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = TopicosKafka.BUG_STATUS_ALTERADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarStatusAlterado(BugStatusAlteradoEvento evento) {
        log.info("Kafka recebeu bug.status.alterado: Bug #{} -> {}", evento.bugId(), evento.statusNovo());
        
        try {
            notificacaoSnsService.publicarEvento(evento, "Status do Bug Alterado");
            log.info("Evento publicado no SNS com sucesso");
        } catch (Exception e) {
            log.error("Erro ao publicar no SNS: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = TopicosKafka.BUG_RESPONSAVEL_ATRIBUIDO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarResponsavelAtribuido(BugResponsavelAtribuidoEvento evento) {
        log.info("Kafka recebeu bug.responsavel.atribuido: Bug #{} -> {}", evento.bugId(), evento.responsavelEmail());
        
        try {
            notificacaoSnsService.publicarEvento(evento, "Bug Atribuído");
            log.info("Evento publicado no SNS com sucesso");
        } catch (Exception e) {
            log.error("Erro ao publicar no SNS: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = TopicosKafka.COMENTARIO_ADICIONADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarComentarioAdicionado(ComentarioAdicionadoEvento evento) {
        log.info("Kafka recebeu comentario.adicionado: Comentário #{} no Bug #{}", evento.comentarioId(), evento.bugId());
        
        try {
            notificacaoSnsService.publicarEvento(evento, "Novo Comentário");
            log.info("Evento publicado no SNS com sucesso");
        } catch (Exception e) {
            log.error("Erro ao publicar no SNS: {}", e.getMessage());
        }
    }
}

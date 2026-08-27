package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugResponsavelAtribuidoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.dto.evento.ComentarioAdicionadoEvento;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoConsumer {

    @KafkaListener(topics = TopicosKafka.BUG_CRIADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarBugCriado(BugCriadoEvento evento) {
        System.out.println("[NotificacaoConsumer] bug.criado recebido (futuro Lambda/SES): " + evento);
    }

    @KafkaListener(topics = TopicosKafka.BUG_STATUS_ALTERADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarStatusAlterado(BugStatusAlteradoEvento evento) {
        System.out.println("[NotificacaoConsumer] bug.status.alterado recebido (futuro Lambda/SES): " + evento);
    }

    @KafkaListener(topics = TopicosKafka.BUG_RESPONSAVEL_ATRIBUIDO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarResponsavelAtribuido(BugResponsavelAtribuidoEvento evento) {
        System.out.println("[NotificacaoConsumer] bug.responsavel.atribuido recebido (futuro Lambda/SES): " + evento);
    }

    @KafkaListener(topics = TopicosKafka.COMENTARIO_ADICIONADO, groupId = GruposConsumidores.NOTIFICACAO)
    public void notificarComentarioAdicionado(ComentarioAdicionadoEvento evento) {
        System.out.println("[NotificacaoConsumer] comentario.adicionado recebido (futuro Lambda/SES): " + evento);
    }
}

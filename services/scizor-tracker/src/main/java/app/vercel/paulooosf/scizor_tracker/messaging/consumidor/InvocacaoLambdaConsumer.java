package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.enums.StatusBug;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvocacaoLambdaConsumer {

    @KafkaListener(topics = TopicosKafka.BUG_STATUS_ALTERADO, groupId = GruposConsumidores.INVOCACAO_LAMBDA)
    public void invocarRelatorioQuandoResolvido(BugStatusAlteradoEvento evento) {
        if (evento.statusNovo() != StatusBug.RESOLVIDO) {
            return;
        }
        System.out.println("[InvocacaoLambdaConsumer] status RESOLVIDO — futuro Lambda/PDF: " + evento);
    }
}

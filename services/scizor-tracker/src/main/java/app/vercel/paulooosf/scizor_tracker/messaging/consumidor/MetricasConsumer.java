package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugStatusAlteradoEvento;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MetricasConsumer {

    @KafkaListener(topics = TopicosKafka.BUG_STATUS_ALTERADO, groupId = GruposConsumidores.METRICAS)
    public void atualizarContadores(BugStatusAlteradoEvento evento) {
        System.out.println("[MetricasConsumer] atualizar contadores por projeto (boilerplate): " + evento);
    }
}

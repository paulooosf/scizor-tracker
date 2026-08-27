package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AlertaCriticoConsumer {

    @KafkaListener(topics = TopicosKafka.BUG_CRITICO, groupId = GruposConsumidores.ALERTA_CRITICO)
    public void alertar(BugCriadoEvento evento) {
        System.out.println("[AlertaCriticoConsumer] alerta imediato (futuro Lambda/SNS): " + evento);
    }
}

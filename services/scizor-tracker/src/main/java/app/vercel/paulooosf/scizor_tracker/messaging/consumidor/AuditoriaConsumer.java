package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaConsumer {

    @KafkaListener(topics = TopicosKafka.BUG_CRIADO, groupId = GruposConsumidores.AUDITORIA)
    public void registrarCriacao(BugCriadoEvento evento) {
        System.out.println("[AuditoriaConsumer] log de criação recebido: " + evento);
    }
}

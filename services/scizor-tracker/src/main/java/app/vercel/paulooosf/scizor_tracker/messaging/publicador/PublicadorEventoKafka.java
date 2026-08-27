package app.vercel.paulooosf.scizor_tracker.messaging.publicador;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicadorEventoKafka implements PublicadorEvento {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PublicadorEventoKafka(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publicar(String topico, String chave, Object evento) {
        kafkaTemplate.send(topico, chave, evento);
        System.out.println("[PublicadorEvento] tópico=" + topico + " chave=" + chave + " payload=" + evento);
    }
}

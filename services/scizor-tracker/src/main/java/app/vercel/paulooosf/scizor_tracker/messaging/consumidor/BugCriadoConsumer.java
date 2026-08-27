package app.vercel.paulooosf.scizor_tracker.messaging.consumidor;

import app.vercel.paulooosf.scizor_tracker.dto.evento.BugCriadoEvento;
import app.vercel.paulooosf.scizor_tracker.enums.Prioridade;
import app.vercel.paulooosf.scizor_tracker.messaging.GruposConsumidores;
import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import app.vercel.paulooosf.scizor_tracker.messaging.publicador.PublicadorEvento;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BugCriadoConsumer {

    private final PublicadorEvento publicadorEvento;

    public BugCriadoConsumer(PublicadorEvento publicadorEvento) {
        this.publicadorEvento = publicadorEvento;
    }

    @KafkaListener(topics = TopicosKafka.BUG_CRIADO, groupId = GruposConsumidores.REPUBLICADOR_BUG_CRITICO)
    public void republicarSeCritico(BugCriadoEvento evento) {
        if (evento.prioridade() != Prioridade.CRITICA) {
            return;
        }
        publicadorEvento.publicar(TopicosKafka.BUG_CRITICO, String.valueOf(evento.bugId()), evento);
        System.out.println("[BugCriadoConsumer] republicado em bug.critico: " + evento);
    }
}

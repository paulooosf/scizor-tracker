package app.vercel.paulooosf.scizor_tracker.config;

import app.vercel.paulooosf.scizor_tracker.messaging.TopicosKafka;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic topicoBugCriado() {
        return TopicBuilder.name(TopicosKafka.BUG_CRIADO)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic topicoBugStatusAlterado() {
        return TopicBuilder.name(TopicosKafka.BUG_STATUS_ALTERADO)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic topicoBugResponsavelAtribuido() {
        return TopicBuilder.name(TopicosKafka.BUG_RESPONSAVEL_ATRIBUIDO)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic topicoComentarioAdicionado() {
        return TopicBuilder.name(TopicosKafka.COMENTARIO_ADICIONADO)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic topicoBugCritico() {
        return TopicBuilder.name(TopicosKafka.BUG_CRITICO)
            .partitions(3)
            .replicas(1)
            .build();
    }
}

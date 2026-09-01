package app.vercel.paulooosf.scizor_tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
public class NotificacaoSnsService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoSnsService.class);

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    public NotificacaoSnsService(SnsClient snsClient, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
    }

    public void publicarEvento(Object evento, String subject) {
        try {
            String mensagemJson = objectMapper.writeValueAsString(evento);

            PublishRequest.Builder requestBuilder = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(mensagemJson);

            if (subject != null && !subject.isEmpty()) {
                requestBuilder.subject(subject);
            }

            PublishResponse response = snsClient.publish(requestBuilder.build());

            log.info("Evento publicado no SNS | MessageId: {} | Topic: {}",
                    response.messageId(), topicArn);

        } catch (Exception e) {
            log.error("Erro ao publicar evento no SNS: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar notificação", e);
        }
    }

    public void publicarEvento(Object evento) {
        publicarEvento(evento, null);
    }
}

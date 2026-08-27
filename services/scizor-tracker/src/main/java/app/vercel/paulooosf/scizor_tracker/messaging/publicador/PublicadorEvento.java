package app.vercel.paulooosf.scizor_tracker.messaging.publicador;

public interface PublicadorEvento {

    void publicar(String topico, String chave, Object evento);
}

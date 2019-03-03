package ricardo.monitoring;

import org.assertj.core.api.WithAssertions;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import ricardo.monitoring.dbaudit.AuditedOp;
import ricardo.monitoring.dbaudit.DbOp;
import ricardo.monitoring.persistance.BookData;
import ricardo.monitoring.persistance.BookRepository;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RicardoMonitoringTest implements WithAssertions {

    @LocalServerPort
    private int port;

    private String getWsUrl() {
        return "ws://localhost:" + port + "/ws";
    }

    @Autowired
    private BookRepository bookRepository;

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        var transports = List.<Transport>of(new WebSocketTransport(new StandardWebSocketClient()));
        var sockjsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockjsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void e2e_websocket_client_recieve_dbevents() throws Exception {
        StompSession stompSession = stompClient
                .connect(getWsUrl(), new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        try {
            var handler = new MessageHandler();
            stompSession.subscribe("/topic/dbevents", handler);

            var book = bookRepository.save(BookData.anyValidBook());
            var auditedOps = handler.getNextAuditedOps();

            assertThat(auditedOps)
                    .isNotNull()
                    .extracting(AuditedOp::getOp, AuditedOp::getId)
                    .containsExactly(DbOp.INSERT, Long.toString(book.getId()));
        } finally {
            stompSession.disconnect();
            stompClient.stop();
        }
    }

    static class MessageHandler implements StompFrameHandler {

        private final LinkedBlockingDeque<AuditedOp> auditedOps;

        MessageHandler() {
            auditedOps = new LinkedBlockingDeque<>();
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return AuditedOp.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object o) {
            auditedOps.add((AuditedOp) o);
        }

        AuditedOp getNextAuditedOps() throws InterruptedException {
            return auditedOps.pollFirst(10, TimeUnit.SECONDS);
        }
    }
}
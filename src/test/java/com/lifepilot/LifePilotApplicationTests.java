package com.lifepilot;

import com.lifepilot.agent.AiClient;
import com.lifepilot.repository.ChatMessageRepository;
import com.lifepilot.repository.ConversationRepository;
import com.lifepilot.repository.NoteRepository;
import com.lifepilot.repository.TodoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
class LifePilotApplicationTests {

    @MockBean
    private AiClient aiClient;

    @MockBean
    private ConversationRepository conversationRepository;

    @MockBean
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private TodoRepository todoRepository;

    @MockBean
    private NoteRepository noteRepository;

    @Test
    void contextLoads() {
    }
}

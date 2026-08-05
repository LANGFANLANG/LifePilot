package com.lifepilot;

import com.lifepilot.agent.AiClient;
import com.lifepilot.repository.DailyReviewRepository;
import com.lifepilot.repository.ChatMessageRepository;
import com.lifepilot.repository.ConversationRepository;
import com.lifepilot.repository.ExecutionLogRepository;
import com.lifepilot.repository.NoteRepository;
import com.lifepilot.repository.PlanPreviewRepository;
import com.lifepilot.repository.ReminderDeliveryRepository;
import com.lifepilot.repository.TodoRepository;
import com.lifepilot.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@AutoConfigureMockMvc
class LifePilotApplicationTests {

    @Autowired
    private MockMvc mockMvc;

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

    @MockBean
    private ExecutionLogRepository executionLogRepository;

    @MockBean
    private UserAccountRepository userAccountRepository;

    @MockBean
    private PlanPreviewRepository planPreviewRepository;

    @MockBean
    private ReminderDeliveryRepository reminderDeliveryRepository;

    @MockBean
    private DailyReviewRepository dailyReviewRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void exposesActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}

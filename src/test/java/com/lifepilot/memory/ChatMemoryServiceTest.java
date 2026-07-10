package com.lifepilot.memory;

import com.lifepilot.domain.ChatMessage;
import com.lifepilot.domain.ChatRole;
import com.lifepilot.domain.Conversation;
import com.lifepilot.memory.dto.ConversationView;
import com.lifepilot.memory.dto.MessageView;
import com.lifepilot.repository.ChatMessageRepository;
import com.lifepilot.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMemoryService chatMemoryService;

    @Test
    void createsConversation() {
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConversationView conversation = chatMemoryService.createConversation("Project planning");

        assertThat(conversation.id()).isNotNull();
        assertThat(conversation.title()).isEqualTo("Project planning");
    }

    @Test
    void appendsUserAndAssistantMessages() {
        Conversation conversation = Conversation.create("Project planning");
        when(conversationRepository.findById(conversation.getId())).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageView userMessage = chatMemoryService.appendMessage(
                conversation.getId(), ChatRole.USER, "Create a task"
        );
        MessageView assistantMessage = chatMemoryService.appendMessage(
                conversation.getId(), ChatRole.ASSISTANT, "Task created"
        );

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(userMessage.role()).isEqualTo(ChatRole.USER);
        assertThat(assistantMessage.role()).isEqualTo(ChatRole.ASSISTANT);
        assertThat(captor.getAllValues()).allSatisfy(message ->
                assertThat(message.getConversationId()).isEqualTo(conversation.getId())
        );
    }

    @Test
    void loadsRecentMessagesForConversation() {
        Conversation conversation = Conversation.create("Project planning");
        ChatMessage userMessage = conversation.addMessage(ChatRole.USER, "Create a task");
        ChatMessage assistantMessage = conversation.addMessage(ChatRole.ASSISTANT, "Task created");
        when(chatMessageRepository.findByConversation_IdOrderByCreatedAtAsc(conversation.getId()))
                .thenReturn(List.of(userMessage, assistantMessage));

        List<MessageView> messages = chatMemoryService.loadRecentMessages(conversation.getId());

        assertThat(messages).extracting(MessageView::role)
                .containsExactly(ChatRole.USER, ChatRole.ASSISTANT);
    }
}

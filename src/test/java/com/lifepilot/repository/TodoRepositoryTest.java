package com.lifepilot.repository;

import com.lifepilot.domain.Todo;
import com.lifepilot.domain.TodoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/lifepilot",
        "spring.datasource.username=lifepilot",
        "spring.datasource.password=lifepilot",
        "spring.flyway.enabled=true"
})
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Test
    void savesAndFindsTodo() {
        Todo todo = Todo.create("Buy milk", "2 bottles", null);

        Todo saved = todoRepository.save(todo);

        assertThat(todoRepository.findById(saved.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getTitle()).isEqualTo("Buy milk");
                    assertThat(found.getStatus()).isEqualTo(TodoStatus.PENDING);
                });
    }
}

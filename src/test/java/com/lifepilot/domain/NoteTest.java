package com.lifepilot.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoteTest {

    @Test
    void createsNote() {
        Note note = Note.create("Meeting notes", "Discuss the MVP scope");

        assertThat(note.getId()).isNotNull();
        assertThat(note.getTitle()).isEqualTo("Meeting notes");
        assertThat(note.getContent()).isEqualTo("Discuss the MVP scope");
    }
}

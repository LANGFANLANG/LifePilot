package com.lifepilot.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class NoteFileServiceTest {

    @Test
    void storesMarkdownFileAndExtractsPreview() {
        AtomicReference<String> storedKey = new AtomicReference<>();
        NoteFileService service = new NoteFileService((objectKey, contentType, file, size) -> {
            storedKey.set(objectKey);
            assertThat(Files.exists(file)).isTrue();
            assertThat(size).isGreaterThan(0);
        });
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "日报.md",
                "text/markdown",
                "# 日报\n完成上传功能".getBytes()
        );

        NoteFileService.StoredNoteFile stored = service.store(file);

        assertThat(stored.originalFilename()).isEqualTo("日报.md");
        assertThat(stored.preview()).contains("# 日报", "完成上传功能");
        assertThat(stored.storedFilename()).startsWith("notes/");
        assertThat(storedKey.get()).isEqualTo(stored.storedFilename());
    }
}

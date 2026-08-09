package com.lifepilot.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Handles uploaded note file storage and lightweight text preview extraction.
 */
@Service
public class NoteFileService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("md", "markdown", "txt", "csv", "doc", "docx", "xls", "xlsx");

    private final NoteObjectStorage objectStorage;

    /**
     * Creates note file service.
     *
     * @param objectStorage object storage for original files
     */
    public NoteFileService(NoteObjectStorage objectStorage) {
        this.objectStorage = objectStorage;
    }

    /**
     * Stores an uploaded file and extracts preview content.
     *
     * @param file uploaded file
     * @return stored file metadata and preview
     */
    public StoredNoteFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的笔记文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("笔记文件不能超过 20MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "note" : file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("仅支持 md、txt、csv、doc、docx、xls、xlsx 文件");
        }

        String objectKey = "notes/" + UUID.randomUUID() + "." + extension;
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("lifepilot-note-", "." + extension);
            file.transferTo(tempFile);
            String preview = extractPreview(tempFile, extension);
            objectStorage.putObject(objectKey, file.getContentType(), tempFile, file.getSize());
            return new StoredNoteFile(originalFilename, file.getContentType(), objectKey, file.getSize(), preview);
        } catch (IOException ex) {
            throw new IllegalStateException("笔记文件保存失败", ex);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Temp file cleanup failure should not mask upload result.
                }
            }
        }
    }

    private String extractPreview(Path file, String extension) throws IOException {
        return switch (extension) {
            case "md", "markdown", "txt", "csv" -> Files.readString(file, StandardCharsets.UTF_8);
            case "docx" -> extractDocx(file);
            case "xlsx" -> extractXlsx(file);
            case "doc", "xls" -> "已上传原文件。当前暂不支持预览老式 Office 格式，请转换为 docx/xlsx 后重新上传。";
            default -> "已上传原文件。";
        };
    }

    private String extractDocx(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            ZipEntry document = zip.getEntry("word/document.xml");
            if (document == null) return "已上传 Word 文件，但未能提取预览内容。";
            try (InputStream input = zip.getInputStream(document)) {
                String text = xmlText(input);
                return text.isBlank() ? "已上传 Word 文件，但正文为空。" : text;
            }
        }
    }

    private String extractXlsx(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            List<String> sharedStrings = readSharedStrings(zip);
            List<String> rows = new ArrayList<>();
            zip.stream()
                    .filter((entry) -> entry.getName().matches("xl/worksheets/sheet\\d+\\.xml"))
                    .limit(5)
                    .forEach((entry) -> rows.addAll(readSheetRows(zip, entry, sharedStrings)));
            return rows.isEmpty() ? "已上传 Excel 文件，但未能提取预览内容。" : String.join("\n", rows);
        }
    }

    private List<String> readSharedStrings(ZipFile zip) {
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return List.of();
        try (InputStream input = zip.getInputStream(entry)) {
            return List.of(xmlText(input).split("\\R"));
        } catch (IOException ex) {
            return List.of();
        }
    }

    private List<String> readSheetRows(ZipFile zip, ZipEntry entry, List<String> sharedStrings) {
        try (InputStream input = zip.getInputStream(entry)) {
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            List<String> values = new ArrayList<>();
            var matcher = java.util.regex.Pattern.compile("<c[^>]*?(?:t=\"(s)\")?[^>]*>\\s*<v>(.*?)</v>\\s*</c>").matcher(xml);
            while (matcher.find()) {
                String value = matcher.group(2);
                if ("s".equals(matcher.group(1))) {
                    int index = Integer.parseInt(value);
                    values.add(index < sharedStrings.size() ? sharedStrings.get(index) : value);
                } else {
                    values.add(value);
                }
            }
            return values.isEmpty() ? List.of() : List.of(String.join(" | ", values));
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String xmlText(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("</w:p>", "</w:p>\n")
                    .replaceAll("</si>", "</si>\n");
            return factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                    .getDocumentElement()
                    .getTextContent()
                    .lines()
                    .map(String::trim)
                    .filter((line) -> !line.isBlank())
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("");
        } catch (Exception ex) {
            throw new IOException("文件预览提取失败", ex);
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Stored uploaded note file metadata.
     *
     * @param originalFilename original filename
     * @param contentType content type
     * @param storedFilename stored filename
     * @param fileSize file size
     * @param preview extracted preview
     */
    public record StoredNoteFile(
            String originalFilename,
            String contentType,
            String storedFilename,
            long fileSize,
            String preview
    ) {
    }
}

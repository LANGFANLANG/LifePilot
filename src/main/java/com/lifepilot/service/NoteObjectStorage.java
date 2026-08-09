package com.lifepilot.service;

import java.nio.file.Path;

/**
 * Storage boundary for uploaded note files.
 */
public interface NoteObjectStorage {

    /**
     * Stores an object.
     *
     * @param objectKey object key
     * @param contentType content type
     * @param file local source file
     * @param size file size
     */
    void putObject(String objectKey, String contentType, Path file, long size);

    /**
     * Creates a temporary URL for reading an object.
     *
     * @param objectKey object key
     * @param downloadName optional response download filename
     * @return temporary URL
     */
    String temporaryUrl(String objectKey, String downloadName);

    /**
     * Deletes an object if it exists.
     *
     * @param objectKey object key
     */
    void deleteObject(String objectKey);
}

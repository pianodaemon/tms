package com.agnux.haul.storage;

import java.io.BufferedInputStream;
import com.agnux.haul.errors.ElementalException;

import java.io.InputStream;

/**
 * The IStorage interface defines the contract for storage-related operations.
 * It provides methods for uploading and downloading files, retrieving the
 * target name, and generating a URI for accessing a file. The interface
 * abstracts storage systems to allow flexibility for different backend
 * implementations (e.g., cloud, local storage).
 * <p>
 * Implementing classes should define how files are stored and retrieved in
 * their specific storage system.
 */
public interface IStorage {

    /**
     * Uploads a file to the storage system with the provided metadata.
     * <p>
     * This method takes the content type, length, file name, and the input
     * stream of the file to upload. It stores the file in the underlying
     * storage system and may throw an exception if the upload fails.
     *
     * @param cType The content type of the file being uploaded (e.g.,
     * "application/pdf", "image/jpeg").
     * @param len The length of the file in bytes.
     * @param fileName The name of the file to be uploaded.
     * @param inputStream The input stream of the file to upload.
     * @throws ElementalException If an error occurs during the upload, an
     * ElementalException is thrown.
     */
    public void upload(final String cType,
            final long len,
            final String fileName,
            InputStream inputStream) throws ElementalException;

    /**
     * Downloads a file from the storage system.
     * <p>
     * This method takes a file name as input and returns a BufferedInputStream
     * for reading the file data. If the file is not found or there is an issue
     * with downloading, an exception is thrown.
     *
     * @param fileName The name of the file to download.
     * @return A BufferedInputStream that can be used to read the file.
     * @throws ElementalException If an error occurs during the download, an
     * ElementalException is thrown.
     */
    public BufferedInputStream download(final String fileName) throws ElementalException;

    /**
     * Retrieves the target name of the storage system.
     * <p>
     * This method returns a string identifying the storage target. It could
     * represent a specific location or system.
     *
     * @return The target name of the storage system.
     * @throws ElementalException If an error occurs while retrieving the target
     * name, an ElementalException is thrown.
     */
    public String getTargetName() throws ElementalException;

    /**
     * Retrieves the URI (Uniform Resource Identifier) for accessing a file in
     * the storage system.
     * <p>
     * This method generates a URI for the given file name, allowing access to
     * the file via a specified protocol (e.g., HTTP, FTP).
     *
     * @param fileName The name of the file for which the URI is requested.
     * @return A URI string for accessing the file.
     * @throws ElementalException If an error occurs while generating the URI,
     * an ElementalException is thrown.
     */
    public String getUri(final String fileName) throws ElementalException;
}
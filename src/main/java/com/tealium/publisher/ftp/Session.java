package com.tealium.publisher.ftp; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

 
public interface Session<F> {

	boolean remove(String path) throws IOException;

	F[] list(String path) throws IOException;

	void read(String source, OutputStream outputStream) throws IOException;

	void write(InputStream inputStream, String destination) throws IOException;

	/**
	 * Append to a file.
	 * @param inputStream the stream.
	 * @param destination the destination.
	 * @throws IOException an IO Exception. 
	 */
	void append(InputStream inputStream, String destination) throws IOException;

	boolean mkdir(String directory) throws IOException;

	/**
	 * Remove a remote directory.
	 * @param directory The directory.
	 * @return True if the directory was removed.
	 * @throws IOException an IO exception. 
	 */
	boolean rmdir(String directory) throws IOException;

	void rename(String pathFrom, String pathTo) throws IOException;

	void close();

	boolean isOpen();

	boolean exists(String path) throws IOException;

	String[] listNames(String path) throws IOException;

	/**
	 * Retrieve a remote file as a raw {@link InputStream}.
	 * @param source The path of the remote file.
	 * @return The raw inputStream.
	 * @throws IOException Any IOException. 
	 */
	InputStream readRaw(String source) throws IOException;

	/**
	 * Invoke after closing the InputStream from {@link #readRaw(String)}.
	 * Required by some session providers.
	 * @return true if successful.
	 * @throws IOException Any IOException. 
	 */
	boolean finalizeRaw() throws IOException;

	/**
	 * Get the underlying client library's client instance for this session.
	 * Returns an {@code Object} to avoid significant changes to -file, -ftp, -sftp
	 * modules, which would be required
	 * if we added another generic parameter. Implementations should narrow the
	 * return type.
	 * @return The client instance. 
	 */
	Object getClientInstance();
	 
	boolean upload(String localFile);

}


/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tealium.publisher.ftp;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.google.inject.Inject;
import com.tealium.publisher.exception.FileDistributorException;
import com.tealium.publisher.exception.FileDistributorException.Error;

public class FtpSession implements Session<FTPFile> {

	private final Log logger = LogFactory.getLog(this.getClass());

	private FTPClient client;

	private final AtomicBoolean readingRaw = new AtomicBoolean();

	@Inject
	public FtpSession(final FTPClient client) {
		Assert.notNull(client, "client must not be null");
		this.client = client;
	}

	@Override
	public boolean remove(String path) throws IOException {
		Assert.hasText(path, "path must not be null");
		try {
			this.client.deleteFile(path);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("remove : " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}

		return Boolean.TRUE;
	}

	@Override
	public FTPFile[] list(String path) throws IOException {
		Assert.hasText(path, "path must not be null");
		try {
			return this.client.list();
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException
				| FTPListParseException e) {
			logger.error("list : " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}
	}

	@Override
	public String[] listNames(String path) throws IOException {
		Assert.hasText(path, "path must not be null");
		try {
			FTPFile[] files = this.client.list(path);

			if (files != null) {
				String[] names = new String[files.length];

				for (int i = 0; i < files.length; i++) {
					names[i] = files[i].getName();
				}
				return names;
			} else {
				return null;
			}
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException
				| FTPListParseException e) {
			logger.error("listNames: " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}

	}

	@Override
	public void read(String path, OutputStream fos) throws IOException {
		Assert.hasText(path, "path must not be null");
		Assert.notNull(fos, "outputStream must not be null");
		try {
			this.client.download(path, fos, 0, null);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException e) {
			logger.error("read: " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}

		logger.info("File has been successfully transfered from: " + path);
	}

	@Override
	public InputStream readRaw(String source) throws IOException {

		throw new IOException("readRaw - Method Not Supported!");
	}

	@Override
	public boolean finalizeRaw() throws IOException {

		throw new IOException("finalizeRaw - Method Not Supported!");
	}

	@Override
	public void write(InputStream inputStream, String path) throws IOException {
		Assert.notNull(inputStream, "inputStream must not be null");
		Assert.hasText(path, "path must not be null or empty");

		OutputStream outputStream = null;

		try {

			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(new File(path));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			logger.debug("Done!");

		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}

		try {
			this.client.upload(new File(path));
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException e) {
			logger.error("write: " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}
	}

	@Override
	public void append(InputStream inputStream, String path) throws IOException {
		Assert.notNull(inputStream, "inputStream must not be null");
		Assert.hasText(path, "path must not be null or empty");
		OutputStream outputStream = null;

		try {

			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(new File(path));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			logger.debug("Done!");

		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
		try {
			client.append(new File(path));
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException e) {
			logger.error("write: " + path + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}
	}

	@Override
	public void close() {

		try {
			this.client.disconnect(false);
		} catch (IllegalStateException | IOException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("close: -" + getError(e), e);
		}

	}

	@Override
	public boolean isOpen() {
		try {
			this.client.noop();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void rename(String pathFrom, String pathTo) throws IOException {

		try {
			this.client.rename(pathFrom, pathTo);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("rename: " + pathFrom + " to " + pathTo + "-"
					+ getError(e), e);
			throw new IOException(getError(e).toString());
		}

		if (logger.isInfoEnabled()) {
			logger.info("File has been successfully renamed from: " + pathFrom
					+ " to " + pathTo);
		}
	}

	@Override
	public boolean mkdir(String remoteDirectory) throws IOException {
		try {
			client.createDirectory(remoteDirectory);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("mkdir: " + remoteDirectory + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean rmdir(String directory) throws IOException {
		try {
			client.deleteDirectory(directory);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("rmdir: " + directory + "-" + getError(e), e);
			throw new IOException(getError(e).toString());
		}
		return Boolean.TRUE;
	}

	@Override
	public boolean exists(String path) throws IOException {
		Assert.hasText(path, "'path' must not be empty");
		boolean exists = true;
		try {
			client.changeDirectory(path);
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("exists: " + path + "-" + getError(e), e);
			exists = false;
		}

		if (exists) {
			try {
				client.sendCustomCommand("cd");
			} catch (IllegalStateException | FTPIllegalReplyException e) {
				logger.error("exists: " + path + "-" + getError(e), e);
			}
		}

		return exists;

	}

	@Override
	public FTPClient getClientInstance() {
		return this.client;
	}

	public static Error getError(Exception e) {
		if (e.getClass().isAssignableFrom(IllegalStateException.class)) {
			return Error.IllegalState;
		}

		if (e.getClass().isAssignableFrom(IOException.class)) {
			return Error.IOException;
		}

		if (e.getClass().isAssignableFrom(FTPIllegalReplyException.class)) {
			return Error.IllegalReply;
		}

		if (e.getClass().isAssignableFrom(FTPException.class)) {
			return Error.FTPGeneric;
		}

		if (e.getClass().isAssignableFrom(FTPDataTransferException.class)) {
			return Error.DataTransfer;
		}

		if (e.getClass().isAssignableFrom(FTPAbortedException.class)) {
			return Error.Aborted;
		}

		if (e.getClass().isAssignableFrom(FTPListParseException.class)) {
			return Error.ListParse;
		}
		if (e.getClass().isAssignableFrom(FileNotFoundException.class)) {
			return Error.FileNotFound;
		}

		return Error.Unknown;

	}

	@Override
	public boolean upload(String localFile) {
		try {
			this.client.upload(new File(localFile));
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException | FTPDataTransferException | FTPAbortedException
				| IOException e) {
			logger.error("upload: " + localFile, e);
			throw new FileDistributorException(getError(e));
		}

		return true;
	}

}

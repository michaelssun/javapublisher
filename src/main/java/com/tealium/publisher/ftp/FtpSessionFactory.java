/*
 * Copyright 2002-2014 the original author or authors.
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

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.springframework.util.Assert;

import com.google.inject.Inject;
import com.google.inject.name.Named; 
import com.tealium.publisher.util.FTPUtil;

 
public class FtpSessionFactory<T extends FTPClient> implements SessionFactory<FTPFile> {

	public static final String DEFAULT_REMOTE_WORKING_DIRECTORY = "/";

	private final Log logger = LogFactory.getLog(this.getClass());

	protected static final Map<String, Boolean> MODE_Z_SUPPORT = new HashMap<String, Boolean>();
	
	@Inject
	protected FTPClientConfig config;

	protected String username;
 
	protected String host;
 
	protected String password;
	
	protected Map<String, String> connParam;

	//@Inject
	protected int port = org.apache.commons.net.ftp.FTP.DEFAULT_PORT;

	@Inject(optional=true)
	protected int bufferSize = 2048; //see https://issues.apache.org/jira/browse/NET-207

	@Inject(optional=true)
	protected int clientMode = org.apache.commons.net.ftp.FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE;

	@Inject(optional=true)
	protected int fileType = FTPClient.TYPE_BINARY;

	@Inject(optional=true)
	@Named(FTPUtil.FTP_CONTROL_ENCODING)
	protected String controlEncoding = FTPUtil.FTP_CONTROL_ENCODING_UTF_8;

	@Inject(optional=true)
	private Integer connectTimeout;

	@Inject(optional=true)
	private Integer defaultTimeout;

	@Inject(optional=true)
	private Integer dataTimeout;
	
	public FtpSessionFactory(Map<String, String> connParam){
		this.connParam=connParam;
		username=connParam.get(FTPUtil.FTP_USER_NAME);
		password=connParam.get(FTPUtil.FTP_PASSWORD);
		host=connParam.get(FTPUtil.FTP_HOST);
		username=connParam.get(FTPUtil.FTP_USER_NAME);
		
	}


	@Override
	public FtpSession getSession() {
		try {
			return new FtpSession(this.createClient());
		}
		catch (Exception e) {
			throw new IllegalStateException("failed to create FTPClient", e);
		}
	}

	private T createClient() throws SocketException, IOException {
		final T client = (T) this.createClientInstance();
		Assert.notNull(client, "client must not be null");
		Assert.hasText(this.username, "username is required");


		this.postProcessClientBeforeConnect(client);

		// Connect
		try {
			String[] resutls = client.connect(this.host, this.port);
			logger.debug("Connected to server [" + host + ":" + port + "]");

			// Login
			client.login(username, password);
			 
		} catch (IllegalStateException | FTPIllegalReplyException
				| FTPException e) {
			logger.error("createClient: -" + FtpSession.getError(e),e);
			throw new IOException(FtpSession.getError(e).toString());
		}

		this.postProcessClientAfterConnect(client);

		this.updateClientMode(client);
		client.setType(fileType); 
		client.setCharset(controlEncoding);
		if (connParam.get(FTPUtil.ALIVE_AUTO_PING)!=null) {
			client.setAutoNoopTimeout(Long.parseLong(connParam.get(FTPUtil.ALIVE_AUTO_PING)));
		}
		client.setCompressionEnabled(isCompressSupported(connParam.get(FTPUtil.FTP_HOST),client));
		return client;
	}
	
	private synchronized boolean isCompressSupported(String host,FTPClient client) {
		if (MODE_Z_SUPPORT.get(host) != null) {
			return MODE_Z_SUPPORT.get(host);
		}

		boolean modeZSupport = client.isCompressionSupported();
		MODE_Z_SUPPORT.put(host, modeZSupport);
		return modeZSupport;
	}

	/**
	 * Sets the mode of the connection. Only local modes are supported.
	 */
	private void updateClientMode(FTPClient client) {
		switch (this.clientMode) {
			case org.apache.commons.net.ftp.FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE:
				client.setPassive(Boolean .FALSE);
				break;
			case org.apache.commons.net.ftp.FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE:
				client.setPassive(Boolean.TRUE);
				break;
			default:
				break;
		}
	}

	protected FTPClient createClientInstance() {
		return new FTPClient();
	}

	/**
	 * Will handle additional initialization after client.connect() method was invoked,
	 * but before any action on the client has been taken
	 *
	 * @param t The client.
	 * @throws IOException Any IOException
	 */
	protected void postProcessClientAfterConnect(T t) throws IOException {
		// NOOP
	}
	/**
	 * Will handle additional initialization before client.connect() method was invoked.
	 *
	 * @param client The client.
	 * @throws IOException Any IOException.
	 */
	protected void postProcessClientBeforeConnect(T client) throws IOException {
		// NOOP
	}

}

package com.tealium.publisher.ftp;

import static com.tealium.publisher.provider.FtpSessionFactoryProvider.ConnectionType.FTP_CDN_AKAMAI;
import static com.tealium.publisher.provider.FtpSessionFactoryProvider.ConnectionType.FTP_CDN_NETWORKS;
import it.sauronsoftware.ftp4j.FTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tealium.publisher.annotation.FtpSessionFactoryAkamai;
import com.tealium.publisher.annotation.FtpSessionFactoryCDNetworks;
import com.tealium.publisher.exception.FileDistributorException;
import com.tealium.publisher.exception.FileDistributorException.Error;
import com.tealium.publisher.provider.FtpSessionFactoryProvider.ConnectionType;
import com.tealium.publisher.util.FTPUtil;

public class FtpFileDistributor {
	private static Logger logger = LoggerFactory
			.getLogger(FtpFileDistributor.class);
	public static final String BASE_PATH = "basePath";
	public static final String RETRIALS_NUM = "retrialsNum";
	public static final String FIXED_THREAD_POOL_SIZE = "fixedThreadPoolSize";

//	public static final String ALIVE_AUTO_PING = "alive.auto.ping";
//	public static final String FTP_USER_NAME = "ftp_user_name";
//	public static final String FTP_PASSWORD = "ftp_password";
//	public static final String FTP_HOST = "ftphost";
//
//	public static final String FTP_HOST_AKAMAI = "akamai.ftphost";
//	public static final String FTP_HOST_EDGECACHE = "edgecache.ftphost";
//	public static final String FTP_HOST_LIMELIGHT = "limelight.ftphost";
//	public static final String FTP_HOST_CDNETWORKS = "limelight.ftphost";
//
//	public static final String ALIVE_AUTO_PING_AKAMAI = "akamai.alive.auto.ping";
//	public static final String FTP_USER_NAME_AKAMAI = "akamai.ftp_user_name";
//	public static final String FTP_PASSWORD_AKAMAI = "akamai.ftp_password";
//
//	public static final String ALIVE_AUTO_PING_CDNETWORKS = "cdnetworks.alive.auto.ping";
//	public static final String FTP_USER_NAME_CDNETWORKS = "cdnetworks.ftp_user_name";
//	public static final String FTP_PASSWORD_CDNETWORKS = "cdnetworks.ftp_password";
//
//	public static final String ALIVE_AUTO_PING_EDGECACHE = "edgecache.alive.auto.ping";
//	public static final String FTP_USER_NAME_EDGECACHE = "edgecache.ftp_user_name";
//	public static final String FTP_PASSWORD_EDGECACHE = "edgecache.ftp_password";
//
//	public static final String ALIVE_AUTO_PING_LIMELIGHT = "limelight.alive.auto.ping";
//	public static final String FTP_USER_NAME_LIMELIGHT = "limelight.ftp_user_name";
//	public static final String FTP_PASSWORD_LIMELIGHT = "limelight.ftp_password";
//
//	public static final String FTP_SESSION_FACTORY_CD_NETWORKS = "FtpSessionFactoryCDNetworks";
//	public static final String FTP_SESSION_FACTORY_AKAMAI = "FtpSessionFactoryAkamai";
//
//	/**
//	 * The constant for the FTP security level.
//	 * 
//	 * 
//	 */
//	public static final int SECURITY_FTP = 0;
//	/**
//	 * The constant for the FTPS (FTP over implicit TLS/SSL) security level.
//	 * 
//	 * 
//	 */
//	public static final int SECURITY_FTPS = 1;
//	/**
//	 * The constant for the FTPES (FTP over explicit TLS/SSL) security level.
//	 * 
//	 * 
//	 */
//	public static final int SECURITY_FTPES = 2;
//
//	/**
//	 * The constant for the MLSD policy that causes the client to use the MLSD
//	 * command instead of LIST, but only if the MLSD command is explicitly
//	 * supported by the server (the support is tested with the FEAT command).
//	 * 
//	 * 
//	 */
//	public static final int MLSD_IF_SUPPORTED = 0;
//	/**
//	 * The constant for the MLSD policy that causes the client to use always the
//	 * MLSD command instead of LIST, also if the MLSD command is not explicitly
//	 * supported by the server (the support is tested with the FEAT command).
//	 * 
//	 * 
//	 */
//	public static final int MLSD_ALWAYS = 1;
//	/**
//	 * The constant for the MLSD policy that causes the client to use always the
//	 * LIST command, also if the MLSD command is explicitly supported by the
//	 * server (the support is tested with the FEAT command).
//	 * 
//	 * 
//	 */
//	public static final int MLSD_NEVER = 2;

	@Inject(optional = true)
	@Named(FIXED_THREAD_POOL_SIZE)
	private int fixedThreadPoolSize = 3;

	@Inject
	@FtpSessionFactoryAkamai
	private FtpSessionFactory<FTPClient> sessionFactoryAkamai;
	@Inject
	@FtpSessionFactoryCDNetworks
	private FtpSessionFactory<FTPClient> sessionFactoryCDNetworks;

	@Inject(optional = true)
	@Named(RETRIALS_NUM)
	private int retrialsNum = 3;

	@Inject(optional = true)
	@Named(BASE_PATH)
	private String basePath = "/tmp/";

	/**
	 * upload data in a map<filename, string as byte[]>; application will write
	 * out the data into a temp designated folder then do ftp upload
	 * 
	 * @param filesMap
	 * @return
	 */
	public boolean uploadFiles(Map<String, byte[]> filesMap) {
		LinkedList<String> files = new LinkedList<String>();
		for (final Entry<String, byte[]> entry : filesMap.entrySet()) {
			try {
				FTPUtil.writeToFile(BASE_PATH + entry.getKey(), new String(
						entry.getValue()));
				files.add(BASE_PATH + entry.getKey());
			} catch (IOException e) {
				logger.error("Unable to write file " + entry.getKey(), e);
				;
			}
		}

		return uploadFiles(files);
	}

	/**
	 * upload files in a list; file should have absolute path, e.g.,
	 * /tmp/utag.sync.js
	 * 
	 * @param files
	 * @return
	 */
	public boolean uploadFiles(LinkedList<String> files) {
		if (logger.isDebugEnabled()) {
			logger.debug("upload(" + files + ")");
		}
		return uploadFiles(files, null, 0);
	}

	private boolean uploadFiles(LinkedList<String> files,
			Collection<FtpResult> failureResults, int retrials) {
		logger.debug("uplodaing(" + files + "|" + failureResults + "|"
				+ retrials);
		ListeningExecutorService service = MoreExecutors
				.listeningDecorator(Executors
						.newFixedThreadPool(fixedThreadPoolSize));

		Map<ConnectionType, FtpWorker> requiredFtpSessions = getRequiredFtpSessions(
				files, failureResults);

		final List<FtpResult> results = new ArrayList<FtpResult>();
		List<ListenableFuture<FtpResult>> futures = new ArrayList<ListenableFuture<FtpResult>>();

		for (final Entry<ConnectionType, FtpWorker> entry : requiredFtpSessions
				.entrySet()) {
			ListenableFuture<FtpResult> listenableFutureAkamai = service
					.submit(entry.getValue());
			Futures.addCallback(listenableFutureAkamai,
					new FutureCallback<FtpResult>() {
						@Override
						public void onFailure(Throwable thrown) {
							logger.error(
									"exception to upload files"
											+ entry.getKey(), thrown);
							FtpResult ftpResult = new FtpResult(entry.getKey(),
									null);
							ftpResult.setSuccess(Boolean.FALSE);
							results.add(ftpResult);
						}

						@Override
						public void onSuccess(FtpResult result) {
							results.add(result);
						}
					});
			futures.add(listenableFutureAkamai);
		}
		List<FtpResult> results1 = null;
		try {
			results1 = Futures.allAsList(futures).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("exception to upload files: " + files);
			;
		}
		service.shutdown();
		logger.debug("results::" + results);
		Iterator<FtpResult> iterator = results.iterator();

		while (iterator.hasNext()) {
			FtpResult result = iterator.next();
			if (result.isSuccess()) {
				results.remove(result);
			}
		}

		if (results.size() > 0) {
			if (retrials + 1 > retrialsNum) {
				logger.error("too many retrials: " + retrials + " for " + files);
				throw new FileDistributorException(Error.TooManyRetrial);
			}

			return uploadFiles(files, failureResults, retrials + 1);
		}
		logger.info("complete: " + results);
		return Boolean.TRUE;
	}

	/**
	 * create a map including which ftp site to create ftp session and failure
	 * uploaded files
	 * 
	 * @param files
	 * @param failureResults
	 * @return
	 */
	private Map<ConnectionType, FtpWorker> getRequiredFtpSessions(
			LinkedList<String> files, Collection<FtpResult> failureResults) {
		Map<ConnectionType, FtpWorker> requiredFtpSessions = new HashMap<ConnectionType, FtpWorker>();

		if (CollectionUtils.isEmpty(failureResults)) {
			FtpWorker akamaiWorker = new FtpWorker(
					(FtpSession) sessionFactoryAkamai.getSession(), files,
					retrialsNum, FTP_CDN_AKAMAI);
			FtpWorker cdnetworkWorker = new FtpWorker(
					(FtpSession) sessionFactoryCDNetworks.getSession(), files,
					retrialsNum, FTP_CDN_NETWORKS);
			requiredFtpSessions.put(FTP_CDN_AKAMAI, akamaiWorker);
			requiredFtpSessions.put(FTP_CDN_NETWORKS, cdnetworkWorker);
		} else {
			for (FtpResult ftpResult : failureResults) {
				switch (ftpResult.getConnType()) {
				case FTP_CDN_AKAMAI:
					FtpWorker akamaiWorker = new FtpWorker(
							(FtpSession) sessionFactoryAkamai.getSession(),
							getUncompleteFiles(files, ftpResult), retrialsNum,
							FTP_CDN_AKAMAI);
					requiredFtpSessions.put(FTP_CDN_AKAMAI, akamaiWorker);
					break;

				case FTP_CDN_NETWORKS:
					FtpWorker networkWorker = new FtpWorker(
							(FtpSession) sessionFactoryAkamai.getSession(),
							getUncompleteFiles(files, ftpResult), retrialsNum,
							FTP_CDN_AKAMAI);
					requiredFtpSessions.put(FTP_CDN_NETWORKS, networkWorker);
					break;

				default:
					throw new IllegalArgumentException(
							"not supported connection type: "
									+ ftpResult.getConnType());
				}
			}
		}

		return requiredFtpSessions;
	}

	/**
	 * remove already uploaded files from input linkedlist files
	 * 
	 * @param files
	 * @param failureResult
	 * @return
	 */
	private LinkedList<String> getUncompleteFiles(LinkedList<String> files,
			FtpResult failureResult) {
		if (failureResult == null
				|| Strings.isNullOrEmpty(failureResult.getLastFailedFileName())) {
			return files;
		}
		Iterator<String> iterator = files.iterator();

		while (iterator.hasNext()) {
			String file = iterator.next();
			if (!failureResult.getLastFailedFileName().equals(file)) {
				files.remove(file);
			} else {
				logger.info("found failed file! remaining files: " + files);
				break;
			}
		}

		return files;
	}
}

/**
 * thread-run object to upload files
 * 
 * @author michaelsun
 *
 */
class FtpWorker implements Callable<FtpResult> {
	private FtpSession session;
	private LinkedList<String> fileList;
	private int retrialsNum;
	private ConnectionType connType;

	public FtpWorker(FtpSession session, LinkedList<String> fileList,
			int retrialsNum, ConnectionType connType) {
		this.session = session;
		this.fileList = fileList;
		this.retrialsNum = retrialsNum;
		this.connType = connType;
	}

	@Override
	public FtpResult call() throws Exception {
		FtpResult ftpResult = null;
		for (Iterator<String> i = fileList.iterator(); i.hasNext();) {
			String localFile = (String) i.next();
			int count = 0;
			boolean success = false;
			while (count < retrialsNum) {
				success = session.upload(localFile);
				if (success) {
					break;
				}
				count++;
			}
			if (!success) {
				ftpResult = new FtpResult(connType, localFile);
				ftpResult.setSuccess(Boolean.FALSE);
				return ftpResult;
			}
		}
		ftpResult = new FtpResult(connType, null);
		ftpResult.setSuccess(Boolean.TRUE);
		return ftpResult;
	}
}

/**
 * result data wrapper filename - last failed file name
 *
 */
class FtpResult {
	private ConnectionType connType;
	private String lastFailedFileName;
	boolean success;

	public FtpResult(ConnectionType connType, String fileName) {
		this.connType = connType;
		this.lastFailedFileName = fileName;
	}

	public ConnectionType getConnType() {
		return connType;
	}

	public String getLastFailedFileName() {
		return lastFailedFileName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat)
			return true;

		if (!(aThat instanceof FtpResult))
			return false;
		FtpResult rhs = (FtpResult) aThat;
		return new EqualsBuilder().append(this.connType, rhs.getConnType())
				.append(this.lastFailedFileName, rhs.lastFailedFileName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return (connType != null ? connType.hashCode() : 120)
				+ (lastFailedFileName != null ? lastFailedFileName.hashCode()
						: 100);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

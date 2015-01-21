package com.tealium.publisher.provider;

import it.sauronsoftware.ftp4j.FTPClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.tealium.publisher.ftp.FtpFileDistributor;
import com.tealium.publisher.ftp.FtpSessionFactory;

/**
 * connection parameter provider
 *
 */

public class FtpSessionFactoryProvider implements
		Provider<FtpSessionFactory<FTPClient>> {

	public enum ConnectionType {
		FTP_CDN_AKAMAI, FTP_CDN_EDGECACHE, FTP_CDN_LIMELIGHT, FTP_CDN_NETWORKS
	};

	public static final Collection<ConnectionType> CONN_TYPE_LIST = Arrays
			.asList(new ConnectionType[] { ConnectionType.FTP_CDN_AKAMAI,
					ConnectionType.FTP_CDN_EDGECACHE,
					ConnectionType.FTP_CDN_LIMELIGHT,
					ConnectionType.FTP_CDN_NETWORKS });

	private ConnectionType connectionType;

	@Inject
	@Named(FtpFileDistributor.ALIVE_AUTO_PING_AKAMAI)
	private String ftpAliveAutoPingAkamai;
	@Inject
	@Named(FtpFileDistributor.FTP_HOST_AKAMAI)
	private String ftpHostAkamai;
	@Inject
	@Named(FtpFileDistributor.FTP_USER_NAME_AKAMAI)
	private String ftpUserNameAkamai;
	@Inject
	@Named(FtpFileDistributor.FTP_PASSWORD_AKAMAI)
	private String ftpPasswordAkamai;

	@Inject
	@Named(FtpFileDistributor.ALIVE_AUTO_PING_EDGECACHE)
	private String ftpAliveAutoPingEdgeCache;
	@Inject
	@Named(FtpFileDistributor.FTP_HOST_EDGECACHE)
	private String ftpHostEdgeCache;
	@Inject
	@Named(FtpFileDistributor.FTP_USER_NAME_EDGECACHE)
	private String ftpUserNameEdgeCache;
	@Inject
	@Named(FtpFileDistributor.FTP_PASSWORD_EDGECACHE)
	private String ftpPasswordEdgeCache;

	@Inject
	@Named(FtpFileDistributor.ALIVE_AUTO_PING_LIMELIGHT)
	private String ftpAliveAutoPingLimeLight;
	@Inject
	@Named(FtpFileDistributor.FTP_HOST_LIMELIGHT)
	private String ftpHostLimeLight;
	@Inject
	@Named(FtpFileDistributor.FTP_USER_NAME_LIMELIGHT)
	private String ftpUserNameLimeLight;
	@Inject
	@Named(FtpFileDistributor.FTP_PASSWORD_LIMELIGHT)
	private String ftpPasswordLimeLight;

	@Inject
	@Named(FtpFileDistributor.ALIVE_AUTO_PING_CDNETWORKS)
	private String ftpAliveAutoPingCDNetworks;
	@Inject
	@Named(FtpFileDistributor.FTP_HOST_CDNETWORKS)
	private String ftpHostCDNetworks;
	@Inject
	@Named(FtpFileDistributor.FTP_USER_NAME_CDNETWORKS)
	private String ftpUserNameCDNetworks;
	@Inject
	@Named(FtpFileDistributor.FTP_PASSWORD_CDNETWORKS)
	private String ftpPasswordCDNetworks;

	private final static Map<ConnectionType, Map<String, String>> paramMap = new HashMap<FtpSessionFactoryProvider.ConnectionType, Map<String, String>>();

	public FtpSessionFactoryProvider(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	@Override
	public FtpSessionFactory<FTPClient> get() {
		if (paramMap.get(connectionType) == null) {
			setParamMap();
		}

		return new FtpSessionFactory<FTPClient>(paramMap.get(connectionType));
	}

	private void setParamMap() {
		paramMap.put(
				ConnectionType.FTP_CDN_AKAMAI,
				getFTPConnParams(ftpHostAkamai, ftpUserNameAkamai,
						ftpPasswordAkamai, ftpAliveAutoPingAkamai));
		paramMap.put(
				ConnectionType.FTP_CDN_LIMELIGHT,
				getFTPConnParams(ftpHostLimeLight, ftpUserNameLimeLight,
						ftpPasswordLimeLight, ftpAliveAutoPingLimeLight));
		paramMap.put(
				ConnectionType.FTP_CDN_EDGECACHE,
				getFTPConnParams(ftpHostEdgeCache, ftpUserNameEdgeCache,
						ftpPasswordEdgeCache, ftpAliveAutoPingEdgeCache));
		paramMap.put(
				ConnectionType.FTP_CDN_NETWORKS,
				getFTPConnParams(ftpHostCDNetworks, ftpUserNameCDNetworks,
						ftpPasswordCDNetworks, ftpAliveAutoPingCDNetworks));

	}

	private Map<String, String> getFTPConnParams(String host, String userName,
			String password, String aliveAutoPing) {
		Map<String, String> connParams = new HashMap<String, String>();
		connParams.put(FtpFileDistributor.FTP_HOST, host);
		connParams.put(FtpFileDistributor.FTP_USER_NAME, userName);
		connParams.put(FtpFileDistributor.FTP_PASSWORD, password);
		connParams.put(FtpFileDistributor.ALIVE_AUTO_PING, aliveAutoPing);

		return connParams;
	}
}

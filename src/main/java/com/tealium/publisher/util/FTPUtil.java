package com.tealium.publisher.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FTPUtil {
	enum CDNSource {
		Akamai("akamai"), LimeLight("limelight"), EdgeCache("edgecache");

		private String cdnName;

		public String getName() {
			return cdnName;
		}

		private CDNSource(String cdnName) {
			this.cdnName = cdnName;
		}
	}

	public static final String ALIVE_AUTO_PING = "alive.auto.ping";
	public static final String FTP_USER_NAME = "ftp_user_name";
	public static final String FTP_PASSWORD = "ftp_password";
	public static final String FTP_HOST = "ftphost";

	public static final String FTP_HOST_AKAMAI = "akamai.ftphost";
	public static final String FTP_HOST_EDGECACHE = "edgecache.ftphost";
	public static final String FTP_HOST_LIMELIGHT = "limelight.ftphost";
	public static final String FTP_HOST_CDNETWORKS = "limelight.ftphost";

	public static final String ALIVE_AUTO_PING_AKAMAI = "akamai.alive.auto.ping";
	public static final String FTP_USER_NAME_AKAMAI = "akamai.ftp_user_name";
	public static final String FTP_PASSWORD_AKAMAI = "akamai.ftp_password";

	public static final String ALIVE_AUTO_PING_CDNETWORKS = "cdnetworks.alive.auto.ping";
	public static final String FTP_USER_NAME_CDNETWORKS = "cdnetworks.ftp_user_name";
	public static final String FTP_PASSWORD_CDNETWORKS = "cdnetworks.ftp_password";

	public static final String ALIVE_AUTO_PING_EDGECACHE = "edgecache.alive.auto.ping";
	public static final String FTP_USER_NAME_EDGECACHE = "edgecache.ftp_user_name";
	public static final String FTP_PASSWORD_EDGECACHE = "edgecache.ftp_password";

	public static final String ALIVE_AUTO_PING_LIMELIGHT = "limelight.alive.auto.ping";
	public static final String FTP_USER_NAME_LIMELIGHT = "limelight.ftp_user_name";
	public static final String FTP_PASSWORD_LIMELIGHT = "limelight.ftp_password";

	public static final String FTP_CONTROL_ENCODING = "ftp.control.encoding";
	public static final String FTP_CONTROL_ENCODING_UTF_8 = "UTF-8";

	/**
	 * The constant for the FTP security level.
	 * 
	 * 
	 */
	public static final int SECURITY_FTP = 0;
	/**
	 * The constant for the FTPS (FTP over implicit TLS/SSL) security level.
	 * 
	 * 
	 */
	public static final int SECURITY_FTPS = 1;
	/**
	 * The constant for the FTPES (FTP over explicit TLS/SSL) security level.
	 * 
	 * 
	 */
	public static final int SECURITY_FTPES = 2;

	/**
	 * The constant for the MLSD policy that causes the client to use the MLSD
	 * command instead of LIST, but only if the MLSD command is explicitly
	 * supported by the server (the support is tested with the FEAT command).
	 * 
	 * 
	 */
	public static final int MLSD_IF_SUPPORTED = 0;
	/**
	 * The constant for the MLSD policy that causes the client to use always the
	 * MLSD command instead of LIST, also if the MLSD command is not explicitly
	 * supported by the server (the support is tested with the FEAT command).
	 * 
	 * 
	 */
	public static final int MLSD_ALWAYS = 1;
	/**
	 * The constant for the MLSD policy that causes the client to use always the
	 * LIST command, also if the MLSD command is explicitly supported by the
	 * server (the support is tested with the FEAT command).
	 * 
	 * 
	 */
	public static final int MLSD_NEVER = 2;

	/**
	 * write out string data to a file with path as absolute path, e.g.,
	 * /tmp/utag.sync.js
	 * 
	 * @param path
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static boolean writeToFile(String path, String data)
			throws IOException {
		Path path2 = Paths.get(path);
		Files.write(path2, Arrays.asList(new String[] { data }),
				StandardCharsets.UTF_8);
		return true;
	}
	
	public static List<String> readContent(String templatePath) throws IOException {
		Path path = Paths.get(templatePath);
		return Files.readAllLines(path, StandardCharsets.UTF_8); 
	} 
}

package com.tealium.publisher.ftp;

import static com.tealium.publisher.util.FTPUtil.ALIVE_AUTO_PING_AKAMAI;
import static com.tealium.publisher.util.FTPUtil.ALIVE_AUTO_PING_CDNETWORKS;
import static com.tealium.publisher.util.FTPUtil.ALIVE_AUTO_PING_EDGECACHE;
import static com.tealium.publisher.util.FTPUtil.ALIVE_AUTO_PING_LIMELIGHT;
import static com.tealium.publisher.util.FTPUtil.FTP_HOST_AKAMAI;
import static com.tealium.publisher.util.FTPUtil.FTP_HOST_CDNETWORKS;
import static com.tealium.publisher.util.FTPUtil.FTP_HOST_EDGECACHE;
import static com.tealium.publisher.util.FTPUtil.FTP_HOST_LIMELIGHT;
import static com.tealium.publisher.util.FTPUtil.FTP_PASSWORD_AKAMAI;
import static com.tealium.publisher.util.FTPUtil.FTP_PASSWORD_CDNETWORKS;
import static com.tealium.publisher.util.FTPUtil.FTP_PASSWORD_EDGECACHE;
import static com.tealium.publisher.util.FTPUtil.FTP_PASSWORD_LIMELIGHT;
import static com.tealium.publisher.util.FTPUtil.FTP_USER_NAME_AKAMAI;
import static com.tealium.publisher.util.FTPUtil.FTP_USER_NAME_CDNETWORKS;
import static com.tealium.publisher.util.FTPUtil.FTP_USER_NAME_EDGECACHE;
import static com.tealium.publisher.util.FTPUtil.FTP_USER_NAME_LIMELIGHT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.sauronsoftware.ftp4j.FTPClient;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.tealium.publisher.annotation.FtpSessionFactoryAkamai;
import com.tealium.publisher.annotation.FtpSessionFactoryCDNetworks;
import com.tealium.publisher.provider.FtpSessionFactoryProvider;
import com.tealium.publisher.provider.FtpSessionFactoryProvider.ConnectionType;
public class FtpFileDistributorTest {
	private FtpFileDistributor ftpFileDistributor;
	
	@Mock
	private  FtpSessionFactory<FTPClient> akamaiFtpSessionFactory; 
	@Mock
	private  FtpSessionFactory<FTPClient> cdnetworksFtpSessionFactory;
	@Before
	public void before(){
		akamaiFtpSessionFactory=Mockito.mock(FtpSessionFactory.class
				);
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {

				bind(String.class).annotatedWith(Names.named(ALIVE_AUTO_PING_AKAMAI)).toInstance("30000");
				bind(String.class).annotatedWith(Names.named(FTP_HOST_AKAMAI)).toInstance("upload.tealium.sf.cdngp.net");
				bind(String.class).annotatedWith(Names.named(FTP_USER_NAME_AKAMAI)).toInstance("dev_tealium");
				bind(String.class).annotatedWith(Names.named(FTP_PASSWORD_AKAMAI)).toInstance("sz4NbTDnw4fs8Fx9PP");

				bind(String.class).annotatedWith(Names.named(ALIVE_AUTO_PING_EDGECACHE)).toInstance("30000");
				bind(String.class).annotatedWith(Names.named(FTP_HOST_EDGECACHE)).toInstance("upload.tealium.sf.cdngp.net");
				bind(String.class).annotatedWith(Names.named(FTP_USER_NAME_EDGECACHE)).toInstance("dev_tealium");
				bind(String.class).annotatedWith(Names.named(FTP_PASSWORD_EDGECACHE)).toInstance("sz4NbTDnw4fs8Fx9PP");

				bind(String.class).annotatedWith(Names.named(ALIVE_AUTO_PING_LIMELIGHT)).toInstance("30000");
				bind(String.class).annotatedWith(Names.named(FTP_HOST_LIMELIGHT)).toInstance("upload.tealium.sf.cdngp.net");
				bind(String.class).annotatedWith(Names.named(FTP_USER_NAME_LIMELIGHT)).toInstance("dev_tealium");
				bind(String.class).annotatedWith(Names.named(FTP_PASSWORD_LIMELIGHT)).toInstance("sz4NbTDnw4fs8Fx9PP");

				bind(String.class).annotatedWith(Names.named(ALIVE_AUTO_PING_CDNETWORKS)).toInstance("30000");
				bind(String.class).annotatedWith(Names.named(FTP_HOST_CDNETWORKS)).toInstance("upload.tealium.sf.cdngp.net");
				bind(String.class).annotatedWith(Names.named(FTP_USER_NAME_CDNETWORKS)).toInstance("dev_tealium");
				bind(String.class).annotatedWith(Names.named(FTP_PASSWORD_CDNETWORKS)).toInstance("sz4NbTDnw4fs8Fx9PP");

				bind(new TypeLiteral<FtpSessionFactory<FTPClient>>(){})
			    .annotatedWith(FtpSessionFactoryAkamai.class).toProvider(new FtpSessionFactoryProvider(ConnectionType.FTP_CDN_AKAMAI)).in(Scopes.SINGLETON);
				bind(new TypeLiteral<FtpSessionFactory<FTPClient>>(){})
			    .annotatedWith(FtpSessionFactoryCDNetworks.class).toProvider(new FtpSessionFactoryProvider(ConnectionType.FTP_CDN_NETWORKS)).in(Scopes.SINGLETON);
				 
			}

		});

		ftpFileDistributor=injector.getInstance(FtpFileDistributor.class);
		assertNotNull(ftpFileDistributor);
	}
	
	@Test
	public void testUploadFiles(){
		LinkedList<String> files=new LinkedList<String>();
		String basePath="/Users/michaelsun/Documents/dev/design/";
		files.addAll(Arrays.asList(new String[]{basePath+"local.sampleprofile.json",basePath+"local.sampleprofile-failpublish.json"}));
		assertTrue(ftpFileDistributor.uploadFiles(files));
	}

}

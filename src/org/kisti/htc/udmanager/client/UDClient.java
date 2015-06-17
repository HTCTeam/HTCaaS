package org.kisti.htc.udmanager.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.message.Message;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import org.kisti.htc.udmanager.bean.DataHandlerFile;
import org.kisti.htc.udmanager.server.ChecksumChecker;
import org.kisti.htc.udmanager.server.UserDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UDClient {

	final static Logger logger = LoggerFactory.getLogger(UDClient.class);
	public UserDataManager udclient;
	private static String UDManagerURL;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;

	private static String FTPAddress;
	private boolean equalServer = false;

	public UDClient() {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			UDManagerURL = prop.getProperty("UDManager.Address");

			if(prop.getProperty("SSL.Authentication").equals("true")){
				SSL = true;
				UDManagerURL = UDManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}
			FTPAddress = prop.getProperty("FTP.Address");
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}

		Map<String, Object> props = new HashMap<String, Object>();
//		props.put("mtom-enabled", Boolean.TRUE);
		props.put(Message.MTOM_ENABLED, "true");

		// UDManager client
		ClientProxyFactoryBean udFactory = new ClientProxyFactoryBean();
		udFactory.setProperties(props);
		udFactory.setServiceClass(UserDataManager.class);
		udFactory.setAddress(UDManagerURL);
//		udFactory.getServiceFactory().setDataBinding(new AegisDatabinding());
		
		udclient = (UserDataManager) udFactory.create();
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(udclient).getConduit();
		
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(600000);
		httpClientPolicy.setReceiveTimeout(0);
//		httpClientPolicy.setCacheControl(ClientCacheControlType.NO_CACHE);
		httpConduit.setClient(httpClientPolicy);
		

		if (UDManagerURL.split(":")[1].substring(2).equals(FTPAddress)) {
			equalServer = true;
		} else {
			equalServer = false;
		}
		
		if(SSL){
        	try {
				setupTLS(udclient);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

	}
	
	private static void setupTLS(UserDataManager port) throws FileNotFoundException, IOException, GeneralSecurityException {

    	HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

		TLSClientParameters tlsCP = new TLSClientParameters();
		KeyStore keyStore = KeyStore.getInstance("JKS");
		String keyStoreLoc = SSLClientPath;
		keyStore.load(new FileInputStream(keyStoreLoc), SSLClientPassword.toCharArray());
		KeyManager[] myKeyManagers = getKeyManagers(keyStore, SSLClientPassword);
		tlsCP.setKeyManagers(myKeyManagers);

		KeyStore trustStore = KeyStore.getInstance("JKS");
		String trustStoreLoc = SSLCAPath;
		trustStore.load(new FileInputStream(trustStoreLoc), SSLCAPassword.toCharArray());
		TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
		tlsCP.setTrustManagers(myTrustStoreKeyManagers);

		// The following is not recommended and would not be done in a
		// prodcution environment,
		// this is just for illustrative purpose
		tlsCP.setDisableCNCheck(true);
		tlsCP.setSecureSocketProtocol("SSL");   // addme
		
		httpConduit.setTlsClientParameters(tlsCP);

	}

	private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
		fac.init(trustStore);
		return fac.getTrustManagers();
	}

	private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
		KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
		fac.init(keyStore, keyPass);
		return fac.getKeyManagers();
	}

	public static void main(String args[]) throws Exception {

		UDClient client = new UDClient();

		
//		client.getFile_Web("sys.sh", "/root/", ".", 0, "p258rsw");
		// System.out.println(UDManagerURL.split(":")[1].substring(2).equals(FTPAddress));
		// for(String a : aa){
		// System.out.println(a);
		// }
//		UUID uid = client.udclient.login(FTPAddress, "p275han", "han7968@@@",1);
		UUID uid = client.udclient.login(FTPAddress, "p258rsw", "kisti4001!@#",0);
//		UUID uid = client.udclient.login(FTPAddress, "p275kdm", "00ppoo99**",1);
//		UUID uid = client.udclient.login(FTPAddress, "htcaas", "htcaas",1);
//		boolean result = client.getFile(uid, "11.cmd", "/phome01/p258rsw/", ".", 0);
//		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ( "yyyy_MM_dd", Locale.KOREA );
//		Date currentTime = new Date ( );
//		String mTime = mSimpleDateFormat.format ( currentTime );
//		boolean result = client.putFile(uid, "build.sh", "/home/htcaas/job/", 1);
//		System.out.println(client.readFile(uid, "/home/htcaas/agent/2013_06_24/agent.108589.log"));
//		System.out.println(client.udclient.calculateServerCheckSum("1LEE.tar.gz", "/usr/local/proteins/"));
//		System.out.println(client.getLocalChecksum("1LEE.tar.gz", "."));
//		System.out.println(client.getLocalChecksum("1LEE.tar.gz", "/usr/local/proteins/"));
//		 UUID uid = client.udclient.login(FTPAddress, "htcaas","htcaas",0);
//		 boolean result = client.getFile(uid, "aaa", "/phome01/p258rsw/", ".", 0);
//		 boolean result = client.getFile_D("test", "/home/p258rsw/", ".", 0);
//		boolean result = client.putFile(uid, "test_2M", "/htcaas/p258rsw/", 1);
//		boolean result = client.putFile_Web("test_10M", "/htcaas/p258rsw/",  0);
//		boolean result = client.putFile_Web("test_1M", "/htcaas/p258rsw/aaa",0, "p258rsw");
//		String result  = client.readFile(uid, "/htcaas/log/job/2013_09_14/job.4755424.log");
//		FTPFile[] aa = client.udclient.getFolderList(uid, "/htcaas/p258rsw/koh/pockets");
		FTPFile[] aa = client.udclient.getFileListFilter(uid, "/htcaas/p258rsw/koh/pockets/1abn", "pdbqt");
		for(FTPFile t : aa){
		  System.out.println(t.getName());
		}
		// boolean result = client.getFileDiff(uid1, "helloworld.sh",
		// "/home/seungwoo/application/helloworld/", ".",0);
		// FTPFile[] list = client.udclient.getAllList(uid1, ".");
		// for(FTPFile aa : list){
		// System.out.println(aa.getName());
		// }

		// System.out.println(result);
		// client.putFile(uid1, "test", "/home/seungwoo/test", 0);
		// client.udclient.logout(uid1,0);
		//
//		client.udclient.logout(uid,1);
		// client.udclient.

		// Current directory (.) is not usable. Absolute directory is needed.
		// System.out.println(client.getFile(uid1, "rmSVN", "/home/seungwoo",
		// "."));

		 client.udclient.logout(uid);

		// client.putFile("/home/shlee/install.sh", "/home/shlee/userdata");
		// client.getFile("install.sh", "/home/shlee/userdata", "/home/shlee");

	}

	// private class MultiThread extends Thread {
	//
	// private UDClient ud;
	//
	// public MultiThread(UDClient ud) {
	// this.ud = ud;
	// }
	//
	// public void run() {
	//
	// while (true) {
	// logger.info("Threads name" + this.currentThread().toString());
	//
	// // UUID uid = ud.udclient.login("pearl.kisti.re.kr", "seungwoo",
	// "shtmddn");
	// // ud.getFileDiff(uid, "rmSVN", "/home/seungwoo", ".");
	// // ud.getFileDiff(uid, "test.zip", "/home/seungwoo", ".");
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// // ud.udclient.logout(uid);
	//
	// }
	// }
	//
	// }

	public void putFolder(UUID uid, String localdir, String remotedir, int agentId) {
		logger.info("PutFolder");

		File ldir = new File(localdir);

		if (!udclient.changeWD(uid, remotedir, agentId)) {
			udclient.createFolder(uid, remotedir, agentId);
			udclient.changeWD(uid, remotedir, agentId);
		} else
			udclient.changeWD(uid, remotedir, agentId);

		udclient.createFolder(uid, ldir.getName(), agentId);
		udclient.changeWD(uid, ldir.getName(), agentId);

		String[] lname = ldir.list();

		DataHandlerFile dhfile = new DataHandlerFile();
		DataSource source = null;
		DataHandler handler = null;

		for (String name : lname) {
			File fullname = null;
			if (!localdir.endsWith(File.separator))
				fullname = new File(localdir + File.separator + name);
			else
				fullname = new File(localdir + name);

			if (fullname.isDirectory()) {
				try {

					// udmanager.createFolder(fullname.getName());
					// udmanager.changeWD(fullname.getName());
					String temp = udclient.printWD(uid);
					putFolder(uid, fullname.getCanonicalPath(), temp, agentId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}
			} else if (fullname.isFile()) {
				System.out.println(udclient.printWD(uid));
				source = new FileDataSource(fullname);
				handler = new DataHandler(source);
				dhfile.setDfile(handler);
				dhfile.setName(handler.getName());
				String temp2 = udclient.printWD(uid);

				udclient.putFileData(uid, dhfile, temp2, agentId);

			}
		}

		udclient.changeToParentWD(uid);

	}

	public boolean putFile(UUID uid, String fullname, String remotedir, int agentId) {
	  logger.info("PutFile " + fullname + ", " + remotedir);

		DataHandlerFile dhfile = new DataHandlerFile();
		DataSource source = new FileDataSource(new File(fullname));
		DataHandler dh = new DataHandler(source);
		dhfile.setDfile(dh);
		dhfile.setName(dh.getName());

		return udclient.putFileData(uid, dhfile, remotedir, agentId);
	}

	public boolean putFile_Web(String fullname, String remotedir, int agentId) {
    logger.info("PutFile_Web " + fullname + ", " + remotedir);

    DataHandlerFile dhfile = new DataHandlerFile();
    DataSource source = new FileDataSource(new File(fullname));
    DataHandler dh = new DataHandler(source);
    dhfile.setDfile(dh);
    dhfile.setName(dh.getName());
    
    return udclient.putFileData_Web(dhfile, remotedir, agentId);
  }
	
	public boolean putFile_Web(String fullname, String remotedir, int agentId, String userId) {
    logger.info("PutFile_Web " + fullname + ", " + remotedir);

    DataHandlerFile dhfile = new DataHandlerFile();
    DataSource source = new FileDataSource(new File(fullname));
    DataHandler dh = new DataHandler(source);
    dhfile.setDfile(dh);
    dhfile.setName(dh.getName());

    return udclient.putFileData_Web(dhfile, remotedir, agentId, userId);
  }
	
	public boolean putFile(UUID uid, String fullname, String remotedir, String remoteName, int agentId) {
		logger.info("PutFile " + fullname + ", " + remotedir + ", " + remoteName);

		DataHandlerFile dhfile = new DataHandlerFile();
		DataSource source = new FileDataSource(new File(fullname));
		DataHandler dh = new DataHandler(source);
		dhfile.setDfile(dh);
		dhfile.setName(remoteName);

		return udclient.putFileData(uid, dhfile, remotedir, agentId);
	}
	
	public boolean putFile_Web(String fullname, String remotedir, String remoteName, int agentId) {
    logger.info("PutFile_Web " + fullname + ", " + remotedir + ", " + remoteName);

    DataHandlerFile dhfile = new DataHandlerFile();
    DataSource source = new FileDataSource(new File(fullname));
    DataHandler dh = new DataHandler(source);
    dhfile.setDfile(dh);
    dhfile.setName(remoteName);

    return udclient.putFileData_Web(dhfile, remotedir, agentId);
  }

	public boolean getFile(UUID uid, String fname, String remotedir, String localdir, int agentId) {
		logger.info("getFile eqaulServer :" + equalServer);

		boolean result = false;
		DataHandler dh = null;
		File dir = new File(localdir);
		if (!dir.exists())
			dir.mkdirs();
		// System.out.println(udclient.printWD(uid));
		if (equalServer) {
			dh = udclient.getFileData(uid, fname, remotedir, agentId);
		} else {
			dh = udclient.getFileDataDiff(uid, fname, remotedir, agentId);
		}

		try {
			if (dh != null) {
				FileOutputStream outputStream = null;
				if (localdir.endsWith(File.separator)) {
					outputStream = new FileOutputStream(localdir + fname);
				} else
					outputStream = new FileOutputStream(localdir + File.separator + fname);

				dh.writeTo(outputStream);
				outputStream.flush();
				outputStream.close();

				result = true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return result;
	}
	
	public boolean getFile_Web(String fname, String remotedir, String localdir, int agentId) {
    logger.info("getFile_Web eqaulServer :" + equalServer);

    boolean result = false;
    DataHandler dh = null;
    File dir = new File(localdir);
    if (!dir.exists())
      dir.mkdirs();
    if (equalServer) {
      dh = udclient.getFileData_Web(fname, remotedir, agentId);
    }else {
      return result;
    }

    try {
      if (dh != null) {
        FileOutputStream outputStream = null;
        if (localdir.endsWith(File.separator)) {
          outputStream = new FileOutputStream(localdir + fname);
        } else
          outputStream = new FileOutputStream(localdir + File.separator + fname);

        dh.writeTo(outputStream);
        outputStream.flush();
        outputStream.close();

        result = true;
      }

    } catch (IOException e) {
      // TODO Auto-generated catch block
      logger.error(e.getMessage());
    }
    return result;
  }
	
	public String readFile(UUID uid, String fullPath) {
		logger.info("getFile eqaulServer :" + equalServer);

		String result = null;
		DataHandler dh = null;

		String[] file = fullPath.split("/");
		String fname = file[file.length-1];
		String remotedir = fullPath.substring(0, fullPath.length()-fname.length());
		if (equalServer) {
			dh = udclient.getFileData(uid, fname, remotedir, 0);
		} else {
			dh = udclient.getFileDataDiff(uid, fname, remotedir, 0);
		}

		try {
			if (dh != null) {

			  StringBuffer out = new StringBuffer();
	      byte[] b = new byte[4096];
	      for (int n; (n = dh.getInputStream().read(b)) != -1;) {
	          out.append(new String(b, 0, n));
	      }
	      
	      result = out.toString();
	      
//	      System.out.println(result);
//				result = IOUtils.toString(dh.getInputStream(), "UTF-8");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return result;
	}
	
	public long getLocalChecksum(String fname, String localdir) {
		logger.info("getLocalChecksum");

		long checksum = 0L;
		File checker = null;

		if (localdir.endsWith(File.separator)) {
			checker = new File(localdir + fname);
		} else {
			checker = new File(localdir + File.separator + fname);
		}

		
		try {
			checksum = ChecksumChecker.getFileChecksum(checker);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		
		return checksum;
	}
	
	public long getServerChecksum(String fname, String remotedir) {
		logger.info("getServerChecksum");

		return udclient.calculateServerCheckSum(fname, remotedir);
	}

	public boolean getFolder(UUID uid, String remotedir, String localdir, int agentId) {
		logger.info("getFolder " + agentId);

		boolean result = true;

		FTPFile[] ftpfile = udclient.getAllList(uid, remotedir);
		if (ftpfile == null) {
			logger.info("No Data");
			return result;
		}

		String fullpath = null;

		for (FTPFile file : ftpfile) {
			if (file.isDirectory()) {
				try {
					// if(localdir.endsWith(File.separator))
					// fullpath = localdir + file.getName();
					// else
					// fullpath = localdir + File.separator + file.getName();

					File localdirFile = new File(localdir, file.getName());

					localdirFile.mkdirs();

					String temp1 = localdirFile.getCanonicalPath();

					if (remotedir.endsWith("/"))
						fullpath = remotedir + file.getName();
					else {
						fullpath = remotedir + "/" + file.getName();
						// udmanager.changeWD(remotedir);
						// fullpath = file.getName();
					}

					getFolder(uid, fullpath, temp1, agentId);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}

			} else if (file.isFile()) {
				boolean temp = getFile(uid, file.getName(), remotedir, localdir, agentId);
				if (temp == false)
					result = temp;

			} else
				logger.info("unknown file type");
		}

		return result;
	}
}

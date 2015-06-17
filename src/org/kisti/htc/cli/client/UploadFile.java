package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.udmanager.bean.DataHandlerFile;
import org.kisti.htc.udmanager.server.UserDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadFile {
	
	final static Logger logger = LoggerFactory.getLogger(UploadFile.class);

	private static String FTPAddress = null;
	private static String fullname = null;
	private static String remotedir = null;
	private static String localdir = null;
	private static String UDManagerURL = null;
	private static UserDataManager client;
	private static String id = null;
	private static Database dbclient;
	private static String DBManagerURL = null;
	private static String address = null;
	private static int total = 0;
	private static int failed = 0;

	/**
	 * @param args
	 */
	static {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

			FTPAddress = prop.getProperty("FTP.Address");

			UDManagerURL = prop.getProperty("UDManager.Address");
			DBManagerURL = prop.getProperty("DBManager.Address");

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("mtom-enabled", Boolean.TRUE);

			ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
			factory.setProperties(props);
			factory.setServiceClass(UserDataManager.class);
			factory.setAddress(UDManagerURL);

			client = (UserDataManager) factory.create();

			HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(client).getConduit();
			HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
			httpClientPolicy.setConnectionTimeout(600000);
			httpClientPolicy.setReceiveTimeout(0);
//			httpClientPolicy.setCacheControl(ClientCacheControlType.NO_CACHE);
			httpConduit.setClient(httpClientPolicy);

			// prepare DBManager client
			ClientProxyFactoryBean factory2 = new ClientProxyFactoryBean();
			factory2.setServiceClass(Database.class);
			factory2.setAddress(DBManagerURL);
			factory2.setDataBinding(new AegisDatabinding());
			dbclient = (Database) factory2.create();

		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		// TODO Auto-generated method stub

		UploadFile uf = new UploadFile();

		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		// options.addOption("a", true, "FTP address(optional)");
		options.addOption("i", "id",true, "user id");
		options.addOption("f", "file", true, "Local absolute file name");
		options.addOption("r", "R", true, "Remote directory");
		options.addOption("l", "L", true, "Local directory");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
//				formatter.printHelp("htcaas-file-put \t Upload file to remote place", options);
				String help= "htcaas-file-put [OPTIONS] ";
				String syn= "\nhtcaas-file-put [-f < file name> ] [-r <remote location>] [-i <username>]";
				syn+= "\nhtcaas-file-put [-l <local directory> ] [-r <remote location>] [-i <username>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Upload file/dir to remote place\n", options, syn);
				System.exit(0);
			} else {
				if (cmd.hasOption("i")) {
					id = cmd.getOptionValue("i");
				}
				if (cmd.hasOption("f")) {
					fullname = cmd.getOptionValue("f");
				}
				if (cmd.hasOption("r")) {
					remotedir = cmd.getOptionValue("r");
				}
				if (cmd.hasOption("l")) {
					localdir = cmd.getOptionValue("l");
				}
			}

			// if ((fullname == null || fullname.startsWith("$") ||
			// fullname.isEmpty())){
			// System.out.println("-f need");
			// System.exit(1);
			if (remotedir == null || remotedir.isEmpty() || remotedir.startsWith("$")) {
				logger.error("need -r option and value");
				System.exit(1);
			} else if (id == null || id.isEmpty() || id.startsWith("$")) {
				logger.error("need -i option and value");
				System.exit(1);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}

//		if (fullname == null) {
//			System.out.println("Transfering folder : " + localdir);
//		} else {
//			System.out.println("Transfering file : " + fullname);
//		}

		UUID uid = null;
		boolean ret = false;
		// if(!localdir.startsWith("/")){
		// localdir = wd+"/"+localdir;
		// System.out.println("localdir :" + localdir);
		// }
		try {
			uid = client.login(address, id, dbclient.getUserPasswd(id));
			if (fullname == null) {
				ret = uf.putFolder(uid, localdir, remotedir);
			} else {
				ret = uf.putFile_Web(fullname, remotedir, localdir, id);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			client.logout(uid);
		}

		if (!ret) {
			System.out.println("Result :" + ret);
			System.out.println("Total : " + total + ", Failed : " + failed);
		}

		// System.out.println("Transfering file : " + fullname);
		// boolean ret = uf.putFile_Web(fullname, remotedir,localdir, id);
		// System.out.println("Result :" + ret );

	}

	public boolean putFile_Web(String fullname, String remotedir, String localdir, String userId) {
		// System.out.println("PutFile_Web " + fullname + ", " + remotedir +", "
		// + localdir);

		boolean ret = false;
		if (fullname.contains("/")) {
			DataHandlerFile dhfile = new DataHandlerFile();
			DataSource source = new FileDataSource(new File(fullname));
			DataHandler dh = new DataHandler(source);
			dhfile.setDfile(dh);
			dhfile.setName(dh.getName());

			ret = client.putFileData_Web(dhfile, remotedir, -1, userId);
			// client.changeFileOwn(userId, remotedir);
		} else {
			
//			if (localdir == null) {
//				File file = new File(fullname);
//				
//				System.out.println(file.getAbsolutePath());
//				try {
//					System.out.println(file.getCanonicalPath());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			String fName = localdir + "/" + fullname;
//			// System.out.println("---------"+fName);
//			DataHandlerFile dhfile = new DataHandlerFile();
//			DataSource source = new FileDataSource(new File(fName));
//			DataHandler dh = new DataHandler(source);
//			dhfile.setDfile(dh);
//			dhfile.setName(dh.getName());
//			
//			ret = client.putFileData_Web(dhfile, remotedir, -1, userId);
			// client.changeFileOwn(userId, remotedir);
				
			}

		return ret;

	}

	public boolean putFolder(UUID uid, String localdir, String remotedir) {
		// System.out.println("PutFolder");

		boolean ret = true;
		File ldir = new File(localdir);

		if (!client.changeWD(uid, remotedir)) {
			client.createFolder(uid, remotedir);
			client.changeWD(uid, remotedir);
		} else
			client.changeWD(uid, remotedir);

		client.createFolder(uid, ldir.getName());
		client.changeWD(uid, ldir.getName());

		String[] lname = ldir.list();

		// DataHandlerFile dhfile = new DataHandlerFile();
		// DataSource source = null;
		// DataHandler handler = null;

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
					String temp = client.printWD(uid);
					ret = putFolder(uid, fullname.getCanonicalPath(), temp);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
			} else if (fullname.isFile()) {
				total++;
				DataHandlerFile dhfile = new DataHandlerFile();
				DataSource source = new FileDataSource(fullname);
				DataHandler dh = new DataHandler(source);
				dhfile.setDfile(dh);
				dhfile.setName(dh.getName());
				//
				String temp2 = client.printWD(uid);

				boolean ret2 = client.putFileData_Web(dhfile, temp2.split("\"")[1], -1, id);
				if (!ret2) {
					System.out.println("Failed :" + fullname);
					failed++;
					ret = false;
				}
				// client.changeFileOwn(id, temp2);
			} else {
				System.out.println("Unknown File Type");
			}

		}

		client.changeToParentWD(uid);

		return ret;
	}

}

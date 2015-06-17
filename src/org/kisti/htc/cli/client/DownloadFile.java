package org.kisti.htc.cli.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.udmanager.server.UserDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class DownloadFile {

	final static Logger logger = LoggerFactory.getLogger(DownloadFile.class);

	private static String remotedir = null;
	private static String localdir = null;
	private static String wd = null;
	private static String UDManagerURL = null;
	private static String DBManagerURL = null;
	private static UserDataManager client;
	private static Database dbclient;
	private static String id = null;
	private static String address = null;

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			UDManagerURL = prop.getProperty("UDManager.Address");
			DBManagerURL = prop.getProperty("DBManager.Address");
			address = prop.getProperty("FTP.Address");
			
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
			e.printStackTrace();
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		// TODO Auto-generated method stub

		DownloadFile uf = new DownloadFile();
		
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		//options.addOption("a", true, "FTP address(optional)");
		options.addOption("i", "id", true, "user id");
		options.addOption("l", "Local directory",true, "Local directory to download files");
		options.addOption("r", "Remote directory",true, "Remote directory or file");
		options.addOption("w", "Working directory",true, "Current working directory");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
				//formatter.printHelp("htcaas-file-get -r {remote} -l {local} -i {id} \t Download files to local directory", options);
				String help= "htcaas-file-get [OPTIONS] ";
				String syn= "\nhtcaas-file-get [-l < filename with absolute localpath> ] [-r <file name with remote location>] [-i <username>]";
				syn += "\nhtcaas-file-get [-l < dir with absolute localpath> ] [-r <file name with remote location>] [-i <username>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Download files/dirs from server to local directory\n", options, syn);
				System.exit(0);
			} else {
				if (cmd.hasOption("i")){
					id = cmd.getOptionValue("i");
				}
				if (cmd.hasOption("r")){
					remotedir = cmd.getOptionValue("r");
				}
				if (cmd.hasOption("l")){
					localdir = cmd.getOptionValue("l");
			}
				
				if (cmd.hasOption("w")){
					wd = cmd.getOptionValue("w");
				}
			}
			
			if ((localdir == null || localdir.startsWith("$") || localdir.isEmpty())){
				logger.error("need -l option and value");
				System.exit(1);
			} else if (remotedir == null || remotedir.isEmpty() || remotedir.startsWith("$")){
				logger.error("need -r option and value");
				System.exit(1);
			} else if (id == null || id.isEmpty() || id.startsWith("$")){
				logger.error("need -i option and value");
				System.exit(1);
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}
		
		
//		System.out.println("Transfering file : " + remotedir);
		UUID uid =  null;
		boolean ret = false;
		if(!localdir.startsWith("/")){
			localdir = wd+"/"+localdir;
//			System.out.println("localdir :" + localdir);
		}
		try{
			uid = client.login(address, id, dbclient.getUserPasswd(id));
			ret = uf.getFolder(uid, remotedir, localdir);
		}catch(Exception e){
			System.out.println(e.toString());
		}finally{
			client.logout(uid);
		}
		
		if(!ret){
			System.out.println("Result :" + ret );
		}

		
	}
	
	public boolean getFolder(UUID uid, String remotedir, String localdir) {
//		System.out.println("getFolder ");

		boolean result = true;

		FTPFile[] ftpfile = null;
		ftpfile = client.getAllList(uid, remotedir);

		if (ftpfile == null) {
			File file = new File(remotedir);

			int tmp = remotedir.lastIndexOf("/");
			
			int tmp_loc= localdir.lastIndexOf("/");
		//	System.out.println(localdir.substring(0,tmp_loc));
			
//		 	result = getFile_Web(file.getName(), remotedir.substring(0,tmp), localdir);
			result = getFile_Web(file.getName(), remotedir.substring(0,tmp), localdir.substring(0,tmp_loc));
			
			if(!result){
				logger.error("There are no Files or failed to download");
			}
			
			return result;
		}
		
		

		String fullpath = null;

		for (FTPFile file : ftpfile) {
			if (file.isDirectory()) {
				try {

					File localdirFile = new File(localdir, file.getName());

					localdirFile.mkdirs();

					String temp1 = localdirFile.getCanonicalPath();

					if (remotedir.endsWith("/"))
						fullpath = remotedir + file.getName();
					else {
						fullpath = remotedir + "/" + file.getName();
					}

					getFolder(uid, fullpath, temp1);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}

			} else if (file.isFile()) {
				boolean temp = getFile_Web(file.getName(), remotedir, localdir);
				if (temp == false)
					result = temp;
			} else if (file.isSymbolicLink()) {
				boolean temp = getFile_Web(file.getName(), remotedir, localdir);
				if (temp == false)
					result = temp;
			} else
				logger.error("unknown file type");
		}

		return result;
	}
	
	public boolean getFile_Web(String fname, String remotedir, String localdir) {
//	    System.out.println("getFile_Web");

	    boolean result = false;
	    DataHandler dh = null;
	    File dir = new File(localdir);
	    if (!dir.exists())
	      dir.mkdirs();

	    dh = client.getFileData_Web(fname, remotedir, -1);

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

}

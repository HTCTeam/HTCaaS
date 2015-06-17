package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.net.ftp.FTPFile;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ListFile {

	final static Logger logger = LoggerFactory.getLogger(ListFile.class);

	private static UDClient udc = new UDClient();
	private static String FTPAddress = null;
	private static String id = null;
	private static String passwd = null;
	private static String remotedir = null;
	private static String default_dir = "/phome01/";

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			FTPAddress = prop.getProperty("FTP.Address");
			//logger.info("FTPAddress: {}", FTPAddress);
			
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {



		Options options = new Options();
		options.addOption("h","help", false, "print help");
//		options.addOption("a", true, "FTP address(optional)");
//		options.addOption("i", true, "FTP id");
		options.addOption("p","pwd", true, "FTP password");
//		options.addOption("f", true, "Local file name");
//		options.addOption("d", true, "Local directory");
		options.addOption("r","R", true, "Remote directory");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
//				formatter.printHelp("htcaas-file-list -p {passwd} -r {remote} \t  List files on the remote server", options);
				String help= "htcaas-file-list [OPTIONS] ";
				String syn= "\nhtcaas-file-list [-p or pwd <FTP password> ] [-r <remote directory>]  ";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Lists an inputted directory's contents on server\n", options, syn);
				System.exit(0);
			} else {
//				if(cmd.hasOption("a")) {
//					FTPAddress = cmd.getOptionValue("a");
//					System.out.println("FTP address:"+FTPAddress);
//				} 
				if (cmd.hasOption("i")){
					id = cmd.getOptionValue("i");
//					System.out.println("id:"+id);
				}
				if (cmd.hasOption("p")){
					passwd = cmd.getOptionValue("p");
//					System.out.println("passwd:"+passwd);
				}

				if (cmd.hasOption("r")){
					remotedir = cmd.getOptionValue("r");
//					System.out.println("remotedir:"+remotedir);
				}
			}
			
//			id = "p336nsh";
//			passwd= "skatngus2!";
			
			try {
				if (id == null || id.isEmpty()) {
					String command = "whoami";
					ProcessBuilder builder = new ProcessBuilder(command);

					Process p;
					try {
						p = builder.start();
						int exitValue = p.waitFor();

						if (exitValue == 0) {
							BufferedReader br = new BufferedReader(
									new InputStreamReader(p.getInputStream()));
							String line;
							while ((line = br.readLine()) != null) {
								id = line;
							}
							br.close();

							if (id == null) {
								logger.error("host not found");
								throw new Exception("host error");
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage());
					}
				}
			} catch (Exception e) {
//				logger.error(e.toString());
				logger.error(e.getMessage());
			}
			
			if(FTPAddress==null || FTPAddress.isEmpty()){
				FTPAddress = "150.183.158.172";
			}
			
			if (passwd == null || passwd.isEmpty()){				
				logger.error("need -p option and value(FTP passwd)");
				System.exit(1); 
				
				// input by keyboard
//			} else if ((fullname == null || fullname.startsWith("$") || fullname.isEmpty()) && (localdir == null || localdir.startsWith("$") || localdir.isEmpty())){
//					logger.info("-f or -d need");
//					System.exit(1);
//			} else if(fullname != null && localdir != null){
//				logger.info("One of -f and -d need");
//				System.exit(1);
			} else if (remotedir == null || remotedir.isEmpty() || remotedir.startsWith("$")){
//				logger.info("-r need remote directory");
//				System.exit(1);
				remotedir = "/pwork01/"+id+"/";
				System.out.println("remote dir:"+remotedir);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}
		

		
		List<String> arrDir = new ArrayList<String>();
		List<String> arrFile = new ArrayList<String>();
		
		UUID uid= null;
		uid = udc.udclient.login(FTPAddress, id, passwd);
		
		try{
			
			FTPFile [] afile = udc.udclient.getAllList(uid, remotedir);
			
			if( afile.length != 0) {
				System.out.println("");
			}
			
			for (FTPFile file : afile) {
				if (file.getType() == FTPFile.DIRECTORY_TYPE) {
					arrDir.add(file.getName());
				}else if (file.getType() == FTPFile.FILE_TYPE) {
					arrFile.add(file.getName());
				}else {
					System.out.println("Unknown Type: \t"+ file.getName());
				}
			}
			
			for (int i =0; i <arrDir.size(); i++) {
				System.out.println("[Directory]: \t"+ arrDir.get(i));
			}
			for (int i =0; i <arrFile.size(); i++) {
				System.out.println("[File]: \t"+ arrFile.get(i));
			}
			
			//udc.get
			
		}catch (NullPointerException e){
 
			System.out.println("File Size: 0");
		}

 

//		if(result == true){
//			logger.info("Upload file success!");
//		}else{
//			logger.info("Upload file failed!");
//		}
		if(uid!=null){
		udc.udclient.logout(uid);
		}
		
	}

}

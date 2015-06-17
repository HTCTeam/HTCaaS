
package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.jobmanager.server.JobManager;
import org.kisti.htc.monitoring.server.Monitoring;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetJobResultAutodock {

	final static Logger logger = LoggerFactory.getLogger(GetJobResultAutodock.class);

	private static JobManager jobclient;
	private static Monitoring monitoring;
	private static String MonitoringURL;
	private static String UDManagerURL;
	private static String JobManagerURL;
	private static String DBManagerURL = null; 
	private static Database dbclient;
	private static UDClient udc = new UDClient();;
	private static String MetaJobId = null;
	private static String high = null;
	private static String low = null;
	private static String User = null;
	private static String Pw = null;
	private static String wd = null;
	private static String localdir = null;
	public static boolean equalServer = false;
	static ProgressBar progress = new ProgressBar();
	private static String FTPAddress = null;
	
 

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			MonitoringURL = prop.getProperty("Monitoring.Address");
			JobManagerURL = prop.getProperty("JobManager.Address");
			UDManagerURL = prop.getProperty("UDManager.Address");
			DBManagerURL = prop.getProperty("DBManager.Address");
			FTPAddress = prop.getProperty("FTP.Address");
			
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
		
		if (UDManagerURL.split(":")[1].substring(2).equals(FTPAddress)) {
			equalServer = true;
		} else {
			equalServer = false;
		}

		try {
			if (User == null) {
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
							User = line;
						}
						br.close();

						if (User == null) {
							logger.error("host not found");
							throw new Exception("host error");
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
	
	

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("h","help", false, "print help");
		options.addOption("m", "M", true, "Metajob id");  // MetaJobId 
		options.addOption("hi", "High", true, "High level energy");
		options.addOption("lo", "Low", true, "Low level energy"); 
		options.addOption("d", "Local Directory", true, "local directory");
		options.addOption("w", "working directory", true, "working directory");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
				String help= "htcaas-get-result-autodock [OPTIONS] ";
				String syn= "\nhtcaas-get-result [-m <metajob id> ] [-h <high level>] [-l <low level>] [-d <local dir>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Get a job result for autodock\n", options, syn);
				System.exit(0);
			} else {
				if(cmd.hasOption("m")) {
					MetaJobId = cmd.getOptionValue("m");
//					System.out.println("metaJobId:"+MetaJobId);
				} 
				 	
				if(cmd.hasOption("hi")) {
					high = cmd.getOptionValue("hi");
				} 

				if(cmd.hasOption("lo")) {
					low = cmd.getOptionValue("lo");
				} 

				if(cmd.hasOption("d")) {
					localdir = cmd.getOptionValue("d");
				}
				
				if(cmd.hasOption("w")) {
					wd = cmd.getOptionValue("w");
				}
			}

			if (MetaJobId == null || MetaJobId.startsWith("$") || MetaJobId.isEmpty()){
				logger.error("-m : Need Argument(MetaJob id)");
				System.exit(1);
			}
			
			if (high == null || high.startsWith("$") || high.isEmpty()){
				logger.error("-hi: Need Argument(high level), e.g., -hi 1");
				System.exit(1);
			}
			
			if (low == null || low.startsWith("$") || low.isEmpty()){
				logger.error("-lo: Need Argument(low level), e.g., -lo -1");
				System.exit(1);
			}
			
		} catch (Exception e) {
			
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}
		// prepare DBManager client
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Database.class);
		factory.setAddress(DBManagerURL);
		factory.setDataBinding(new AegisDatabinding());
		dbclient = (Database) factory.create();
		
		ClientProxyFactoryBean factory2 = new ClientProxyFactoryBean();
		factory2.setServiceClass(JobManager.class);
		factory2.setAddress(JobManagerURL);
		factory2.setDataBinding(new AegisDatabinding());
		jobclient = (JobManager) factory2.create();
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(jobclient).getConduit();
		
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(600000);
		httpClientPolicy.setReceiveTimeout(0);
//		httpClientPolicy.setCacheControl(ClientCacheControlType.NO_CACHE);
		httpConduit.setClient(httpClientPolicy);
		
		ClientProxyFactoryBean factory3 = new ClientProxyFactoryBean();
		factory3.setServiceClass(Monitoring.class);
		factory3.setAddress(MonitoringURL);
		factory3.setDataBinding(new AegisDatabinding());
		monitoring = (Monitoring) factory3.create();
		
		
		Pw = dbclient.getUserPasswd(User);
		
		if(localdir!=null && !localdir.isEmpty() && !localdir.startsWith("/")){
			localdir = wd+"/"+localdir;
		}else{
			localdir = wd;
		}
		
		List<Integer> jobIdList = new ArrayList<Integer>();
		UUID uid= null;
		
        try {

        	int hi = Integer.parseInt(high);
        	int lo = Integer.parseInt(low);
        	int nu = 0 ;
        	///////////////////
        	//FIXME 
        	String adminUser = "htcaas";
        	String adminPw = "htcaas";
        	//////////////////
	
   			uid = udc.udclient.login(FTPAddress, adminUser, adminPw);

        	int mid = Integer.parseInt(MetaJobId);
              System.out.println("Getting specific job id...");
              List<Integer> idList = monitoring.getJobIdListByStatus(mid, JobConstant.JOB_STATUS_DONE);
              int i = 1;
              for (Integer id : idList) {
                String jobLog = monitoring.getJobLog(id);
                String result = null;
                try{
                	result = udc.readFile(uid, jobLog);
                }catch(NullPointerException e){
                	System.out.println("ID" + id +  "is null.");
                	nu++;
                	continue;
                }
                if(result.isEmpty()){
                	System.out.println("ID" + id +  "is empty.");
                	nu++;
                	continue;
                }
                BufferedReader br = new BufferedReader(new StringReader(result));
                String s;
                try {
                  while ((s = br.readLine()) != null) {

                    if (s.contains("Mean_Energy")) {
                      String[] tmp = s.split("=");
                      double meanEnergy = Double.parseDouble(tmp[1].trim());
                      if ((meanEnergy >= lo) && (meanEnergy <= hi)) {
                        System.out.println("Found " + i);
                        System.out.println("JobID : " + id + ", Mean_Energy : " + meanEnergy);
                        jobIdList.add(id);
                        i++;
                      }

                      break;
                    }
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
        	
//        	jobIdList.add(535876);

              System.out.println("Call getJobResultAutodock");
              getJobResultAutodock("down", "Meta", jobIdList, lo, hi);
              System.out.println("Total " + nu + " ligands was failed");


        } catch (NumberFormatException e) {
          logger.error("The format of the metaJobId is wrong.");
          System.out.println("htcaas-get-result-autodock -m {metajobId} -lo {the lowest energyLv} -hi {the highest energyLv})       get the autodock job result of a metajob.");
        } finally {
        	if(uid != null){
        		udc.udclient.logout(uid);
        	}
        }
		

		
	}
	
	 public static void getJobResultAutodock(String status, String divJob, List<Integer> jobIdList, int low, int high) {
		    List<String> jobResult = null;
		    
		    try {
		      System.out.println("Getting the output lists of ligands...");
		      
//		      User = "p258rsw";
//		      Pw = "kisti4001!@#";
		      
		      jobResult = jobclient.getJobIdListResult(User, jobIdList);
		      
		      if (jobResult.size() > 0) {
		        try {
		          if (status.equals("down")) {
		            List<Map<String, String>> getFileList = new ArrayList<Map<String, String>>();
		            System.out.println("Putting the output lists of ligands in fileInfo list...");
		            for (String list : jobResult) {
//		              System.out.print(list + "\t");
		              Map<String, String> fileInfo = new HashMap<String, String>();
		              fileInfo.put("filedir", list);
//		              System.out.println("filedir:" + list);
		              fileInfo.put("localdir", localdir);
//		              System.out.println("localdir:" + localdir);
		              getFileList.add(fileInfo);
		              
		            }
		            System.out.println("Downloadding the output of ligands...");
		            
		            UUID uid = null;
		            try{
		            	uid =  udc.udclient.login(FTPAddress, User, Pw);
		            	createLocalFile(uid, "", getFileList, localdir);
		            }finally{
		            	if(uid != null){
		            		udc.udclient.logout(uid);
		            	}
		            }
		            System.out.println("Finished...");
		            System.out.println();

		          } else if(status.equals("list")) {
		            logger.info("Creating the output lists of the specific ligands...");
		            StringBuilder sb = new StringBuilder();
		            sb.append("The output lists of specific ligands : Low " + low + ", High " + high +", Size " + jobResult.size());
		            sb.append("\n");
		            for (String list : jobResult) {
		              System.out.println(list);
		              sb.append(list);
		              sb.append("\n");
		            }
		            FileWriter fw = null;
		            String fn = "Output_"+low+"_"+high+".list";
		              try {

		                // file write
		                fw = new FileWriter(fn);
		                BufferedWriter bw = new BufferedWriter(fw);

		                bw.write(sb.toString());
		                bw.close();
		                fw.close();

		              } catch (FileNotFoundException e1) {
		                e1.printStackTrace();
		                return;
		              } catch (Exception e) {
		                logger.error("Ligand list File read/write error: " + e.getMessage());
		                e.printStackTrace();
		              }
		              logger.info("");
		              logger.info("Finished creating the output list file : " + fn);
		          }else{
		              logger.info("The list is completed as follows:");
		              for (String list : jobResult) {
		                System.out.println(list);
		            }
		          } // if - else
		        } catch (NullPointerException e) {
		          logger.error("There are no result files. Please check the files");
		        }
		      } // if
		    } catch (NullPointerException e) {
		      logger.error("There are no job results. Please check the metajob results");
		    }
		  } // getJobResult

	 public static boolean createLocalFile(UUID uid, String fname, List<Map<String, String>> getFileList, String localdir) {

//		 System.out.println("fname:" + fname);
//		 System.out.println("localdir :" + localdir);
			boolean result = false;
			String relocal = "";
			String remotedir = "";
			// String temp = remotedir;
			for (int i = 0; i < getFileList.size(); i++) {
				String[] addr = getFileList.get(i).get("filedir").toString().split("/");
				if (addr.length > 2) {
					fname = addr[addr.length - 1];
					remotedir = getFileList.get(i).get("filedir").toString()
							.substring(0, getFileList.get(i).get("filedir").toString().length() - (fname.length() + 1));
					relocal = getFileList.get(i).get("localdir").toString();
				}
				DataHandler dh;
				if (equalServer) {
					dh = udc.udclient.getFileData(uid, fname, remotedir, 0);
				} else {
					dh = udc.udclient.getFileDataDiff(uid, fname, remotedir, 0);
				}
				try {
					if (dh != null) {
						FileOutputStream outputStream = null;
						if (localdir.endsWith(File.separator)) {
							outputStream = new FileOutputStream(relocal + fname);
						} else {
							outputStream = new FileOutputStream(relocal + File.separator + fname);
						}

						dh.writeTo(outputStream);
						outputStream.flush();
						// -----------------
						progress.printProgBar((i + 1) * 100 / getFileList.size(), fname, (i + 1) + "/" + getFileList.size());
						outputStream.close();
						result = true;
					} else {
						logger.warn("Please check the path to a remote folder");
						udc.udclient.logout(uid);
					}

				} catch (IOException e) {
					logger.error("Failed to download.");
					logger.error(e.getMessage());
				} // try - catch
			}
			return result;
		}
}

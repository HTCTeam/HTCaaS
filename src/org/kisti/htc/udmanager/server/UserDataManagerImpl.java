package org.kisti.htc.udmanager.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.constant.MetaJobConstant;
import org.kisti.htc.udmanager.bean.DataHandlerFile;
import org.kisti.htc.udmanager.server.ChecksumChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataManagerImpl implements UserDataManager {

	private static final Logger logger = LoggerFactory.getLogger(UserDataManagerImpl.class);
	private static ConcurrentHashMap<UUID, FTPClient> clientMap;
	private static String CLUSTER_QUEUE = "dteam";
	
	// private FTPClient client; // FtpService client
	int reply = 0;

	public UserDataManagerImpl() {
		// FtpService client
		clientMap = new ConcurrentHashMap<UUID, FTPClient>();
		
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CLUSTER_QUEUE = prop.getProperty("CLUSTER_QUEUE");

	}

	@Override
	public UUID login(String address, String id, String passwd) {
		logger.info("login : {}");
		FTPClient client = new FTPClient();
		UUID uid = UUID.randomUUID();
		clientMap.put(uid, client);
		try {

			client.connect(address, 50021);
			reply = client.getReplyCode();

			logger.debug("getRplyCode ack : reply " + reply);
			if (!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
			}
			if (!client.login(id, passwd)) {
				client.logout();
				client.disconnect();
			}

			client.setFileType(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.setKeepAlive(false);

		} catch (Exception e) {
			logger.error("Failed to connect ftpServer: {}", e.getMessage());
			logger.error("Login failed! ID : {}", id);
			e.printStackTrace();
			return null;
		}

		logger.info("Login Success! ID : {}", id);
		return uid;
	}

	@Override
	public UUID login(String address, String id, String passwd, int agentId) throws SocketTimeoutException {
		logger.info("login : aid {}", agentId);
		FTPClient client = new FTPClient();
		UUID uid = UUID.randomUUID();
		clientMap.put(uid, client);
		try {

			client.connect(address, 50021);
			reply = client.getReplyCode();
			logger.debug("getRplyCode ack : reply " + reply + ", aid {}", agentId);
			if (!FTPReply.isPositiveCompletion(reply)) {
				logger.error("PositiveCompletion false : reply " + reply + ", aid {}", agentId);
				client.disconnect();
				throw new Exception("positiveCompletion error");
			}

			if (!client.login(id, passwd)) {
				logger.error("Inner login fail : aid " + agentId);
				client.logout();
				client.disconnect();
				throw new Exception("Inner login fail");
			}

			client.setFileType(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.setKeepAlive(false);

		} catch (Exception e) {
			logger.error("Failed to connect ftpServer: {}", e.getMessage());
			logger.error("Login failed! : aid " + agentId + ", {}", id);
			e.printStackTrace();
			return null;
		}

		logger.info("Login Success! : aid " + agentId + ", {}", id);
		return uid;
	}

	@Override
	public boolean changeFileOwn(String uid, String filePath) {
		logger.info("ChangeFileOwn : " + uid + " " + filePath);

		String userNum = null;
		String groupNum = null;
		
		try {
			List<String> command = new ArrayList<String>();
			command.add("id");
			command.add(uid);
			command.add("-u");
			// command.add(filePath);

			logger.info("" + command);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File("."));

			Process p = builder.start();
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				logger.info("| check id number - Success");

				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				userNum = br.readLine();
				br.close();

			} else {
				StringBuffer sb = new StringBuffer();

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				throw new Exception("check id number - Error");
			}
			
			command.clear();
			command.add("id");
			command.add(uid);
			command.add("-g");
			
			logger.info("" + command);
			
			builder = new ProcessBuilder(command);
			builder.directory(new File("."));

			p = builder.start();
			exitValue = p.waitFor();

			if (exitValue == 0) {
				logger.info("| check gid number - Success");
				
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				groupNum = br.readLine();
				br.close();

			} else {
				StringBuffer sb = new StringBuffer();

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				throw new Exception("check gid number - Error");
			}
			
			

			command.clear();
			command.add("chown");
			command.add("-R");
			
//			if(CLUSTER_QUEUE == "NONE"){
				command.add(userNum + ":" + groupNum);
//			}else{
//				command.add(userNum + ":" + CLUSTER_QUEUE);
//			}
			
			command.add(filePath);

			logger.info("" + command);

			builder = new ProcessBuilder(command);
			builder.directory(new File("."));

			p = builder.start();
			exitValue = p.waitFor();

			if (exitValue == 0) {
				logger.info("| chown success");

			} else {
				StringBuffer sb = new StringBuffer();

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				throw new Exception("chown Error");
			}
		} catch (Exception e) {

			return false;
		}
		return true;
	}

	@Override
	public boolean changeFileExecutableMod(String filePath) {

		logger.info("ChangeFilExecutableMod : " + filePath);

		try {
			List<String> command = new ArrayList<String>();

			command.add("chmod");
			command.add("+x");
			command.add(filePath);

			logger.info("" + command);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File("."));

			Process p = builder.start();
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				logger.info("| chmod success");

			} else {
				StringBuffer sb = new StringBuffer();

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				throw new Exception("chmod Error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());

			return false;
		}
		return true;
	}

	@Override
	public boolean putFileData(UUID uid, DataHandlerFile udfile, String remotedir, int agentId) {
		logger.info("putFileData : {}", agentId);

		InputStream is = null;
		boolean rPath = false;
		boolean result = false;
		FTPClient client = clientMap.get(uid);

		if (!hasFolder(uid, remotedir, agentId)) {
			rPath = createFolder(uid, remotedir, agentId);
		} else
			rPath = true;

		if (rPath) {
			try {
				client.changeWorkingDirectory(remotedir);
				is = udfile.getDfile().getInputStream();
				result = client.storeFile(udfile.getName(), is);
				logger.info("Upload file  : {} ({})", udfile.getName(), result);
				is.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("remotedir " + remotedir + " , " + agentId);
				logger.error("failed to upload file : {}", e.getMessage());
				logger.error("result : " + result);
				e.printStackTrace();
			}
		}

		return result;

	}

	@Override
	public boolean putFileData_Web(DataHandlerFile udfile, String remotedir, int agentId) {
		logger.info("putFileData_Web : " + remotedir + ", " + agentId);

		boolean rPath = false;
		boolean result = false;

		File remote = new File(remotedir);
		logger.debug("RemoteDIR exists : " + remote.exists());
		if (!remote.exists()) {
			rPath = remote.mkdirs();
		} else {
			rPath = true;
		}

		if (rPath) {
			try {

				if (udfile != null) {
					FileOutputStream outputStream = null;
					if (remotedir.endsWith(File.separator)) {
						outputStream = new FileOutputStream(remotedir + udfile.getName());
					} else
						outputStream = new FileOutputStream(remotedir + File.separator + udfile.getName());

					udfile.getDfile().writeTo(outputStream);
					outputStream.flush();
					outputStream.close();

					result = true;
					logger.info("Upload file  : " + udfile.getName() + ", " + result);
				} else {
					logger.error("Failed to upload file");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("remotedir " + remotedir + " , " + agentId);
				logger.error("failed to upload file : {}", e.getMessage());
				logger.error("result : " + result);
				e.printStackTrace();
			}

		} else {
			logger.error("Failed to make remote directory");
		}

		return result;

	}

	
	private boolean makeDirectory(File remoteFile, String remoteOri, String userId){
		
		boolean rPath = false;
		
		if(!remoteFile.exists()){
			rPath = makeDirectory(remoteFile.getParentFile(), remoteOri, userId);
		} else {
			String existingDir = remoteFile.getAbsolutePath();
			logger.info("RemoteDir exists : " + existingDir);
			if(!existingDir.equals(remoteOri)){
				logger.info("Creating Directory : " + remoteOri);
				File remoteOriFile = new File(remoteOri);
				rPath = remoteOriFile.mkdirs();
				String remote = remoteFile.getAbsolutePath();
				String temp = remote+"/"+remoteOri.substring(existingDir.length(), remoteOri.length()).split("/")[1];
				changeFileOwn(userId, temp);
			} else {
				logger.info("Same Directory");
				rPath = true;
			}
		}
		
		return rPath;
		
	}
	
	@Override
	public boolean putFileData_Web(DataHandlerFile udfile, String remoteDir, int agentId, String userId) {
		logger.info("putFileData_Web : " + remoteDir + ", " + agentId + ", " + userId);

		boolean rPath = false;
		boolean result = false;
		
		File remoteFile = new File(remoteDir);
		
		rPath = makeDirectory(remoteFile, remoteDir, userId);

		String filePath = null;
		if (rPath) {
			try {
				if (udfile != null) {
					FileOutputStream outputStream = null;
					if (remoteDir.endsWith(File.separator)) {
						filePath = remoteDir + udfile.getName();
						outputStream = new FileOutputStream(filePath);
					} else
						filePath = remoteDir + File.separator + udfile.getName();
					outputStream = new FileOutputStream(filePath);

					udfile.getDfile().writeTo(outputStream);
					outputStream.flush();
					outputStream.close();

					result = true;

					for (int i = 0; i < 5; i++) {
						boolean tmp = changeFileOwn(userId, filePath);

						if (tmp)
							break;
					}

					logger.info("Upload file  : " + udfile.getName() + ", " + result);
				} else {
					logger.error("Failed to upload file");
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("remotedir " + remoteDir + " , " + agentId);
				logger.error("failed to upload file : {}", e.getMessage());
				logger.error("result : " + result);
				e.printStackTrace();
			}

		} else {
			logger.error("Failed to make remote directory");
		}

		return result;

	}

	@Override
	public boolean putFileDataAll(UUID uid, List<DataHandlerFile> fname, String remotedir) {
		logger.info("putFileDataAll");

		InputStream is = null;
		boolean rPath = false;
		boolean result = false;
		FTPClient client = clientMap.get(uid);

		if (!hasFolder(uid, remotedir)) {
			rPath = createFolder(uid, remotedir);
		} else
			rPath = true;

		if (rPath) {
			try {

				client.changeWorkingDirectory(remotedir);
				int i = 1;
				int j = 0;
				for (DataHandlerFile ud : fname) {
					// File uploadFile = new File(localdir, name);
					// fis = new FileInputStream(uploadFile);
					is = ud.getDfile().getInputStream();
					boolean temp = client.storeFile(ud.getName(), is);
					logger.info("[" + i + "]" + " upload file  : " + ud.getName() + "(" + temp + ")");
					if (!temp) {
						j++;
					}

					i++;
				}

				if (j == 0) {
					result = true;
				}

				logger.info(((i - 1) - j) + "/" + (i - 1) + " upload");
				is.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("failed to upload file : " + e.getMessage());
				logger.error("result : " + result);
				e.printStackTrace();
			}
		}

		return result;

	}

	@Override
	public boolean putFileDataAll(UUID uid, List<DataHandlerFile> fname, String remotedir, int agentId) {
		logger.info("putFileDataAll aid" + agentId);

		InputStream is = null;
		boolean rPath = false;
		boolean result = false;
		FTPClient client = clientMap.get(uid);

		if (!hasFolder(uid, remotedir, agentId)) {
			rPath = createFolder(uid, remotedir, agentId);
		} else
			rPath = true;

		if (rPath) {
			try {

				client.changeWorkingDirectory(remotedir);
				int i = 1;
				int j = 0;
				for (DataHandlerFile ud : fname) {
					// File uploadFile = new File(localdir, name);
					// fis = new FileInputStream(uploadFile);
					is = ud.getDfile().getInputStream();
					boolean temp = client.storeFile(ud.getName(), is);
					logger.info("[" + i + "]" + " upload file  : " + ud.getName() + "(" + temp + ")");
					if (!temp) {
						j++;
					}
					i++;
				}

				if (j == 0) {
					result = true;
				}

				logger.info(((i - 1) - j) + "/" + (i - 1) + " upload");
				is.close();

				return result;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("failed to upload file : " + e.getMessage());
				logger.error("result : " + result);
				e.printStackTrace();
			}
		}

		return result;

	}

	@Override
	@Deprecated
	public DataHandler getFileDataDiff(UUID uid, String fname, String remotedir, int agentId) {

		logger.info("getFileDataDiff " + fname + ", " + remotedir + agentId);

		FTPClient client = clientMap.get(uid);
		FileOutputStream fos = null;
		File f = null;
		FileDataSource dataSource = null;
		DataHandler dh = null;

		try {
			if (client.changeWorkingDirectory(remotedir)) {

				if (fname.length() <= 2)
					f = File.createTempFile(fname + "000", null);
				else
					f = new File(fname);

				fos = new FileOutputStream(f);

				boolean temp = client.retrieveFile(fname, fos);
				logger.info("download file : " + fname + "(" + temp + ")");
				fos.flush();
				fos.close();

				if (temp) {
					dataSource = new FileDataSource(f);
					dh = new DataHandler(dataSource);
				}

				// f.deleteOnExit();
				// f.delete();

			} else
				logger.info("failed to get filedata. Fault remotedir : " + remotedir);

		} catch (Exception e) { // TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return dh;
	}

	@Override
	public DataHandler getFileData(UUID uid, String fname, String remotedir, int agentId) {
		logger.info("getFileData " + fname + ", " + remotedir + ", " + agentId);

		FTPClient client = clientMap.get(uid);
		FileDataSource dataSource = null;
		DataHandler dh = null;
		String fullname = null;
		try {
			if (client.changeWorkingDirectory(remotedir)) {

				if (!remotedir.endsWith(File.separator))
					fullname = remotedir + File.separator + fname;
				else
					fullname = remotedir + fname;

				File file = new File(fullname);
				if (file.exists()) {
					logger.info("File:" + fullname + "(success)");
					dataSource = new FileDataSource(fullname);
					dh = new DataHandler(dataSource);
				} else {
					logger.info("File:" + fullname + "(failed)");
				}

			} else
				logger.info("failed to get filedata. Fault remotedir : " + remotedir);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return dh;
	}

	@Override
	public DataHandler getFileData_Web(String fname, String remotedir, int agentId) {
		logger.info("getFileData_Web " + fname + ", " + remotedir + ", " + agentId);

		FileDataSource dataSource = null;
		DataHandler dh = null;
		String fullname = null;
		try {
			if (!remotedir.endsWith(File.separator))
				fullname = remotedir + File.separator + fname;
			else
				fullname = remotedir + fname;

			File file = new File(fullname);
			if (file.exists()) {
				logger.info("File:" + fullname + "(success)");
				dataSource = new FileDataSource(fullname);
				dh = new DataHandler(dataSource);
			} else {
				logger.warn("File does not exist");
				logger.info("File:" + fullname + "(failed)");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return dh;
	}

	@Override
	public long calculateServerCheckSum(String fname, String remotedir) {
		String fullname = null;
		long checksum = -1;

		if (!remotedir.endsWith(File.separator))
			fullname = remotedir + File.separator + fname;
		else
			fullname = remotedir + fname;

		File file = new File(fullname);

		if (file.exists()) {
			try {
				checksum = ChecksumChecker.getFileChecksum(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.toString());
				e.printStackTrace();
			}
		} else {
			logger.error("cannot find file and calculate file checksum.");
		}

		return checksum;
	}

	@Override
	public boolean createFolder(UUID uid, String dir) {
		logger.info("createFolder {}");

		FTPClient client = clientMap.get(uid);
		boolean result = false;
		try {
			result = client.makeDirectory(dir);

			if (result)
				logger.info("created folder : " + dir);
			else
				logger.info("failed to create folder.Subfolder path error(only 1 folder) or pre-created folder : " + dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean createFolder(UUID uid, String dir, int agentId) {
		logger.info("createFolder {}", agentId);

		FTPClient client = clientMap.get(uid);
		boolean result = false;
		try {
			result = client.makeDirectory(dir);

			if (result)
				logger.info("created folder : " + dir);
			else
				logger.info("failed to create folder. not created subfolder or pre-created folder : " + dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean deleteFolder(UUID uid, String dir) {
		logger.info("deleteFolder {}");

		FTPClient client = clientMap.get(uid);
		boolean result = false;

		try {
			if (client.changeWorkingDirectory(dir)) {
				FTPFile[] files = client.listFiles(".");
				if (files.length == 0) {
					client.changeToParentDirectory();
					result = client.removeDirectory(dir);
					logger.info("removed directory : " + dir + "(" + result + ")");
					return result;
				} else {
					for (FTPFile fi : files) {
						int type = fi.getType();
						if (type == FTPFile.FILE_TYPE)
							client.deleteFile(fi.getName());
						else if (type == FTPFile.DIRECTORY_TYPE) {
							if (!client.removeDirectory(fi.getName()))
								deleteFolder(uid, fi.getName());
						} else {
							logger.info("failed to remove folder. Unknown File Type :" + fi.getName());
							return result;
						}
					}
					client.changeToParentDirectory();
					result = client.removeDirectory(dir);
					logger.info("removed directory : " + dir + "(" + result + ")");
					return result;
				}
			} else {
				logger.info("failed to remove folder. not exist folder : " + dir);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean deleteFolder(UUID uid, String dir, int agentId) {
		logger.info("deleteFolder {}", agentId);

		FTPClient client = clientMap.get(uid);
		boolean result = false;

		try {
			if (client.changeWorkingDirectory(dir)) {
				FTPFile[] files = client.listFiles(".");
				if (files.length == 0) {
					client.changeToParentDirectory();
					result = client.removeDirectory(dir);
					logger.info("removed directory : " + dir + "(" + result + ")");
					return result;
				} else {
					for (FTPFile fi : files) {
						int type = fi.getType();
						if (type == FTPFile.FILE_TYPE)
							client.deleteFile(fi.getName());
						else if (type == FTPFile.DIRECTORY_TYPE) {
							if (!client.removeDirectory(fi.getName()))
								deleteFolder(uid, fi.getName(), agentId);
						} else {
							logger.info("failed to remove folder. Unknown File Type :" + fi.getName());
							return false;
						}
					}
					client.changeToParentDirectory();
					result = client.removeDirectory(dir);
					logger.info("removed directory : " + dir + "(" + result + ")");
					return result;
				}
			} else {
				logger.info("failed to remove folder. not exist folder : " + dir);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean deleteFile(UUID uid, String fname, String remotedir) {
		// TODO Auto-generated method stub
		logger.info("deleteFile");

		FTPClient client = clientMap.get(uid);
		boolean result = false;

		try {
			if (client.changeWorkingDirectory(remotedir)) {
				result = client.deleteFile(fname);
				logger.info("removed file : " + fname + "(" + result + ")");
				return result;
			} else {
				logger.info("failed to delete files. not exist folder : " + remotedir);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean deleteFileAll(UUID uid, ArrayList<String> fname, String dir) {
		logger.info("deleteFileAll");

		FTPClient client = clientMap.get(uid);
		try {
			if (client.changeWorkingDirectory(dir)) {
				int i = 1;
				for (String name : fname) {
					client.deleteFile(name);
					logger.info("[" + i + "]" + "removed file : " + name);
				}
				return true;
			} else {
				logger.info("failed to delete files. not exist folder : " + dir);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public FTPFile[] getAllList(UUID uid, String dir) {
		logger.info("getAllList");

		FTPClient client = clientMap.get(uid);
		try {
			if (client.changeWorkingDirectory(dir)) {
				FTPFile[] afile = client.listFiles(".");
				for (FTPFile file : afile) {
					if (file.getType() == FTPFile.DIRECTORY_TYPE)
						// System.out.println("D "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("Directory : " + file.getName());
					else if (file.getType() == FTPFile.FILE_TYPE)
						// System.out.println("F "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("File : " + file.getName());
					else if(file.getType() == FTPFile.SYMBOLIC_LINK_TYPE)
						// System.out.println("U "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("Symbolic link : " + file.getName());
				}
				return afile;
			} else
				logger.info("failed to get All file list. not exist folder : " + dir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FTPFile[] getFileList(UUID uid, String dir) {
		logger.info("getFileList");

		FTPClient client = clientMap.get(uid);
		List<FTPFile> rfile = new ArrayList<FTPFile>();

		try {
			if (client.changeWorkingDirectory(dir)) {
				FTPFile[] afile = client.listFiles();
				for (FTPFile file : afile) {
					if (file.getType() == FTPFile.FILE_TYPE) {
						rfile.add(file);
						// System.out.println("F "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("File : " + file.getName());
					}
				}
				return rfile.toArray(new FTPFile[rfile.size()]);
			} else
				logger.info("failed to get file list. fault directory.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FTPFile[] getFileListFilter(UUID uid, String dir, String filter) {
		logger.info("getFileListFilter");

		FTPClient client = clientMap.get(uid);
		List<FTPFile> rfile = new ArrayList<FTPFile>();

		final String innerFilter = filter;
		try {
			if (client.changeWorkingDirectory(dir)) {

				FTPFileFilter ftpFilter = new FTPFileFilter() {

					@Override
					public boolean accept(FTPFile arg0) {

						if (arg0.getName().endsWith(innerFilter)) {
							return true;
						} else {
							return false;
						}
					}
				};

				FTPFile[] afile = client.listFiles(".", ftpFilter);

				for (FTPFile file : afile) {
					if (file.getType() == FTPFile.FILE_TYPE) {
						rfile.add(file);
						// System.out.println("F "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("File : " + file.getName());
					}
				}
				return rfile.toArray(new FTPFile[rfile.size()]);
			} else
				logger.info("failed to get file list. fault directory.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public FTPFile[] getFolderList(UUID uid, String dir) {
		logger.info("getFolderList");

		FTPClient client = clientMap.get(uid);
		List<FTPFile> rfile = new ArrayList<FTPFile>();

		try {
			if (client.changeWorkingDirectory(dir)) {
				FTPFile[] afile = client.listFiles();
				for (FTPFile file : afile) {
					if (file.getType() == FTPFile.DIRECTORY_TYPE) {
						rfile.add(file);
						// System.out.println("D "
						// + file.getName()
						// + " "
						// + FileUtils.byteCountToDisplaySize(file
						// .getSize()));
						logger.info("Foler : " + file.getName());
					}
				}
				return rfile.toArray(new FTPFile[rfile.size()]);
			} else
				logger.info("failed to get directory list. fault directory.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean hasFolder(UUID uid, String dir) {
		logger.info("hasFolder {}");
		boolean result = false;

		FTPClient client = clientMap.get(uid);
		try {
			if (client.changeWorkingDirectory(dir)) {
				logger.info("Folder : " + dir + "(true)");
				result = true;
			} else {
				logger.info("Folder : " + dir + "(false)");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean hasFolder(UUID uid, String dir, int agentId) {
		logger.info("hasFolder {}", agentId);
		boolean result = false;

		FTPClient client = clientMap.get(uid);
		try {
			if (client.changeWorkingDirectory(dir)) {
				logger.info("Folder : " + dir + "(true)");
				result = true;
			} else {
				logger.info("Folder : " + dir + "(false)");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean logout(UUID uid, int agentId) {
		logger.info("logout {}", agentId);
		boolean result = false;

		FTPClient client = clientMap.get(uid);
		try {
			client.logout();
			client.disconnect();
			logger.info("Logout Success!");
			result = true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			clientMap.remove(uid);
		}

		return result;
	}

	@Override
	public boolean logout(UUID uid) {
		logger.info("logout {}");
		boolean result = false;

		FTPClient client = clientMap.get(uid);
		try {
			client.logout();
			client.disconnect();
			logger.info("Logout Success!");
			result = true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} finally {
			clientMap.remove(uid);
		}

		return result;
	}

	@Override
	public boolean changeWD(UUID uid, String dir) {
		// TODO Auto-generated method stub
		logger.info("change working directory {}");

		FTPClient client = clientMap.get(uid);

		try {
			return client.changeWorkingDirectory(dir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

		return false;
	}

	@Override
	public boolean changeWD(UUID uid, String dir, int agentId) {
		// TODO Auto-generated method stub
		logger.info("change working directory {}", agentId);

		FTPClient client = clientMap.get(uid);

		try {
			return client.changeWorkingDirectory(dir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

		return false;
	}

	@Override
	public String printWD(UUID uid) {
		// TODO Auto-generated method stub

		FTPClient client = clientMap.get(uid);
		try {
			return client.printWorkingDirectory();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return null;
	}

	@Override
	public boolean changeToParentWD(UUID uid) {
		// TODO Auto-generated method stub

		FTPClient client = clientMap.get(uid);
		try {
			return client.changeToParentDirectory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return false;
	}

	public void run() {
		// Thread m1 = new MultiThread(this);
		// Thread m2 = new MultiThread(this);
		// Thread m3 = new MultiThread(this);
		// Thread m4 = new MultiThread(this);
		// Thread m5 = new MultiThread(this);
		// Thread m6 = new MultiThread(this);
		// Thread m7 = new MultiThread(this);
		// Thread m8 = new MultiThread(this);
		// Thread m9 = new MultiThread(this);
		// Thread m = new MultiThread(this);
		// Thread m9 = new MultiThread(this);
		//
		//
		// m1.start();
		// m2.start();
		// m3.start();
		// m4.start();
		// m5.start();
		// m6.start();
		// m7.start();
		// m8.start();
		// m9.start();

	}

	public static void main(String arg[]) {

		UserDataManagerImpl udclient = new UserDataManagerImpl();

		boolean ret = udclient.changeFileExecutableMod("err");
		System.out.println(ret);

		// String fullname = "test_1G";
		// String remotedir = "/htcaas/p258rsw/";
		// int agentId = 0;
		// DataHandlerFile dhfile = new DataHandlerFile();
		// DataSource source = new FileDataSource(new File(fullname));
		// DataHandler dh = new DataHandler(source);
		// dhfile.setDfile(dh);
		// dhfile.setName(dh.getName());
		//
		// udclient.putFileData_Web(dhfile, remotedir, agentId);
		//
		// ud.run();

		// FTPClient client = new FTPClient();
		// UUID uid = UUID.randomUUID();
		// clientMap.put(uid, client);
		// String id = "p143ksw";
		// String passwd = "kisti0568%";
		// try {
		//
		// client.connect("150.183.158.172", 50021);
		// int reply = client.getReplyCode();
		// if (!FTPReply.isPositiveCompletion(reply)) {
		// client.disconnect();
		// }
		// if (!client.login(id, passwd)) {
		// client.logout();
		// }
		// client.setFileType(FTP.BINARY_FILE_TYPE);
		// client.enterLocalPassiveMode();
		// client.logout();
		//
		// } catch (Exception e) {
		// logger.error("Failed to connect ftpServer: {}", e.getMessage());
		// logger.info("Login failed! ID : {}", id);
		// }

		// UUID uid = udclient.login("150.183.158.172", "p312kjs", "jiksoo75!");
		// UUID uid = udclient.login("150.183.158.172", "p258rsw",
		// "kisti4001!@#");
		// UUID uid = udclient.login("150.183.158.172", "p260ksy", "ksy5295)#");
		// UUID uid = udclient.login("150.183.158.172", "p330ksw",
		// "ksw0408!@#");
		// FTPFile[] test = udclient.getFileListFilter(uid,
		// "/htcaas/p258rsw/koh/test/proteins/10gs", "pdbqt" );
		// for(FTPFile file : test){
		// System.out.println(file.getName());
		// }
		// udclient.logout(uid);
		// UUID uid = udclient.login("150.183.158.172", "plsiportal",
		// "zltmxl^^456");

		// long a = udclient.calculateServerCheckSum("build.sh",
		// "/phome01/p258rsw/");
		// System.out.println(a);

		// System.out.println(udclient.changeWD(uid, "/home/seungwoo/"));

		// String localdir = ".";
		// String remotedir = "/home/test/";
		// String fname = "helloworld.sh";
		// File dir = new File(localdir);
		// if (!dir.exists())
		// dir.mkdirs();
		// System.out.println(udclient.printWD(uid));
		// DataHandler dh = udclient.getFileDataDiff(uid, fname, remotedir, 0);
		// // DataHandler dh = udclient.getFileData(uid, fname, remotedir);
		// try {
		// FileOutputStream outputStream = null;
		// if (localdir.endsWith(File.separator)) {
		// outputStream = new FileOutputStream(localdir + fname);
		// } else
		// outputStream = new FileOutputStream(localdir + File.separator
		// + fname);
		//
		// dh.writeTo(outputStream);
		// outputStream.flush();
		// outputStream.close();
		//
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// logger.error(e.getMessage());
		// }

		// ud.changeWD("/home/seungwoo/output");
		// udclient.getAllList(uid, "/home/p258rsw/");
		// ud.getFileData(uid, "test.zip", ".");

		// String fname = "test.zip";
		// String localdir = "/home/seungwoo";
		// String remotedir = "/home/seungwoo/test";
		// int agentId = 0;
		// boolean result = false;
		// File dir = new File(localdir);
		// if (!dir.exists())
		// dir.mkdirs();
		// System.out.println(ud.printWD(uid));
		// DataHandler dh = ud.getFileDataDiff(uid, fname, remotedir, agentId);
		// if(dh != null){
		// try {
		// FileOutputStream outputStream = null;
		// if (localdir.endsWith(File.separator)) {
		// outputStream = new FileOutputStream(localdir + fname);
		// } else
		// outputStream = new FileOutputStream(localdir + File.separator
		// + fname);
		//
		// dh.writeTo(outputStream);
		// outputStream.flush();
		// outputStream.close();
		//
		//
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// logger.error(e.getMessage());
		// }
		//
		// }
		//
		// udclient.logout(uid, 0);

	}
}

package org.kisti.htc.udmanager.server;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.net.ftp.FTPFile;
import org.kisti.htc.udmanager.bean.DataHandlerFile;

// TODO: Auto-generated Javadoc
/**
 * The Interface UserDataManager.
 * 
 * @author seungwoo
 * @version 1.0
 */
public interface UserDataManager {

	/**
	 * Login.
	 * 
	 * @param address
	 *            server's ftp address
	 * @param id
	 *            the id
	 * @param passwd
	 *            the passwd
	 * @return the uuid
	 */
	public UUID login(String address, String id, String passwd);

	/**
	 * Login.
	 * 
	 * @param address
	 *            server's ftp address
	 * @param id
	 *            the id
	 * @param passwd
	 *            the passwd
	 * @param agentId
	 *            the agent id
	 * @return the uuid
	 */
	public UUID login(String address, String id, String passwd, int agentId) throws SocketTimeoutException;

	
	public boolean changeFileOwn(String uid, String filePath);
	
	public boolean changeFileExecutableMod(String filePath);
	
	/**
	 * Put file data.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param udfile
	 *            the user data handler file
	 * @param remotedir
	 *            the remote dir
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean putFileData(UUID uuid, DataHandlerFile udfile, String remotedir, int agentId);
	
	public boolean putFileData_Web(DataHandlerFile udfile, String remotedir, int agentId);
	
	public boolean putFileData_Web(DataHandlerFile udfile, String remotedir, int agentId, String uid);

	/**
	 * Put file data all.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param flist
	 *            the data handler file list
	 * @param remotedir
	 *            the remote dir
	 * @return true, if successful
	 */
	public boolean putFileDataAll(UUID uuid, List<DataHandlerFile> flist, String remotedir);

	/**
	 * Put file data all.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param flist
	 *            the data handler file list
	 * @param remotedir
	 *            the remote dir
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean putFileDataAll(UUID uuid, List<DataHandlerFile> flist, String remotedir, int agentId);

	/**
	 * Gets the file data handler when ftp server and userdata server is
	 * differnt.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param fname
	 *            the file name to get
	 * @param remotedir
	 *            the remote dir
	 * @param agentId
	 *            the agent id
	 * @return the file data hanlder stream
	 */
	
	@Deprecated
	public DataHandler getFileDataDiff(UUID uuid, String fname, String remotedir, int agentId);

	/**
	 * Gets the file data handler when ftp server and userdata server is same.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param fname
	 *            the file name to get
	 * @param remotedir
	 *            the remote dir
	 * @param agentId
	 *            the agent id
	 * @return the file data handler stream
	 */
	public DataHandler getFileData(UUID uuid, String fname, String remotedir, int agentId);
	
	public DataHandler getFileData_Web(String fname, String remotedir, int agentId);

	public long calculateServerCheckSum(String fname, String remotedir);

	/**
	 * Creates the folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to make
	 * @return true, if successful
	 */
	public boolean createFolder(UUID uuid, String dir);

	/**
	 * Creates the folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to make
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean createFolder(UUID uuid, String dir, int agentId);

	/**
	 * Delete folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to remove
	 * @return true, if successful
	 */
	public boolean deleteFolder(UUID uuid, String dir);

	/**
	 * Delete folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to remove
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean deleteFolder(UUID uuid, String dir, int agentId);

	/**
	 * Delete file.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param fname
	 *            the file name to remove
	 * @param remotedir
	 *            the remote dir
	 * @return true, if successful
	 */
	public boolean deleteFile(UUID uuid, String fname, String remotedir);

	/**
	 * Delete file all.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param fname
	 *            the file name list to remove
	 * @param remotedir
	 *            the remote dir to remove
	 * @return true, if successful
	 */
	public boolean deleteFileAll(UUID uuid, ArrayList<String> fname, String remotedir);

	/**
	 * Gets the all list.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to access
	 * @return all the file and directory list
	 */
	public FTPFile[] getAllList(UUID uuid, String dir);

	/**
	 * Gets the file list.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to access
	 * @return the file list
	 */
	public FTPFile[] getFileList(UUID uuid, String dir);
	
	public FTPFile[] getFileListFilter(UUID uuid, String dir, String filter);

	/**
	 * Gets the folder list.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to access
	 * @return the folder list
	 */
	public FTPFile[] getFolderList(UUID uuid, String dir);

	/**
	 * Checks for folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to check
	 * @return true, if successful
	 */
	public boolean hasFolder(UUID uuid, String dir);

	/**
	 * Checks for folder.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to check
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean hasFolder(UUID uuid, String dir, int agentId);

	/**
	 * Change working directory.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to change
	 * @return true, if successful
	 */
	public boolean changeWD(UUID uuid, String dir);

	/**
	 * Change working directory.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param dir
	 *            the dir to change
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean changeWD(UUID uuid, String dir, int agentId);

	/**
	 * Prints the working directory.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @return the string
	 */
	public String printWD(UUID uuid);

	/**
	 * Change to parent working directory.
	 * 
	 * @param uuid
	 *            the uuid
	 * @return true, if successful
	 */
	public boolean changeToParentWD(UUID uuid);

	/**
	 * Logout.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @return true, if successful
	 */
	public boolean logout(UUID uuid);

	/**
	 * Logout.
	 * 
	 * @param uuid
	 *            the ftp connection uuid
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean logout(UUID uuid, int agentId);

}

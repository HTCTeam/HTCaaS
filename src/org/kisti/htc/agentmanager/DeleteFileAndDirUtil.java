package org.kisti.htc.agentmanager;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;


/**
 * @author Song Ji-Hoon.
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeleteFileAndDirUtil {

	final static Logger logger = LoggerFactory.getLogger(AgentManager.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
	
	public static void main(String[] args) {
	}

	public static void deleteFilesAndDirs(String path) {
		deleteFiles(path);
		deleteDirs(path);
	}

	public static void deleteFiles(String path) {		
		File file = new File(path);
		File[] files = file.listFiles();

		if (files == null) {
			return;
		}

		if (files.length != 0) {
			for (int i =  0; i < files.length; i++) {
				if (files[i].isFile()) {
					files[i].delete();
				} else {
					deleteFiles(files[i].getPath());
				}
			}
		}
	}

	public static void deleteDirs(String path) {
		File dir = new File(path);
		File[] dirs = dir.listFiles();

		if (dirs == null) {
			return;
		}

		if (dirs.length != 0) {
			for (int i = 0; i < dirs.length; i++) {				
				if (dirs[i].listFiles().length == 0) {
					dirs[i].delete();
				} else {
					deleteDirs(dirs[i].getPath());
				}
			}
		}
		dir.delete();
	}
}

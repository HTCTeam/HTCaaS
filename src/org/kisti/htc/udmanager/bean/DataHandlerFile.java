package org.kisti.htc.udmanager.bean;

import java.io.Serializable;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class DataHandlerFile implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4673048233468570717L;

	private String Name;
	private String FileType;
	
	@XmlMimeType("application/octet-stream")
	private DataHandler Dfile;

	public String getName() {
		return this.Name;
	}

	public void setName(String Name) {
		this.Name = Name;
	}

	public DataHandler getDfile() {
		return this.Dfile;
	}

	public void setDfile(DataHandler Dfile) {
		this.Dfile = Dfile;
	}

	public String getFileType() {
		return FileType;
	}

	public void setFileType(String FileType) {
		this.FileType = FileType;
	}
}

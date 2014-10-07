package other;

import com.sun.org.apache.bcel.internal.generic.GETFIELD;

public class FileDownloadLink {
	private String fileName = null;
	private String downloadLink = null;
	
	public FileDownloadLink(String fileName, String downloadLink){
		this.fileName = fileName;
		this.downloadLink = downloadLink;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public String getDownloadLink(){
		return downloadLink;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void setDownloadLink(String downloadLink){
		this.downloadLink = downloadLink;
	}

}

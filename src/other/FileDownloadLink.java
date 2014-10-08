package other;
/**
 * Data structure that contains file name and download links
 * @author Ervin
 *
 */
public class FileDownloadLink {
	private String fileName = null;
	private String downloadLink = null;
	
	/**
	 * Constructor
	 * @param fileName Name of the file
	 * @param downloadLink Download link of the file
	 */
	public FileDownloadLink(String fileName, String downloadLink){
		this.fileName = fileName;
		this.downloadLink = downloadLink;
	}
	
	/**
	 * Returns file name
	 * @return file name
	 */
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * Returns download link
	 * @return download link
	 */
	public String getDownloadLink(){
		return downloadLink;
	}
	
	/**
	 * Sets file name
	 * @param fileName file name
	 */
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Sets Download link
	 * @param downloadLink download link
	 */
	public void setDownloadLink(String downloadLink){
		this.downloadLink = downloadLink;
	}

}

package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.RequestContext;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import apis.GoogleDrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;


/**
 * Servlet implementation class OAuth2Callback
 */
@WebServlet("/oauth2callback")
@MultipartConfig(
		fileSizeThreshold=1024*1024*2, // 2MB
		maxFileSize=1024*1024*10,      // 10MB
		maxRequestSize=1024*1024*50)   // 50MB

public class OAuth2CallBack extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SAVE_DIR = "uploads";
	GoogleDrive googleDrive;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OAuth2CallBack() {
		super();
		googleDrive = new GoogleDrive();
		
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stubv
		System.out.println(request.getParameter("code"));
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+"driveready.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+uploadFile(request, response));
		dispatcher.forward(request, response);
	}
	
	private String uploadFile(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		
		// gets absolute path of the web application
		String appPath = request.getServletContext().getRealPath("");
		// constructs path of the directory to save uploaded file
		String savePath = appPath + File.separator + SAVE_DIR;

		// creates the save directory if it does not exists
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}

		try {
			for (Part part : request.getParts()) {
				String fileName = extractFileName(part);
				part.write(savePath + File.separator + fileName);
				System.out.println(savePath + File.separator + fileName);
				com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
			    body.setTitle(extractFileName(part));
			    body.setDescription("A test document");
			    body.setMimeType(part.getContentType());

			    java.io.File fileContent = new File(savePath + File.separator + fileName);;
			    FileContent mediaContent = new FileContent("text/plain", fileContent);
			    
			    
			    googleDrive.send(body, mediaContent);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		
		System.out.println("Done!");
		request.setAttribute("message", "Successfully Uploaded File");
		return "uploadfile.jsp";
	}

	/**
	 * Extracts file name from HTTP header content-disposition
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				String whole = s.substring(s.indexOf("=") + 2, s.length()-1);
				String[] split = whole.replace('\\', '/').split("/");
				return split[split.length-1];
			}
		}
		return "";
	}

}

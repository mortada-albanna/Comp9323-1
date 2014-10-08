package controller;


import interfaces.UserManagement;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import apis.GoogleDrive;
import apis.Stormpath;

import com.google.api.client.http.FileContent;


/**
 * Servlet implementation class Controller
 */
@WebServlet("/controller")
@MultipartConfig(
		fileSizeThreshold=1024*1024*10, // 10MB
		maxFileSize=1024*1024*20,      // 20MB
		maxRequestSize=1024*1024*50)   // 50MB
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(Controller.class.getName());

	private UserManagement stormpath;
	private GoogleDrive googleDrive;
	private static final String SAVE_DIR = "uploads";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Controller() {
		super();
		stormpath = new Stormpath();
		googleDrive = new GoogleDrive();

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.getParameter("code"));
		// TODO not sure why it tries to access driveready here
		try{
			googleDrive.initDrive(request.getParameter("code"));
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+"driveready.jsp");
			dispatcher.forward(request, response);
		}catch(NullPointerException e){
			// Default go to login page or welcome page if you are logged in
			HttpSession session = request.getSession();
			String user = (String)session.getAttribute("user");
			String forwardPage="";
			if( user == null){
				forwardPage = login(request, response, session);
			}else{
				forwardPage = "WEB-INF/welcome.jsp";
			}
			System.out.println("doGet: user=" + user + " " + new Date(session.getLastAccessedTime()));
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
			dispatcher.forward(request, response);

		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String forwardPage = "";
		String action = request.getParameter("action");
		HttpSession session = request.getSession(true);
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart){
			forwardPage = uploadFile(request, response, session);
		}else{
			
			if(action==null){
				logger.info("invalid action");

				System.out.println(request.getParameterNames());
			}else{
				switch (action){
				case "login":
					forwardPage = login(request, response, session);
					break;
				case "register":
					forwardPage = register(request, response, session);
					break;
				case "download":
					forwardPage = download(request, response, session);
					break;
				case "list_files":
					forwardPage = listFiles(request, response, session);
					break;
				case "create_account":
					forwardPage = createAccount(request, response, session);
					break;
				case "get_details":
					forwardPage = getDetails(request, response, session);
					break;
				case "set_password":
					forwardPage = setPassword(request, response, session);
					break;
				default:
					//TODO return 404 not found
					
				}
			}
		}
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
		dispatcher.forward(request, response);
	}

	/**
	 * Takes user to the downloadFile page
	 * @param request
	 * @param response
	 * @param session
	 * @return downloadFile page
	 */
	private String download(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		return "downloadFile.jsp";
	}

	/**
	 * Sets the "links" attribute to contain a list of FileDownloadLinks
	 * @param request
	 * @param response
	 * @param session
	 * @return listFile page
	 */
	private String listFiles(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		try {
			request.setAttribute("links", googleDrive.getFileDownloadLinks());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "listFiles.jsp";
	}

	/**
	 * Uploads a file to the Google Drive
	 * @param request
	 * @param response
	 * @param session
	 * @return uploadFile page
	 */
	private String uploadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {

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
				if(  fileName != null && fileName.matches(".*[^a-zA-Z0-9_].*") ){
					request.setAttribute("message", "Filename contained illegal characthers");
					return "WEB-INF/welcome.jsp";
				
				}
				if (fileName != null){
					System.out.println("Saving to " + savePath + File.separator + fileName);
					part.write(savePath + File.separator + fileName);
					System.out.println(savePath + File.separator + fileName);
					com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
					body.setTitle(extractFileName(part));
					body.setDescription("A test document");
					body.setMimeType(part.getContentType());

					java.io.File fileContent = new File(savePath + File.separator + fileName);;
					FileContent mediaContent = new FileContent("text/plain", fileContent);
					

					googleDrive.send(body, mediaContent);
					fileContent.delete();
				}


			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}




		System.out.println("Done!");
		request.setAttribute("message", "Successfully Uploaded File");
		return "WEB-INF/welcome.jsp";
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
		return null;
	}

	/**
	 * Sets the password for the account given
	 * @param request
	 * @param response
	 * @param session
	 * @return setPassword page
	 */
	private String setPassword(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		if (stormpath.setPassword(request.getParameter("username"), request.getParameter("password"))){
			request.setAttribute("message", "successfully changed password");
		}else{
			request.setAttribute("message", "could not find matching account");
		}
		return "setpassword.jsp";
	}

	/**
	 * returns details of the given account in a string
	 * @param request
	 * @param response
	 * @param session
	 * @return getDetails page
	 */
	private String getDetails(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		request.setAttribute("message", stormpath.getDetails(request.getParameter("username")));
		return "getdetails.jsp";
	}

	/**
	 * Takes user to the registration page
	 * @param request
	 * @param response
	 * @param session
	 * @return createAccount page
	 */
	private String register(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		return "createaccount.jsp";
	}

	/**
	 * Authenticates the user
	 * @param request
	 * @param response
	 * @param session
	 * @return welcome page
	 */
	private String login(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		String forwardPage = null;
		if( session.getAttribute("user") == null){
			if (stormpath.authenticateAccount(request.getParameter("username"), request.getParameter("password"))){
				forwardPage = "WEB-INF/welcome.jsp";
				session.setAttribute("user", request.getParameter("username"));
				session.setAttribute("group", stormpath.getAuthorizationGroup(request.getParameter("username")));
			}else{
				forwardPage = "login.jsp";
				request.setAttribute("message", "login attempt failed.");
			}
		}else{
			forwardPage ="WEB-INF/welcome.jsp";
			System.out.println("login: user = " + session.getAttribute("user"));
		}

		return forwardPage;
	}

	/**
	 * Creates an account which defaults the username to the email.
	 * Password needs to be non trivial ie. needs to contain upper and lower case letters with numbers and
	 * special characters.
	 * @param request
	 * @param response
	 * @param session
	 * @return login page
	 */
	private String createAccount(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {

		//Set the account properties
		String givenName = request.getParameter("given_name");
		String surname = request.getParameter("surname");
		String userName = request.getParameter("email"); //optional, defaults to email if unset
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
		if (password.equals(password2)){
			stormpath.createAccount(givenName, surname, userName, password, email, "Student");
			request.setAttribute("message", "Registration Successful!");
		}


		//Create the account using the existing Application object


		return "login.jsp";
	}

}
